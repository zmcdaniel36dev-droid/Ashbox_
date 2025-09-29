package com.nullform.ashbox.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nullform.ashbox.data.dao.ChatMessageDao
import com.nullform.ashbox.data.entity.ChatMessage
import com.nullform.ashbox.data.entity.ChatSession
import com.nullform.ashbox.data.database.TypeConverters as AppTypeConverters
import com.nullform.ashbox.data.dao.ChatSessionDao

@Database(entities = [ChatMessage::class, ChatSession::class], version = 1, exportSchema = false)
@TypeConverters(AppTypeConverters::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatSessionDao(): ChatSessionDao
}
