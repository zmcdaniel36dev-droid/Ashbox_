package com.nullform.ashbox.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nullform.ashbox.data.entity.ChatMessage
import com.nullform.ashbox.data.dao.ChatMessageDao

@Database(entities = [ChatMessage::class, ChatSession::class], version = 1, exportSchema = false)
@TypeConverters(AppTypeConverters::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
}
