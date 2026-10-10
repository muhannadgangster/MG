package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        User::class,
        Video::class,
        Comment::class,
        VideoLike::class,
        SavedVideo::class,
        Follow::class,
        NotificationItem::class,
        ChatMessage::class,
        Report::class,
        Draft::class,
        SearchHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mgDao(): MgDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mg_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
