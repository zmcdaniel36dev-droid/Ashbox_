package com.nullform.ashbox.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nullform.ashbox.data.database.ChatSession
import java.util.UUID

enum class SenderType {
    USER,
    AI
}

@Entity(
    tableName = "chat_messages",
    foreignKeys = [ForeignKey(
        entity = ChatSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE // If a chat session is deleted, its messages are also deleted
    )],
    indices = [Index(value = ["sessionId"])] // Index for faster queries by session ID
)
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String, // Foreign key linking to ChatSession
    var content: String,
    val sender: SenderType,
    val timestamp: Long = System.currentTimeMillis(),
    val isProcessing: Boolean = false // Useful for AI messages that are being generated
)
