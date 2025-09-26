package com.nullform.ashbox.data.repository

import com.nullform.ashbox.data.dao.ChatMessageDao
import com.nullform.ashbox.data.dao.ChatSessionDao
import com.nullform.ashbox.data.database.ChatSession
import com.nullform.ashbox.data.entity.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val chatSessionDao: ChatSessionDao
) {

    // Get all chat sessions, ordered by the most recent message
    fun getAllSessions(): Flow<List<ChatSession>> = chatSessionDao.getAllSessions()

    // Get all messages for a specific chat session
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForSession(sessionId)
    }

    // Insert a new message and update the session's timestamp
    suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
        // Update the last message timestamp for the session
        val session = chatSessionDao.getSessionById(message.sessionId)
        session?.let {
            chatSessionDao.insertSession(it.copy(lastMessageTimestamp = System.currentTimeMillis()))
        }
    }

    // Create a new chat session
    suspend fun createNewSession(title: String): String {
        val newSession = ChatSession(title = title)
        chatSessionDao.insertSession(newSession)
        return newSession.id
    }

    // Get a session by its ID
    suspend fun getSessionById(sessionId: String): ChatSession? {
        return chatSessionDao.getSessionById(sessionId)
    }

    // Delete a chat session and all its messages
    suspend fun deleteSession(sessionId: String) {
        chatSessionDao.deleteSession(sessionId)
        chatMessageDao.deleteMessagesForSession(sessionId)
    }
}
