package com.nullform.ashbox.data

import com.nullform.ashbox.data.dao.ChatMessageDao
import com.nullform.ashbox.data.dao.ChatSessionDao
import com.nullform.ashbox.data.entity.ChatMessage
import com.nullform.ashbox.data.entity.ChatSession
import com.nullform.ashbox.data.entity.SenderType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatSessionDao: ChatSessionDao,
    private val chatMessageDao: ChatMessageDao
) {
    fun getAllSessions(): Flow<List<ChatSession>> = chatSessionDao.getAllSessions()

    suspend fun createNewSession(title: String): Long {
        val newSession = ChatSession(title = title)
        return chatSessionDao.insertSession(newSession)
    }

    suspend fun deleteSession(sessionId: Long) {
        chatSessionDao.deleteSession(sessionId)
        chatMessageDao.deleteMessagesForSession(sessionId)
    }

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>> { // Changed from String to Long
        return chatMessageDao.getMessagesForSession(sessionId)
    }

    suspend fun addMessageToSession(sessionId: Long, text: String, sender: SenderType) { // Changed from String to Long
        val message = ChatMessage(
            sessionId = sessionId,
            text = text,
            sender = sender
        )
        chatMessageDao.insertMessage(message)

        // Update the session's last message timestamp
        val session = chatSessionDao.getSessionById(sessionId) // No .toLong() needed now
        session?.let {
            chatSessionDao.insertSession(it.copy(lastMessageTimestamp = System.currentTimeMillis()))
        }
    }
}