package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.Comment
import com.example.data.model.Draft
import com.example.data.model.NotificationItem
import com.example.data.model.Report
import com.example.data.model.SearchHistory
import com.example.data.model.User
import com.example.data.model.Video
import com.example.data.repository.MgRepository
import com.example.data.repository.UnverifiedEmailException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class VerificationPendingState(
    val email: String,
    val username: String = "",
    val displayName: String = "",
    val firebaseUser: FirebaseUser? = null
)

sealed class AppDestination {
    object Splash : AppDestination()
    object Onboarding : AppDestination()
    object Auth : AppDestination()
    object Main : AppDestination()
    object Search : AppDestination()
    object CreateStudio : AppDestination()
    object CreatorStudio : AppDestination()
    object AdminDashboard : AppDestination()
    data class UserProfile(val userId: String) : AppDestination()
    data class ChatDetail(val otherUserId: String, val otherUsername: String) : AppDestination()
    object Settings : AppDestination()
    object LandingPage : AppDestination()
    data class Legal(val docType: String) : AppDestination() // "terms", "privacy", "guidelines", "copyright", "about"
}

enum class MainTab {
    HOME, EXPLORE, CREATE, INBOX, PROFILE
}

class MgViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MgRepository
    init {
        val db = AppDatabase.getDatabase(application)
        repository = MgRepository(application, db.mgDao())
    }

    // --- Navigation Destination & Current Tab ---
    private val _currentDestination = MutableStateFlow<AppDestination>(AppDestination.Splash)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
    }

    fun setTab(tab: MainTab) {
        if (tab == MainTab.CREATE) {
            _currentDestination.value = AppDestination.CreateStudio
        } else {
            _currentTab.value = tab
            _currentDestination.value = AppDestination.Main
        }
    }

    // --- Theme & Language Preferences ---
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    fun setDarkMode(enabled: Boolean) { _isDarkMode.value = enabled }

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    fun setLanguage(lang: String) { _selectedLanguage.value = lang }

    // --- Auth State ---
    private val _verificationPending = MutableStateFlow<VerificationPendingState?>(null)
    val verificationPending: StateFlow<VerificationPendingState?> = _verificationPending.asStateFlow()

    val currentUser: StateFlow<User?> = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val allUsers: StateFlow<List<User>> = repository.getAllUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Real-Time Presence Tracking ---
    val presenceMap: StateFlow<Map<String, com.example.data.model.UserPresence>> = repository.presenceMap.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun getUserPresence(userId: String): com.example.data.model.UserPresence {
        return repository.getUserPresence(userId)
    }

    fun updateUserPresence(isOnline: Boolean) {
        currentUser.value?.let { user ->
            repository.updateUserPresence(user.id, isOnline)
        }
    }

    fun recordHeartbeat() {
        currentUser.value?.let { user ->
            repository.recordUserHeartbeat(user.id)
        }
    }

    // --- Video Feeds ---
    val publicFeed: StateFlow<List<Video>> = repository.getPublicVideos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val followingFeed: StateFlow<List<Video>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getFollowingFeed(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Feed Tab in Home: "FOR_YOU" vs "FOLLOWING"
    private val _homeFeedType = MutableStateFlow("FOR_YOU")
    val homeFeedType: StateFlow<String> = _homeFeedType.asStateFlow()
    fun setHomeFeedType(type: String) { _homeFeedType.value = type }

    // --- User Saved & Liked Videos ---
    val likedVideos: StateFlow<List<Video>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getLikedVideos(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedVideos: StateFlow<List<Video>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getSavedVideos(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userDrafts: StateFlow<List<Draft>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getDrafts(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Notifications ---
    val notifications: StateFlow<List<NotificationItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotifications(user.id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Search & Explore ---
    val predefinedCategories = listOf(
        "All", "Technology", "Music", "Art", "Sports", "Gaming",
        "Comedy", "Education", "Travel", "Fashion", "Food", "Fitness",
        "Dance", "Science", "Film & Animation", "Beauty"
    )

    val popularHashtagSuggestions = listOf(
        "#LG", "#LiquidGlass", "#FutureOfVideo", "#TechTok", "#ModularSynth",
        "#CyberpunkTokyo", "#FPVDrone", "#Cinematography", "#VFX", "#Viral", "#Creator"
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedHashtag = MutableStateFlow<String?>(null)
    val selectedHashtag: StateFlow<String?> = _selectedHashtag.asStateFlow()

    fun selectHashtag(tag: String?) {
        _selectedHashtag.value = tag
        if (tag != null) {
            _searchQuery.value = tag
        }
    }

    // Dynamic hashtag index extracted across all videos with video frequencies
    val indexedHashtags: StateFlow<List<Pair<String, Int>>> = publicFeed.map { videos ->
        val map = mutableMapOf<String, Int>()
        videos.forEach { v ->
            v.hashtags.split(" ", ",", "\n")
                .map { it.trim() }
                .filter { it.startsWith("#") && it.length > 1 }
                .forEach { tag ->
                    val clean = tag.lowercase()
                    map[clean] = (map[clean] ?: 0) + 1
                }
        }
        // Merge with defaults
        listOf("#lg", "#liquidglass", "#futureofvideo", "#techtok", "#modularsynth", "#cyberpunktokyo", "#fpvdrone").forEach { def ->
            if (!map.containsKey(def)) map[def] = 1
        }
        map.toList().sortedByDescending { it.second }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchResults: StateFlow<List<Video>> = combine(_searchQuery, _selectedCategory, _selectedHashtag) { query, cat, tag ->
        Triple(query, cat, tag)
    }.flatMapLatest { (q, cat, tag) ->
        val effectiveQuery = when {
            tag != null -> tag
            q.isNotBlank() -> q
            else -> ""
        }
        if (effectiveQuery.isBlank() && cat == "All") {
            repository.getPublicVideos()
        } else if (cat != "All" && effectiveQuery.isNotBlank()) {
            repository.filterVideos(cat, effectiveQuery)
        } else if (cat != "All") {
            repository.getVideosByCategory(cat)
        } else {
            repository.searchVideos(effectiveQuery)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchHistory: StateFlow<List<SearchHistory>> = repository.getSearchHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun submitSearch(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            repository.recordSearch(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // --- Admin Dashboard State ---
    val allVideosAdmin: StateFlow<List<Video>> = repository.getAllVideosAdmin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reportsList: StateFlow<List<Report>> = repository.getAllReports().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCommentsAdmin: StateFlow<List<Comment>> = repository.getAllCommentsAdmin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteCommentAdmin(commentId: String) {
        viewModelScope.launch { repository.deleteCommentAdmin(commentId) }
    }

    fun togglePinVideo(videoId: String, isPinned: Boolean) {
        viewModelScope.launch { repository.togglePinVideo(videoId, isPinned) }
    }

    fun deleteVideo(videoId: String, creatorId: String = "") {
        viewModelScope.launch { repository.deleteVideo(videoId, creatorId) }
    }

    // --- Auth Actions ---
    fun onFirebaseUserAuthenticated(firebaseUser: com.google.firebase.auth.FirebaseUser, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithFirebaseUser(firebaseUser)
            if (result.isSuccess) {
                _currentDestination.value = AppDestination.Main
            }
            onResult(result)
        }
    }

    fun loginWithGoogle(email: String, displayName: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithGoogle(email, displayName)
            if (result.isSuccess) {
                _currentDestination.value = AppDestination.Main
            }
            onResult(result)
        }
    }

    fun loginWithEmail(email: String, pass: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginWithEmail(email, pass)
            if (result.isSuccess) {
                _verificationPending.value = null
                _currentDestination.value = AppDestination.Main
            } else {
                val ex = result.exceptionOrNull()
                if (ex is UnverifiedEmailException) {
                    _verificationPending.value = VerificationPendingState(
                        email = email.trim(),
                        firebaseUser = ex.firebaseUser
                    )
                }
            }
            onResult(result)
        }
    }

    fun registerWithEmail(email: String, pass: String, username: String, name: String, onResult: (Result<FirebaseUser>) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerWithEmail(email, pass, username, name)
            if (result.isSuccess) {
                _verificationPending.value = VerificationPendingState(
                    email = email.trim(),
                    username = username.trim().removePrefix("@"),
                    displayName = name.trim(),
                    firebaseUser = result.getOrNull()
                )
            }
            onResult(result)
        }
    }

    fun checkEmailVerification(onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val pending = _verificationPending.value
            val result = repository.checkEmailVerificationStatus(
                targetUser = pending?.firebaseUser,
                username = pending?.username ?: "",
                displayName = pending?.displayName ?: ""
            )
            if (result.isSuccess) {
                _verificationPending.value = null
                _currentDestination.value = AppDestination.Main
            }
            onResult(result)
        }
    }

    fun resendVerificationEmail(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val pending = _verificationPending.value
            val result = repository.resendVerificationEmail(pending?.firebaseUser)
            onResult(result)
        }
    }

    fun clearVerificationPending() {
        _verificationPending.value = null
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            onResult(result)
        }
    }

    suspend fun checkUsernameAvailability(username: String): Boolean {
        return repository.isUsernameAvailable(username)
    }

    suspend fun suggestUsernames(base: String): List<String> {
        return repository.generateUsernameSuggestions(base)
    }

    fun logout() {
        repository.logout()
        _currentDestination.value = AppDestination.Auth
    }

    fun updateProfile(user: User) {
        viewModelScope.launch { repository.updateProfile(user) }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteCurrentAccount()
            if (res.isSuccess) {
                _currentDestination.value = AppDestination.Auth
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    // --- Video Interaction Actions ---
    fun registerVideoView(videoId: String) {
        viewModelScope.launch {
            repository.registerView(videoId, currentUser.value?.id)
        }
    }

    fun toggleLike(videoId: String, isCurrentlyLiked: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleLike(videoId, user, isCurrentlyLiked)
        }
    }

    fun toggleSave(videoId: String, isCurrentlySaved: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleSave(videoId, user.id, isCurrentlySaved)
        }
    }

    fun toggleFollow(targetUserId: String, isCurrentlyFollowing: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleFollow(user, targetUserId, isCurrentlyFollowing)
        }
    }

    fun shareVideo(videoId: String) {
        viewModelScope.launch {
            repository.shareVideo(videoId)
        }
    }

    fun getCommentsForVideo(videoId: String) = repository.getCommentsForVideo(videoId)

    fun addComment(videoId: String, text: String, parentId: String?) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addComment(videoId, user, text, parentId)
        }
    }

    fun deleteComment(commentId: String, videoId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId, videoId)
        }
    }

    fun likeComment(commentId: String) {
        viewModelScope.launch {
            repository.likeComment(commentId)
        }
    }

    fun reportVideo(videoId: String, videoTitle: String, reason: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.reportVideo(videoId, user, videoTitle, reason)
        }
    }

    // --- Video Publishing & Drafts ---
    fun publishVideo(
        title: String,
        description: String,
        hashtags: String,
        audioTitle: String,
        category: String,
        visibility: String,
        commentsEnabled: Boolean,
        downloadsEnabled: Boolean,
        remixEnabled: Boolean,
        gradientStart: Long,
        gradientEnd: Long,
        onComplete: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val newVideo = Video(
                videoId = "v_${UUID.randomUUID().toString().take(8)}",
                creatorId = user.id,
                creatorUsername = user.username,
                creatorDisplayName = user.displayName,
                creatorAvatar = user.avatarUrl,
                title = title.trim(),
                description = description.trim(),
                hashtags = hashtags.trim(),
                audioTitle = audioTitle.ifBlank { "Original Audio - @${user.username}" },
                audioArtist = user.displayName,
                visibility = visibility,
                commentsEnabled = commentsEnabled,
                downloadsEnabled = downloadsEnabled,
                remixEnabled = remixEnabled,
                category = category,
                gradientStart = gradientStart,
                gradientEnd = gradientEnd,
                views = 1,
                likes = 1
            )
            repository.publishVideo(newVideo)
            onComplete()
        }
    }

    fun saveDraft(draft: Draft, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveDraft(draft)
            onComplete()
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            repository.deleteDraft(draftId)
        }
    }

    // --- Messaging ---
    fun getMessagesForConversation(convId: String) = repository.getMessagesForConversation(convId)
    fun getAllMessagesForUser() = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllMessagesForUser(user.id) else flowOf(emptyList())
    }

    fun sendChatMessage(receiverId: String, text: String, sharedVideoId: String? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.sendMessage(user, receiverId, text, sharedVideoId)
        }
    }

    fun markNotificationsAsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch { repository.markAllNotificationsAsRead(user.id) }
    }

    // --- Admin Dashboard Actions ---
    fun setBannedStatus(userId: String, isBanned: Boolean) {
        viewModelScope.launch { repository.setBannedStatus(userId, isBanned) }
    }

    fun setVideoModeration(videoId: String, status: String) {
        viewModelScope.launch { repository.updateVideoModeration(videoId, status) }
    }

    fun resolveReport(reportId: String, status: String) {
        viewModelScope.launch { repository.resolveReport(reportId, status) }
    }

    fun broadcastAnnouncement(title: String, message: String) {
        viewModelScope.launch { repository.sendBroadcastAnnouncement(title, message) }
    }

    fun getUserById(userId: String) = repository.getUserById(userId)
    fun getVideosByCreator(creatorId: String) = repository.getVideosByCreator(creatorId)
}
