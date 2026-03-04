package com.wades.launcher.di

import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.repository.BuiltInWidgetRepository
import com.wades.launcher.core.domain.repository.LayoutRepository
import com.wades.launcher.core.domain.repository.SearchRepository
import com.wades.launcher.core.domain.repository.TaskRepository
import com.wades.launcher.core.domain.repository.WeatherRepository
import com.wades.launcher.core.domain.repository.WidgetRepository
import com.wades.launcher.core.domain.usecase.ClassifyAppUseCase
import com.wades.launcher.core.domain.usecase.GetWeatherUseCase
import com.wades.launcher.core.domain.usecase.ManageBuiltInWidgetsUseCase
import com.wades.launcher.core.domain.usecase.ManageGroupUseCase
import com.wades.launcher.core.domain.usecase.ManageTasksUseCase
import com.wades.launcher.core.domain.usecase.ManageWidgetsUseCase
import com.wades.launcher.core.domain.usecase.SearchAppsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideSearchAppsUseCase(
        searchRepository: SearchRepository,
    ): SearchAppsUseCase = SearchAppsUseCase(searchRepository)

    @Provides
    fun provideManageWidgetsUseCase(
        widgetRepository: WidgetRepository,
    ): ManageWidgetsUseCase = ManageWidgetsUseCase(widgetRepository)

    @Provides
    fun provideManageGroupUseCase(
        layoutRepository: LayoutRepository,
    ): ManageGroupUseCase = ManageGroupUseCase(layoutRepository)

    @Provides
    fun provideManageTasksUseCase(
        taskRepository: TaskRepository,
    ): ManageTasksUseCase = ManageTasksUseCase(taskRepository)

    @Provides
    fun provideManageBuiltInWidgetsUseCase(
        builtInWidgetRepository: BuiltInWidgetRepository,
    ): ManageBuiltInWidgetsUseCase = ManageBuiltInWidgetsUseCase(builtInWidgetRepository)

    @Provides
    fun provideGetWeatherUseCase(
        weatherRepository: WeatherRepository,
    ): GetWeatherUseCase = GetWeatherUseCase(weatherRepository)

    @Provides
    fun provideClassifyAppUseCase(
        layoutRepository: LayoutRepository,
        appRepository: AppRepository,
    ): ClassifyAppUseCase = ClassifyAppUseCase(layoutRepository, appRepository)
}
