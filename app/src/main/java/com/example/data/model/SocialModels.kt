package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey
    val commentId: String,
    val videoId: String,
    val userId: String,
    val username: String,
    val userAvatar: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val parentCommentId: String? = null
)

@Entity(tableName = "video_likes", primaryKeys = ["videoId", "userId"])
data class VideoLike(
    val videoId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_videos", primaryKeys = ["videoId", "userId"])
data class SavedVideo(
    val videoId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows", primaryKeys = ["followerId", "followingId"])
data class Follow(
    val followerId: String,
    val followingId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey
    val id: String,
    val userId: String,
    val actorId: String,
    val actorUsername: String,
    val actorAvatar: String = "",
    val type: String, // LIKE, COMMENT, FOLLOW, MENTION, SYSTEM
    val title: String,
    val message: String,
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val senderUsername: String,
    val text: String,
    val sharedVideoId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey
    val id: String,
    val videoId: String,
    val reportedByUserId: String,
    val reportedByUsername: String,
    val videoTitle: String,
    val reason: String,
    val status: String = "PENDING", // PENDING, RESOLVED, DISMISSED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    val hashtags: String,
    val videoUri: String,
    val filterName: String = "Normal",
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val overlayText: String = "",
    val audience: String = "PUBLIC",
    val commentsEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
