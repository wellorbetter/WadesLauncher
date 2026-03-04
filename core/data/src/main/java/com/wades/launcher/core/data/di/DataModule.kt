package com.wades.launcher.core.data.di

import android.content.Context
import androidx.room.Room
import com.wades.launcher.core.data.local.AppUsageDao
import com.wades.launcher.core.data.local.BuiltInWidgetDao
import com.wades.launcher.core.data.local.GroupDao
import com.wades.launcher.core.data.local.LauncherDatabase
import com.wades.launcher.core.data.local.TaskDao
import com.wades.launcher.core.data.local.WidgetDao
import com.wades.launcher.core.data.preferences.UserPreferencesRepositoryImpl
import com.wades.launcher.core.data.repository.AppRepositoryImpl
import com.wades.launcher.core.data.repository.BuiltInWidgetRepositoryImpl
import com.wades.launcher.core.data.repository.LayoutRepositoryImpl
import com.wades.launcher.core.data.repository.SearchRepositoryImpl
import com.wades.launcher.core.data.repository.TaskRepositoryImpl
import com.wades.launcher.core.data.repository.WeatherRepositoryImpl
import com.wades.launcher.core.data.repository.WidgetRepositoryImpl
import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.repository.BuiltInWidgetRepository
import com.wades.launcher.core.domain.repository.LayoutRepository
import com.wades.launcher.core.domain.repository.PreferencesRepository
import com.wades.launcher.core.domain.repository.SearchRepository
import com.wades.launcher.core.domain.repository.TaskRepository
import com.wades.launcher.core.domain.repository.WeatherRepository
import com.wades.launcher.core.domain.repository.WidgetRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindLayoutRepository(impl: LayoutRepositoryImpl): LayoutRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindWidgetRepository(impl: WidgetRepositoryImpl): WidgetRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindBuiltInWidgetRepository(impl: BuiltInWidgetRepositoryImpl): BuiltInWidgetRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: UserPreferencesRepositoryImpl): PreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): LauncherDatabase {
            return Room.databaseBuilder(
                context,
                LauncherDatabase::class.java,
                "wades_launcher.db",
            ).fallbackToDestructiveMigration()
            .build()
        }

        @Provides
        fun provideGroupDao(db: LauncherDatabase): GroupDao = db.groupDao()

        @Provides
        fun provideWidgetDao(db: LauncherDatabase): WidgetDao = db.widgetDao()

        @Provides
        fun provideAppUsageDao(db: LauncherDatabase): AppUsageDao = db.appUsageDao()

        @Provides
        fun provideBuiltInWidgetDao(db: LauncherDatabase): BuiltInWidgetDao = db.builtInWidgetDao()

        @Provides
        fun provideTaskDao(db: LauncherDatabase): TaskDao = db.taskDao()
    }
}
