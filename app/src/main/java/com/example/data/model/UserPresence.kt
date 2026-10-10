package com.example.data.model

data class UserPresence(
    val userId: String,
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val customStatus: String? = null
) {
    val statusDisplay: String
        get() {
            if (isOnline) return "Active now"
            val diffMs = System.currentTimeMillis() - lastSeenTimestamp
            val diffMins = diffMs / (60 * 1000)
            return when {
                diffMins < 1 -> "Active just now"
                diffMins < 60 -> "Active ${diffMins}m ago"
                diffMins < 1440 -> "Active ${diffMins / 60}h ago"
                else -> "Offline"
            }
        }
}
