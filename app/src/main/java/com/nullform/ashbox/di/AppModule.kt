package com.nullform.ashbox.di

import android.content.Context
import androidx.room.Room
import com.nullform.ashbox.data.database.ChatDatabase
import com.nullform.ashbox.data.dao.ChatMessageDao
import com.nullform.ashbox.ui.aiutils.OllamaUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @OptIn(markerClass = arrayOf(InternalSerializationApi::class))
    @Singleton
    @Provides
    fun provideOpenAIUtil(): OllamaUtil {
        return OllamaUtil
    }

    @Singleton
    @Provides
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideChatMessageDao(chatDatabase: ChatDatabase): ChatMessageDao {
        return chatDatabase.chatMessageDao()
    }
}