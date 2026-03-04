package com.wades.launcher.core.ai.di

import com.wades.launcher.core.ai.parser.CommandParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideCommandParser(): CommandParser = CommandParser()
}
