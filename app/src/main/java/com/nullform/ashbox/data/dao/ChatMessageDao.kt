package com.nullform.ashbox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nullform.ashbox.data.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM ChatMessage ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM ChatMessage WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): ChatMessage?

    @Query("DELETE FROM chat_message")
    suspend fun deleteAllMessages()
}
