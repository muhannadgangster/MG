package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val videosCount: Int = 0,
    val likesCount: Int = 0,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val allowComments: Boolean = true,
    val allowMessages: Boolean = true,
    val allowDownloads: Boolean = true,
    val role: String = "USER", // "USER" or "ADMIN"
    val isBanned: Boolean = false,
    val isOnline: Boolean = true,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
