package com.nullform.ashbox.di

import com.nullform.ashbox.ui.aiutils.OllamaUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}