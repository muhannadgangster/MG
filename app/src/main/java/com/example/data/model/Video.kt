package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey
    val videoId: String,
    val creatorId: String,
    val creatorUsername: String,
    val creatorDisplayName: String,
    val creatorAvatar: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val title: String,
    val description: String,
    val hashtags: String = "", // comma or space separated, e.g. "#mg #trending #future"
    val audioTitle: String = "Original Sound - MG Audio",
    val audioArtist: String = "MG Studio",
    val views: Long = 0,
    val likes: Long = 0,
    val comments: Long = 0,
    val shares: Long = 0,
    val saves: Long = 0,
    val visibility: String = "PUBLIC", // PUBLIC, FOLLOWERS, PRIVATE
    val commentsEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true,
    val remixEnabled: Boolean = true,
    val status: String = "published", // published, draft
    val moderationStatus: String = "APPROVED", // APPROVED, PENDING, REJECTED, RESTRICTED
    val durationSeconds: Int = 15,
    val gradientStart: Long = 0xFF0D1B2A,
    val gradientEnd: Long = 0xFF1B263B,
    val category: String = "Technology",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
