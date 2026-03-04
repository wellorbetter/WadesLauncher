package com.wades.launcher.core.data.repository

import com.wades.launcher.core.data.local.WidgetDao
import com.wades.launcher.core.data.local.WidgetEntity
import com.wades.launcher.core.domain.model.WidgetInfo
import com.wades.launcher.core.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    private val widgetDao: WidgetDao,
) : WidgetRepository {

    override fun observeWidgets(): Flow<List<WidgetInfo>> {
        return widgetDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addWidget(widget: WidgetInfo) {
        widgetDao.insert(widget.toEntity())
    }

    override suspend fun removeWidget(widgetId: Int) {
        widgetDao.deleteById(widgetId)
    }

    override suspend fun updateWidget(widget: WidgetInfo) {
        widgetDao.update(widget.toEntity())
    }

    private fun WidgetEntity.toDomain() = WidgetInfo(
        id = id,
        appWidgetId = appWidgetId,
        packageName = packageName,
        label = label,
        spanX = spanX,
        spanY = spanY,
        sortOrder = sortOrder,
    )

    private fun WidgetInfo.toEntity() = WidgetEntity(
        id = id,
        appWidgetId = appWidgetId,
        packageName = packageName,
        label = label,
        spanX = spanX,
        spanY = spanY,
        sortOrder = sortOrder,
    )
}
