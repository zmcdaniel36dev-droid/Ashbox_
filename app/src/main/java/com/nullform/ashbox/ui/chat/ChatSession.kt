package com.nullform.ashbox.data.entity // Or your preferred package

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID // For generating unique IDs

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // Unique ID for the chat session
    val title: String? = null, // Optional: User might name chats later
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
)
