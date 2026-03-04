package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.WidgetInfo
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    fun observeWidgets(): Flow<List<WidgetInfo>>
    suspend fun addWidget(widget: WidgetInfo)
    suspend fun removeWidget(widgetId: Int)
    suspend fun updateWidget(widget: WidgetInfo)
}
