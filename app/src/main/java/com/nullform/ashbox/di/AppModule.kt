package com.nullform.ashbox.di

import android.content.Context
import com.nullform.ashbox.ui.aiutils.OpenAIUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOpenAIUtil(): OpenAIUtil {
        return OpenAIUtil
    }
}