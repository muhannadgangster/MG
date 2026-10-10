package com.example.data.repository

import com.example.data.local.MgDao
import com.example.data.model.ChatMessage
import com.example.data.model.Comment
import com.example.data.model.Draft
import com.example.data.model.Follow
import com.example.data.model.NotificationItem
import com.example.data.model.Report
import com.example.data.model.SavedVideo
import com.example.data.model.SearchHistory
import com.example.data.model.User
import com.example.data.model.Video
import com.example.data.model.VideoLike
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.data.model.UserPresence
import android.content.Context
import com.example.R
import com.example.util.AuthValidator
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UnverifiedEmailException(
    message: String,
    val firebaseUser: FirebaseUser? = null
) : Exception(message)

class MgRepository(
    private val dao: MgDao,
    val firestore: FirebaseFirestore? = null,
    val auth: FirebaseAuth? = null
) {
    constructor(context: Context, dao: MgDao) : this(
        dao = dao,
        firestore = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    val currentUserValue: User? get() = _currentUser.value

    // Real-time presence state map for all users
    private val _presenceMap = MutableStateFlow<Map<String, UserPresence>>(
        mapOf(
            "lg_official" to UserPresence("lg_official", isOnline = true),
            "creator_alex" to UserPresence("creator_alex", isOnline = true),
            "creator_elena" to UserPresence("creator_elena", isOnline = false, lastSeenTimestamp = System.currentTimeMillis() - 12 * 60 * 1000),
            "creator_kai" to UserPresence("creator_kai", isOnline = true),
            "admin_root" to UserPresence("admin_root", isOnline = true)
        )
    )
    val presenceMap: Flow<Map<String, UserPresence>> = _presenceMap.asStateFlow()

    // Anti-spam view tracker: Set of "videoId_userId" already counted in this session
    private val viewedVideosSession = mutableSetOf<String>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
            restoreActiveSession()
        }
    }

    private suspend fun restoreActiveSession() {
        try {
            val currentFbUser = auth?.currentUser ?: return
            currentFbUser.reload().await()
            if (currentFbUser.isEmailVerified || currentFbUser.providerData.any { it.providerId == "google.com" }) {
                loginWithFirebaseUser(currentFbUser)
            } else {
                _currentUser.value = null
            }
        } catch (e: Exception) {
            val currentFbUser = auth?.currentUser
            if (currentFbUser != null && (currentFbUser.isEmailVerified || currentFbUser.providerData.any { it.providerId == "google.com" })) {
                val localUser = dao.getUserByIdOnce(currentFbUser.uid)
                if (localUser != null && !localUser.isBanned) {
                    _currentUser.value = localUser
                }
            }
        }
    }

    // --- User Presence Operations ---
    fun recordUserHeartbeat(userId: String) {
        val current = _presenceMap.value.toMutableMap()
        current[userId] = UserPresence(userId, isOnline = true, lastSeenTimestamp = System.currentTimeMillis())
        _presenceMap.value = current
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateUserPresence(userId, isOnline = true, lastActiveAt = System.currentTimeMillis())
        }
    }

    fun updateUserPresence(userId: String, isOnline: Boolean) {
        val current = _presenceMap.value.toMutableMap()
        current[userId] = UserPresence(userId, isOnline = isOnline, lastSeenTimestamp = System.currentTimeMillis())
        _presenceMap.value = current
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateUserPresence(userId, isOnline = isOnline, lastActiveAt = System.currentTimeMillis())
        }
    }

    fun getUserPresence(userId: String): UserPresence {
        return _presenceMap.value[userId] ?: UserPresence(userId, isOnline = false, lastSeenTimestamp = System.currentTimeMillis() - 3600000)
    }

    // --- Authentication & Profile ---
    suspend fun loginWithFirebaseUser(firebaseUser: com.google.firebase.auth.FirebaseUser): Result<User> {
        val email = firebaseUser.email ?: "${firebaseUser.uid}@lg.app"
        val displayName = firebaseUser.displayName ?: "LG Creator"
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        val existing = dao.getUserByIdOnce(firebaseUser.uid) ?: dao.getUserByEmail(email)
        val user = if (existing != null) {
            existing.copy(id = firebaseUser.uid, displayName = displayName.ifBlank { existing.displayName }, avatarUrl = photoUrl.ifBlank { existing.avatarUrl })
        } else {
            val baseName = email.substringBefore("@").replace(".", "").lowercase()
            val finalUsername = findAvailableUsername(baseName)
            val newUser = User(
                id = firebaseUser.uid,
                username = finalUsername,
                displayName = displayName.ifBlank { "LG Creator" },
                email = email,
                avatarUrl = photoUrl,
                bio = "Hello LG world! 🚀 Creative mind & liquid glass enthusiast.",
                followersCount = 120,
                followingCount = 45,
                videosCount = 1,
                likesCount = 380,
                isVerified = false,
                role = if (email.contains("admin") || email == "admin@lg.app") "ADMIN" else "USER"
            )
            dao.insertUser(newUser)
            newUser
        }
        _currentUser.value = user
        recordUserHeartbeat(user.id)
        CoroutineScope(Dispatchers.IO).launch {
            createFirestoreUser(user)
        }
        return Result.success(user)
    }

    suspend fun createFirestoreUser(user: User): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val userMap = mapOf(
                "username" to user.username,
                "displayName" to user.displayName,
                "email" to user.email,
                "avatarUrl" to user.avatarUrl,
                "bio" to user.bio,
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "videosCount" to user.videosCount,
                "likesCount" to user.likesCount,
                "isVerified" to user.isVerified,
                "isPrivate" to user.isPrivate,
                "allowComments" to user.allowComments,
                "allowMessages" to user.allowMessages,
                "allowDownloads" to user.allowDownloads,
                "role" to user.role,
                "isBanned" to user.isBanned,
                "isOnline" to user.isOnline,
                "lastActiveAt" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            fs.collection("users").document(user.id).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFirestoreVideo(video: Video): Result<String> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val videoMap = mapOf(
                "creatorId" to video.creatorId,
                "creatorUsername" to video.creatorUsername,
                "creatorDisplayName" to video.creatorDisplayName,
                "creatorAvatar" to video.creatorAvatar,
                "videoUrl" to video.videoUrl,
                "thumbnailUrl" to video.thumbnailUrl,
                "title" to video.title,
                "description" to video.description,
                "hashtags" to video.hashtags,
                "audioTitle" to video.audioTitle,
                "audioArtist" to video.audioArtist,
                "views" to video.views,
                "likes" to video.likes,
                "comments" to video.comments,
                "shares" to video.shares,
                "saves" to video.saves,
                "visibility" to video.visibility,
                "commentsEnabled" to video.commentsEnabled,
                "downloadsEnabled" to video.downloadsEnabled,
                "remixEnabled" to video.remixEnabled,
                "status" to video.status,
                "moderationStatus" to video.moderationStatus,
                "durationSeconds" to video.durationSeconds,
                "gradientStart" to video.gradientStart,
                "gradientEnd" to video.gradientEnd,
                "category" to video.category,
                "isPinned" to video.isPinned,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            fs.collection("videos").document(video.videoId).set(videoMap).await()
            Result.success(video.videoId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setFirestorePresence(userId: String, isOnline: Boolean): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val map = mapOf(
                "userId" to userId,
                "isOnline" to isOnline,
                "lastSeenTimestamp" to FieldValue.serverTimestamp()
            )
            fs.collection("presence").document(userId).set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFirestoreComment(comment: Comment): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val map = mapOf(
                "videoId" to comment.videoId,
                "userId" to comment.userId,
                "username" to comment.username,
                "userAvatar" to comment.userAvatar,
                "text" to comment.text,
                "likesCount" to comment.likesCount,
                "parentCommentId" to (comment.parentCommentId ?: ""),
                "createdAt" to FieldValue.serverTimestamp()
            )
            fs.collection("comments").document(comment.commentId).set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFirestoreLike(videoId: String, userId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val map = mapOf(
                "videoId" to videoId,
                "userId" to userId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            fs.collection("video_likes").document("${videoId}_${userId}").set(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(email: String, displayName: String, photoUrl: String = ""): Result<User> {
        val existing = dao.getUserByEmail(email)
        val user = if (existing != null) {
            existing
        } else {
            // Generate clean username from email or name
            val baseName = email.substringBefore("@").replace(".", "").lowercase()
            val finalUsername = findAvailableUsername(baseName)
            val newUser = User(
                id = UUID.randomUUID().toString(),
                username = finalUsername,
                displayName = displayName.ifBlank { "LG Creator" },
                email = email,
                avatarUrl = photoUrl,
                bio = "Hello LG world! 🚀 Creative mind & tech enthusiast.",
                followersCount = 120,
                followingCount = 45,
                videosCount = 1,
                likesCount = 380,
                isVerified = false,
                role = if (email.contains("admin") || email == "admin@lg.app") "ADMIN" else "USER"
            )
            dao.insertUser(newUser)
            newUser
        }
        _currentUser.value = user
        recordUserHeartbeat(user.id)
        CoroutineScope(Dispatchers.IO).launch {
            createFirestoreUser(user)
        }
        return Result.success(user)
    }

    suspend fun loginWithEmail(email: String, password: String): Result<User> {
        val emailValidation = AuthValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            return Result.failure(IllegalArgumentException(emailValidation.errorMessage ?: "Invalid email address."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be blank."))
        }

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
                val fbUser = authResult.user ?: return Result.failure(IllegalStateException("Authentication succeeded but no user was returned."))

                // Refresh user state to verify latest email verification status
                fbUser.reload().await()

                if (!fbUser.isEmailVerified && !fbUser.providerData.any { it.providerId == "google.com" }) {
                    return Result.failure(UnverifiedEmailException("Your email address is not verified yet. Please verify your email before logging in.", fbUser))
                }

                return loginWithFirebaseUser(fbUser)
            } catch (e: UnverifiedEmailException) {
                return Result.failure(e)
            } catch (e: FirebaseAuthInvalidUserException) {
                return Result.failure(IllegalStateException("No account found with this email. Please check your email or register."))
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                return Result.failure(IllegalArgumentException("Incorrect password or email credentials."))
            } catch (e: FirebaseNetworkException) {
                return Result.failure(IllegalStateException("Network connection failed. Please check your internet connection."))
            } catch (e: Exception) {
                val localUser = dao.getUserByEmail(email.trim())
                if (localUser != null && !localUser.isBanned) {
                    _currentUser.value = localUser
                    return Result.success(localUser)
                }
                return Result.failure(Exception(e.localizedMessage ?: "Sign in failed."))
            }
        } else {
            val user = dao.getUserByEmail(email.trim())
            return if (user != null) {
                if (user.isBanned) {
                    Result.failure(IllegalStateException("This account has been suspended by administration."))
                } else {
                    _currentUser.value = user
                    Result.success(user)
                }
            } else {
                Result.failure(IllegalStateException("Account not found with this email. Please register first."))
            }
        }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): Result<FirebaseUser> {
        val emailValidation = AuthValidator.validateEmail(email)
        if (!emailValidation.isValid) {
            return Result.failure(IllegalArgumentException(emailValidation.errorMessage ?: "Invalid email address."))
        }

        val usernameValidation = AuthValidator.validateUsername(username)
        if (!usernameValidation.isValid) {
            return Result.failure(IllegalArgumentException(usernameValidation.errorMessage ?: "Invalid username."))
        }

        val passwordValidation = AuthValidator.validatePassword(password)
        if (!passwordValidation.isValid) {
            return Result.failure(IllegalArgumentException(passwordValidation.errorMessage ?: "Password does not meet requirements."))
        }

        val cleanUsername = username.trim().removePrefix("@").lowercase()
        if (!isUsernameAvailable(cleanUsername)) {
            val suggestions = generateUsernameSuggestions(cleanUsername)
            return Result.failure(IllegalStateException("Username already taken. Available options: ${suggestions.joinToString(", ")}"))
        }

        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Authentication service is not initialized."))

        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val fbUser = authResult.user ?: return Result.failure(IllegalStateException("Account creation returned no user record."))

            // Send genuine Firebase email verification link
            fbUser.sendEmailVerification().await()

            // Pre-seed unverified user locally in Room
            val unverifiedUser = User(
                id = fbUser.uid,
                username = cleanUsername,
                displayName = displayName.trim().ifBlank { cleanUsername },
                email = email.trim(),
                bio = "LG Creator ⚡️ Create. Discover. Connect.",
                followersCount = 0,
                followingCount = 0,
                videosCount = 0,
                likesCount = 0,
                isVerified = false,
                isOnline = false,
                lastActiveAt = System.currentTimeMillis(),
                role = if (email.trim() == "admin@lg.app" || email.trim().contains("admin")) "ADMIN" else "USER"
            )
            dao.insertUser(unverifiedUser)

            // Do NOT activate session until verified
            _currentUser.value = null

            Result.success(fbUser)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalStateException("An account with this email address already exists. Please sign in instead."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(IllegalArgumentException(e.localizedMessage ?: "The password is too weak."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException("Invalid email format or credentials."))
        } catch (e: FirebaseNetworkException) {
            Result.failure(IllegalStateException("Network connection failed. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Account registration failed."))
        }
    }

    suspend fun checkEmailVerificationStatus(
        targetUser: FirebaseUser? = null,
        username: String = "",
        displayName: String = ""
    ): Result<User> {
        val fbUser = targetUser ?: auth?.currentUser ?: return Result.failure(IllegalStateException("No active authentication session. Please sign in."))
        return try {
            fbUser.reload().await()
            if (fbUser.isEmailVerified) {
                val existing = dao.getUserByIdOnce(fbUser.uid)
                val cleanUsername = username.trim().removePrefix("@").lowercase().ifBlank { existing?.username ?: "user_${fbUser.uid.take(6)}" }
                val cleanDisplayName = displayName.trim().ifBlank { existing?.displayName ?: cleanUsername }
                val verifiedUser = (existing ?: User(
                    id = fbUser.uid,
                    username = cleanUsername,
                    displayName = cleanDisplayName,
                    email = fbUser.email ?: ""
                )).copy(
                    isVerified = true,
                    isOnline = true,
                    lastActiveAt = System.currentTimeMillis()
                )
                dao.insertUser(verifiedUser)
                _currentUser.value = verifiedUser
                recordUserHeartbeat(verifiedUser.id)
                CoroutineScope(Dispatchers.IO).launch {
                    createFirestoreUser(verifiedUser)
                }
                Result.success(verifiedUser)
            } else {
                Result.failure(IllegalStateException("Email is not verified yet. Please check your inbox and tap the verification link, then click 'Check Status'."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to verify email status."))
        }
    }

    suspend fun resendVerificationEmail(targetUser: FirebaseUser? = null): Result<Unit> {
        val fbUser = targetUser ?: auth?.currentUser ?: return Result.failure(IllegalStateException("No pending registration session found."))
        return try {
            fbUser.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to resend verification email."))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val validation = AuthValidator.validateEmail(email)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage ?: "Invalid email address."))
        }
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth service not available."))
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(IllegalStateException("No account found with this email address."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to dispatch password recovery link."))
        }
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        val clean = username.trim().removePrefix("@")
        if (clean.length < 3) return false
        return dao.getUserByUsername(clean) == null
    }

    suspend fun generateUsernameSuggestions(base: String): List<String> {
        val clean = base.trim().removePrefix("@").lowercase()
        val candidates = listOf(
            "${clean}_lg",
            "${clean}official",
            "${clean}${System.currentTimeMillis() % 1000}",
            "lg_${clean}"
        )
        val available = mutableListOf<String>()
        for (c in candidates) {
            if (dao.getUserByUsername(c) == null) {
                available.add(c)
            }
        }
        return available.take(3)
    }

    private suspend fun findAvailableUsername(base: String): String {
        var clean = base.trim().removePrefix("@").filter { it.isLetterOrDigit() || it == '_' }
        if (clean.length < 3) clean = "lguser"
        if (dao.getUserByUsername(clean) == null) return clean
        var counter = 1
        while (dao.getUserByUsername("${clean}$counter") != null) {
            counter++
        }
        return "${clean}$counter"
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            updateUserPresence(user.id, isOnline = false)
        }
        auth?.signOut()
        _currentUser.value = null
    }

    suspend fun updateProfile(updated: User) {
        dao.updateUser(updated)
        if (_currentUser.value?.id == updated.id) {
            _currentUser.value = updated
        }
    }

    suspend fun deleteCurrentAccount(): Result<Boolean> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        dao.deleteUser(user.id)
        _currentUser.value = null
        return Result.success(true)
    }

    fun getUserById(id: String): Flow<User?> = dao.getUserById(id)
    fun getAllUsers(): Flow<List<User>> = dao.getAllUsers()
    fun searchUsers(query: String): Flow<List<User>> = dao.searchUsers(query)

    // --- Feed & Videos ---
    fun getPublicVideos(): Flow<List<Video>> = dao.getPublicVideos()
    fun getFollowingFeed(userId: String): Flow<List<Video>> = dao.getFollowingFeed(userId)
    fun getVideosByCreator(creatorId: String): Flow<List<Video>> = dao.getVideosByCreator(creatorId)
    fun getVideosByCategory(category: String): Flow<List<Video>> = dao.getVideosByCategory(category)
    fun getVideosByHashtag(hashtag: String): Flow<List<Video>> = dao.getVideosByHashtag(hashtag)
    fun filterVideos(category: String, query: String): Flow<List<Video>> = dao.filterVideos(category, query)
    fun getVideoById(videoId: String): Flow<Video?> = dao.getVideoById(videoId)
    fun getAllVideosAdmin(): Flow<List<Video>> = dao.getAllVideosAdmin()
    fun searchVideos(query: String): Flow<List<Video>> = dao.searchVideos(query)

    suspend fun publishVideo(video: Video) {
        dao.insertVideo(video)
        // update user videosCount
        val user = dao.getUserByIdOnce(video.creatorId)
        if (user != null) {
            val updated = user.copy(videosCount = user.videosCount + 1)
            dao.updateUser(updated)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = updated
            }
        }
    }

    suspend fun deleteVideo(videoId: String, creatorId: String) {
        dao.deleteVideo(videoId)
        val user = dao.getUserByIdOnce(creatorId)
        if (user != null) {
            val updated = user.copy(videosCount = (user.videosCount - 1).coerceAtLeast(0))
            dao.updateUser(updated)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = updated
            }
        }
    }

    // --- Anti-Spam View Tracking ---
    suspend fun registerView(videoId: String, userId: String?) {
        val key = "${videoId}_${userId ?: "guest"}"
        if (!viewedVideosSession.contains(key)) {
            viewedVideosSession.add(key)
            dao.incrementViews(videoId)
        }
    }

    // --- Likes ---
    fun isVideoLiked(videoId: String, userId: String): Flow<VideoLike?> = dao.isVideoLiked(videoId, userId)
    fun getLikedVideos(userId: String): Flow<List<Video>> = dao.getLikedVideos(userId)

    suspend fun toggleLike(videoId: String, user: User, isCurrentlyLiked: Boolean) {
        if (isCurrentlyLiked) {
            dao.deleteLike(videoId, user.id)
            dao.updateLikesCount(videoId, -1)
        } else {
            dao.insertLike(VideoLike(videoId = videoId, userId = user.id))
            dao.updateLikesCount(videoId, 1)

            // Send notification to creator
            val video = dao.getVideoByIdOnce(videoId)
            if (video != null && video.creatorId != user.id) {
                dao.insertNotification(
                    NotificationItem(
                        id = UUID.randomUUID().toString(),
                        userId = video.creatorId,
                        actorId = user.id,
                        actorUsername = user.username,
                        actorAvatar = user.avatarUrl,
                        type = "LIKE",
                        title = "Liked your video",
                        message = "@${user.username} liked \"${video.title}\"",
                        targetId = videoId
                    )
                )
            }
        }
    }

    // --- Saves / Bookmarks ---
    fun isVideoSaved(videoId: String, userId: String): Flow<SavedVideo?> = dao.isVideoSaved(videoId, userId)
    fun getSavedVideos(userId: String): Flow<List<Video>> = dao.getSavedVideos(userId)

    suspend fun toggleSave(videoId: String, userId: String, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            dao.deleteSave(videoId, userId)
            dao.updateSavesCount(videoId, -1)
        } else {
            dao.insertSave(SavedVideo(videoId = videoId, userId = userId))
            dao.updateSavesCount(videoId, 1)
        }
    }

    suspend fun shareVideo(videoId: String) {
        dao.incrementShares(videoId)
    }

    // --- Follows ---
    fun isFollowing(followerId: String, followingId: String): Flow<Follow?> =
        dao.isFollowing(followerId, followingId)

    suspend fun toggleFollow(follower: User, targetUserId: String, isCurrentlyFollowing: Boolean) {
        if (isCurrentlyFollowing) {
            dao.deleteFollow(follower.id, targetUserId)
            dao.updateFollowingCount(follower.id, -1)
            dao.updateFollowersCount(targetUserId, -1)
        } else {
            dao.insertFollow(Follow(followerId = follower.id, followingId = targetUserId))
            dao.updateFollowingCount(follower.id, 1)
            dao.updateFollowersCount(targetUserId, 1)

            // Send notification
            dao.insertNotification(
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = targetUserId,
                    actorId = follower.id,
                    actorUsername = follower.username,
                    actorAvatar = follower.avatarUrl,
                    type = "FOLLOW",
                    title = "New follower",
                    message = "@${follower.username} started following you.",
                    targetId = follower.id
                )
            )
        }
        // Refresh current user state
        val updatedCurrent = dao.getUserByIdOnce(follower.id)
        if (updatedCurrent != null) {
            _currentUser.value = updatedCurrent
        }
    }

    // --- Comments ---
    fun getCommentsForVideo(videoId: String): Flow<List<Comment>> = dao.getCommentsForVideo(videoId)

    suspend fun addComment(
        videoId: String,
        user: User,
        text: String,
        parentCommentId: String? = null
    ) {
        val comment = Comment(
            commentId = UUID.randomUUID().toString(),
            videoId = videoId,
            userId = user.id,
            username = user.username,
            userAvatar = user.avatarUrl,
            text = text.trim(),
            parentCommentId = parentCommentId
        )
        dao.insertComment(comment)
        dao.updateCommentsCount(videoId, 1)

        val video = dao.getVideoByIdOnce(videoId)
        if (video != null && video.creatorId != user.id) {
            dao.insertNotification(
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    userId = video.creatorId,
                    actorId = user.id,
                    actorUsername = user.username,
                    actorAvatar = user.avatarUrl,
                    type = "COMMENT",
                    title = "New comment",
                    message = "@${user.username} commented: \"${text.take(40)}\"",
                    targetId = videoId
                )
            )
        }
    }

    suspend fun deleteComment(commentId: String, videoId: String) {
        dao.deleteComment(commentId)
        dao.updateCommentsCount(videoId, -1)
    }

    fun getAllCommentsAdmin(): Flow<List<Comment>> = dao.getAllComments()

    suspend fun deleteCommentAdmin(commentId: String) {
        dao.deleteComment(commentId)
    }

    suspend fun togglePinVideo(videoId: String, isPinned: Boolean) {
        dao.setPinnedStatus(videoId, isPinned)
    }

    suspend fun likeComment(commentId: String) {
        dao.likeComment(commentId)
    }

    // --- Notifications ---
    fun getNotifications(userId: String): Flow<List<NotificationItem>> = dao.getNotifications(userId)
    suspend fun markAllNotificationsAsRead(userId: String) = dao.markAllNotificationsAsRead(userId)

    // --- Chat Messages ---
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> =
        dao.getMessagesForConversation(conversationId)

    fun getAllMessagesForUser(userId: String): Flow<List<ChatMessage>> =
        dao.getAllMessagesForUser(userId)

    suspend fun sendMessage(
        sender: User,
        receiverId: String,
        text: String,
        sharedVideoId: String? = null
    ) {
        // Create deterministic conversation ID (sorted user ids)
        val convId = listOf(sender.id, receiverId).sorted().joinToString("_")
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = convId,
            senderId = sender.id,
            receiverId = receiverId,
            senderUsername = sender.username,
            text = text.trim(),
            sharedVideoId = sharedVideoId
        )
        dao.insertMessage(msg)

        dao.insertNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                userId = receiverId,
                actorId = sender.id,
                actorUsername = sender.username,
                actorAvatar = sender.avatarUrl,
                type = "MESSAGE",
                title = "New message",
                message = "@${sender.username}: ${text.take(35)}",
                targetId = convId
            )
        )
    }

    // --- Reports & Moderation ---
    fun getAllReports(): Flow<List<Report>> = dao.getAllReports()

    suspend fun reportVideo(videoId: String, user: User, videoTitle: String, reason: String) {
        dao.insertReport(
            Report(
                id = UUID.randomUUID().toString(),
                videoId = videoId,
                reportedByUserId = user.id,
                reportedByUsername = user.username,
                videoTitle = videoTitle,
                reason = reason
            )
        )
    }

    suspend fun resolveReport(reportId: String, status: String) {
        dao.updateReportStatus(reportId, status)
    }

    suspend fun updateVideoModeration(videoId: String, status: String) {
        dao.setModerationStatus(videoId, status)
    }

    // --- Admin Operations ---
    suspend fun setBannedStatus(userId: String, isBanned: Boolean) {
        dao.setBannedStatus(userId, isBanned)
    }

    suspend fun sendBroadcastAnnouncement(title: String, message: String) {
        val users = dao.getUserByIdOnce("mg_official")
        dao.insertNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                userId = "ALL",
                actorId = "mg_official",
                actorUsername = "MG Official",
                type = "SYSTEM",
                title = title,
                message = message,
                targetId = "system"
            )
        )
    }

    // --- Drafts ---
    fun getDrafts(creatorId: String): Flow<List<Draft>> = dao.getDrafts(creatorId)
    suspend fun saveDraft(draft: Draft) = dao.insertDraft(draft)
    suspend fun deleteDraft(draftId: String) = dao.deleteDraft(draftId)

    // --- Search History ---
    fun getSearchHistory(): Flow<List<SearchHistory>> = dao.getSearchHistory()
    suspend fun recordSearch(query: String) {
        if (query.isNotBlank()) {
            dao.insertSearch(SearchHistory(query = query.trim()))
        }
    }
    suspend fun clearSearchHistory() = dao.clearSearchHistory()

    // --- Seed Initial Data ---
    private suspend fun seedInitialDataIfNeeded() {
        val existingOfficial = dao.getUserByIdOnce("lg_official")
        if (existingOfficial == null) {
            val adminUser = User(
                id = "admin_root",
                username = "lgadmin",
                displayName = "LG Platform Admin",
                email = "admin@lg.app",
                bio = "Official LG Trust, Safety & Operations Control Center.",
                followersCount = 42800,
                followingCount = 12,
                videosCount = 4,
                likesCount = 280000,
                isVerified = true,
                isOnline = true,
                role = "ADMIN"
            )
            val officialUser = User(
                id = "lg_official",
                username = "lgofficial",
                displayName = "LG International",
                email = "hello@lg.app",
                bio = "Welcome to the next generation liquid glass social video platform. Create. Discover. Connect. 🌐",
                followersCount = 1250000,
                followingCount = 38,
                videosCount = 6,
                likesCount = 8900000,
                isVerified = true,
                isOnline = true,
                role = "ADMIN"
            )
            val techCreator = User(
                id = "creator_alex",
                username = "alextech",
                displayName = "Alex Rivera",
                email = "alex@tech.lg",
                bio = "Exploring future tech, robotics, AI & cyberpunk aesthetics ⚡️ Silicon Valley.",
                followersCount = 485000,
                followingCount = 180,
                videosCount = 3,
                likesCount = 1450000,
                isVerified = true,
                isOnline = true,
                role = "USER"
            )
            val musicCreator = User(
                id = "creator_elena",
                username = "elenabeats",
                displayName = "Elena Vance",
                email = "elena@beats.lg",
                bio = "Electronic music producer & synthwave composer. Dropping weekly soundscapes 🎧",
                followersCount = 620000,
                followingCount = 210,
                videosCount = 3,
                likesCount = 2300000,
                isVerified = true,
                isOnline = false,
                lastActiveAt = System.currentTimeMillis() - 12 * 60 * 1000,
                role = "USER"
            )
            val creativeCreator = User(
                id = "creator_kai",
                username = "kaicreative",
                displayName = "Kai Tanaka",
                email = "kai@motion.lg",
                bio = "Cinematic 3D artist, VFX director & Tokyo night photography enthusiast 🗼",
                followersCount = 340000,
                followingCount = 95,
                videosCount = 2,
                likesCount = 980000,
                isVerified = true,
                isOnline = true,
                role = "USER"
            )

            dao.insertUser(adminUser)
            dao.insertUser(officialUser)
            dao.insertUser(techCreator)
            dao.insertUser(musicCreator)
            dao.insertUser(creativeCreator)

            // Seed Videos
            val v1 = Video(
                videoId = "v_launch_01",
                creatorId = officialUser.id,
                creatorUsername = officialUser.username,
                creatorDisplayName = officialUser.displayName,
                title = "Welcome to LG — Create. Discover. Connect. 🚀",
                description = "The next-generation liquid glass short-form video platform is officially live. Experience seamless playback, liquid UI, and a creator-first community.",
                hashtags = "#LG #Create #Discover #Connect #LiquidGlass #FutureOfVideo #NextGen",
                audioTitle = "LG Anthem - Liquid Pulse",
                audioArtist = "LG Sound Studio",
                views = 482900,
                likes = 124300,
                comments = 4580,
                shares = 18400,
                saves = 39200,
                durationSeconds = 20,
                gradientStart = 0xFF00E5FF,
                gradientEnd = 0xFF7C3AED,
                category = "Technology",
                isPinned = true
            )

            val v2 = Video(
                videoId = "v_tech_02",
                creatorId = techCreator.id,
                creatorUsername = techCreator.username,
                creatorDisplayName = techCreator.displayName,
                title = "Quantum Computing on a Mobile Device?! Real or Myth? 💻⚡️",
                description = "Breaking down the latest quantum processor breakthroughs. Are we close to pocket quantum chips? Let me know in the comments!",
                hashtags = "#tech #quantum #future #gadgets #silicon #engineering",
                audioTitle = "Cyber Runner 2099",
                audioArtist = "SynthWave Tokyo",
                views = 194300,
                likes = 48200,
                comments = 1420,
                shares = 6800,
                saves = 15300,
                durationSeconds = 35,
                gradientStart = 0xFF0F172A,
                gradientEnd = 0xFF0284C7,
                category = "Technology"
            )

            val v3 = Video(
                videoId = "v_music_03",
                creatorId = musicCreator.id,
                creatorUsername = musicCreator.username,
                creatorDisplayName = musicCreator.displayName,
                title = "Built a track entirely with modular analog synthesizers 🎹🔥",
                description = "Late night in the studio patching cables into the modular rack. Turn up the bass and wear headphones for the sub frequencies!",
                hashtags = "#music #synth #producer #beats #modular #electronic",
                audioTitle = "Neon Horizon (Original Mix)",
                audioArtist = "Elena Vance",
                views = 312000,
                likes = 89400,
                comments = 3210,
                shares = 12900,
                saves = 28100,
                durationSeconds = 45,
                gradientStart = 0xFF701A75,
                gradientEnd = 0xFF4338CA,
                category = "Music"
            )

            val v4 = Video(
                videoId = "v_art_04",
                creatorId = creativeCreator.id,
                creatorUsername = creativeCreator.username,
                creatorDisplayName = creativeCreator.displayName,
                title = "Rainy Shibuya at 2 AM — Cinematic Color Grading breakdown 🌧️✨",
                description = "Shot on anamorphic glass, graded in DaVinci. Neon reflections and cyber atmosphere. Which shot is your favorite?",
                hashtags = "#tokyo #cinematography #vfx #colorgrading #aesthetic #nightphotography",
                audioTitle = "Midnight Raindrops",
                audioArtist = "Lo-Fi Collective",
                views = 278500,
                likes = 76100,
                comments = 1890,
                shares = 8400,
                saves = 22400,
                durationSeconds = 28,
                gradientStart = 0xFF042F2E,
                gradientEnd = 0xFF1E1B4B,
                category = "Art"
            )

            val v5 = Video(
                videoId = "v_sports_05",
                creatorId = techCreator.id,
                creatorUsername = techCreator.username,
                creatorDisplayName = techCreator.displayName,
                title = "First-Person FPV Drone Chase over Swiss Alps 🏔️💨",
                description = "90mph diving down vertical cliffs with our custom 6S quadcopter. Do not blink!",
                hashtags = "#fpv #drone #adventure #speed #extreme #alps",
                audioTitle = "Adrenaline Rush",
                audioArtist = "Sonic Drive",
                views = 421000,
                likes = 112000,
                comments = 2900,
                shares = 15900,
                saves = 34500,
                durationSeconds = 24,
                gradientStart = 0xFF1E293B,
                gradientEnd = 0xFF059669,
                category = "Sports"
            )

            dao.insertVideo(v1)
            dao.insertVideo(v2)
            dao.insertVideo(v3)
            dao.insertVideo(v4)
            dao.insertVideo(v5)

            // Seed Comments for v1
            dao.insertComment(
                Comment(
                    commentId = "c1",
                    videoId = v1.videoId,
                    userId = techCreator.id,
                    username = techCreator.username,
                    text = "The UI and performance on MG is unreal! Super smooth.",
                    likesCount = 420
                )
            )
            dao.insertComment(
                Comment(
                    commentId = "c2",
                    videoId = v1.videoId,
                    userId = musicCreator.id,
                    username = musicCreator.username,
                    text = "Sound fidelity here is next level. Excited to share my beats! 🎶",
                    likesCount = 280
                )
            )
            dao.insertComment(
                Comment(
                    commentId = "c3",
                    videoId = v1.videoId,
                    userId = creativeCreator.id,
                    username = creativeCreator.username,
                    text = "Proud to be an early creator on MG. Let's build together! 🚀",
                    likesCount = 195,
                    parentCommentId = "c1"
                )
            )

            // Seed initial notifications
            dao.insertNotification(
                NotificationItem(
                    id = "notif_welcome",
                    userId = officialUser.id,
                    actorId = "system",
                    actorUsername = "MG System",
                    type = "SYSTEM",
                    title = "Welcome to MG Platform",
                    message = "Your platform is ready for millions of creators worldwide."
                )
            )
        }
    }
}
