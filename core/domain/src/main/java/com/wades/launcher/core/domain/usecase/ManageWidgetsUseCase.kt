package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.WidgetInfo
import com.wades.launcher.core.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow

class ManageWidgetsUseCase(
    private val widgetRepository: WidgetRepository,
) {
    fun observeWidgets(): Flow<List<WidgetInfo>> = widgetRepository.observeWidgets()

    suspend fun addWidget(widget: WidgetInfo) {
        widgetRepository.addWidget(widget)
    }

    suspend fun removeWidget(widgetId: Int) {
        widgetRepository.removeWidget(widgetId)
    }

    suspend fun updateWidget(widget: WidgetInfo) {
        widgetRepository.updateWidget(widget)
    }
}
