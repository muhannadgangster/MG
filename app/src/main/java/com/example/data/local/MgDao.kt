package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface MgDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdOnce(id: String): User?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username)")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email)")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users ORDER BY followersCount DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE LOWER(username) LIKE '%' || LOWER(:query) || '%' OR LOWER(displayName) LIKE '%' || LOWER(:query) || '%'")
    fun searchUsers(query: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("UPDATE users SET isBanned = :isBanned WHERE id = :id")
    suspend fun setBannedStatus(id: String, isBanned: Boolean)

    @Query("UPDATE users SET isOnline = :isOnline, lastActiveAt = :lastActiveAt WHERE id = :id")
    suspend fun updateUserPresence(id: String, isOnline: Boolean, lastActiveAt: Long)

    // --- Videos ---
    @Query("SELECT * FROM videos WHERE moderationStatus = 'APPROVED' AND visibility = 'PUBLIC' ORDER BY createdAt DESC")
    fun getPublicVideos(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE creatorId = :creatorId ORDER BY isPinned DESC, createdAt DESC")
    fun getVideosByCreator(creatorId: String): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE category = :category AND moderationStatus = 'APPROVED' ORDER BY views DESC")
    fun getVideosByCategory(category: String): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE LOWER(hashtags) LIKE '%' || LOWER(:hashtag) || '%' AND moderationStatus = 'APPROVED' ORDER BY views DESC")
    fun getVideosByHashtag(hashtag: String): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE (:category = 'All' OR category = :category) AND (LOWER(title) LIKE '%' || LOWER(:query) || '%' OR LOWER(description) LIKE '%' || LOWER(:query) || '%' OR LOWER(hashtags) LIKE '%' || LOWER(:query) || '%') AND moderationStatus = 'APPROVED' ORDER BY views DESC")
    fun filterVideos(category: String, query: String): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    fun getVideoById(videoId: String): Flow<Video?>

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    suspend fun getVideoByIdOnce(videoId: String): Video?

    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun getAllVideosAdmin(): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video)

    @Update
    suspend fun updateVideo(video: Video)

    @Query("DELETE FROM videos WHERE videoId = :videoId")
    suspend fun deleteVideo(videoId: String)

    @Query("UPDATE videos SET views = views + 1 WHERE videoId = :videoId")
    suspend fun incrementViews(videoId: String)

    @Query("UPDATE videos SET likes = MAX(0, likes + :delta) WHERE videoId = :videoId")
    suspend fun updateLikesCount(videoId: String, delta: Long)

    @Query("UPDATE videos SET comments = MAX(0, comments + :delta) WHERE videoId = :videoId")
    suspend fun updateCommentsCount(videoId: String, delta: Long)

    @Query("UPDATE videos SET shares = shares + 1 WHERE videoId = :videoId")
    suspend fun incrementShares(videoId: String)

    @Query("UPDATE videos SET saves = MAX(0, saves + :delta) WHERE videoId = :videoId")
    suspend fun updateSavesCount(videoId: String, delta: Long)

    @Query("UPDATE videos SET isPinned = :isPinned WHERE videoId = :videoId")
    suspend fun setPinnedStatus(videoId: String, isPinned: Boolean)

    @Query("UPDATE videos SET moderationStatus = :status WHERE videoId = :videoId")
    suspend fun setModerationStatus(videoId: String, status: String)

    @Query("SELECT * FROM videos WHERE (LOWER(title) LIKE '%' || LOWER(:query) || '%' OR LOWER(description) LIKE '%' || LOWER(:query) || '%' OR LOWER(hashtags) LIKE '%' || LOWER(:query) || '%') AND moderationStatus = 'APPROVED'")
    fun searchVideos(query: String): Flow<List<Video>>

    // --- Likes ---
    @Query("SELECT * FROM video_likes WHERE videoId = :videoId AND userId = :userId")
    fun isVideoLiked(videoId: String, userId: String): Flow<VideoLike?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: VideoLike)

    @Query("DELETE FROM video_likes WHERE videoId = :videoId AND userId = :userId")
    suspend fun deleteLike(videoId: String, userId: String)

    @Query("SELECT v.* FROM videos v INNER JOIN video_likes l ON v.videoId = l.videoId WHERE l.userId = :userId ORDER BY l.timestamp DESC")
    fun getLikedVideos(userId: String): Flow<List<Video>>

    // --- Saved Videos ---
    @Query("SELECT * FROM saved_videos WHERE videoId = :videoId AND userId = :userId")
    fun isVideoSaved(videoId: String, userId: String): Flow<SavedVideo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSave(save: SavedVideo)

    @Query("DELETE FROM saved_videos WHERE videoId = :videoId AND userId = :userId")
    suspend fun deleteSave(videoId: String, userId: String)

    @Query("SELECT v.* FROM videos v INNER JOIN saved_videos s ON v.videoId = s.videoId WHERE s.userId = :userId ORDER BY s.timestamp DESC")
    fun getSavedVideos(userId: String): Flow<List<Video>>

    // --- Follows ---
    @Query("SELECT * FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    fun isFollowing(followerId: String, followingId: String): Flow<Follow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: Follow)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: String, followingId: String)

    @Query("SELECT v.* FROM videos v INNER JOIN follows f ON v.creatorId = f.followingId WHERE f.followerId = :followerId AND v.moderationStatus = 'APPROVED' ORDER BY v.createdAt DESC")
    fun getFollowingFeed(followerId: String): Flow<List<Video>>

    @Query("UPDATE users SET followingCount = MAX(0, followingCount + :delta) WHERE id = :userId")
    suspend fun updateFollowingCount(userId: String, delta: Int)

    @Query("UPDATE users SET followersCount = MAX(0, followersCount + :delta) WHERE id = :userId")
    suspend fun updateFollowersCount(userId: String, delta: Int)

    // --- Comments ---
    @Query("SELECT * FROM comments ORDER BY timestamp DESC")
    fun getAllComments(): Flow<List<Comment>>

    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp ASC")
    fun getCommentsForVideo(videoId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    suspend fun deleteComment(commentId: String)

    @Query("UPDATE comments SET likesCount = likesCount + 1 WHERE commentId = :commentId")
    suspend fun likeComment(commentId: String)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesForUser(userId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    // --- Drafts ---
    @Query("SELECT * FROM drafts WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getDrafts(creatorId: String): Flow<List<Draft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: Draft)

    @Query("DELETE FROM drafts WHERE id = :draftId")
    suspend fun deleteDraft(draftId: String)

    // --- Search History ---
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 15")
    fun getSearchHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
