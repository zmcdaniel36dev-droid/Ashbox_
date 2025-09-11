package com.nullform.ashbox.ui.di

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
    fun provideOpenAIUtil(@ApplicationContext context: Context): OpenAIUtil {
        return OpenAIUtil(context)
    }
}