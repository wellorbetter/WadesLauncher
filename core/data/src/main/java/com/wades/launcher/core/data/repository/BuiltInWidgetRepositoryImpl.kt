package com.wades.launcher.core.data.repository

import com.wades.launcher.core.data.local.BuiltInWidgetDao
import com.wades.launcher.core.data.local.BuiltInWidgetEntity
import com.wades.launcher.core.domain.model.BuiltInWidget
import com.wades.launcher.core.domain.model.BuiltInWidgetType
import com.wades.launcher.core.domain.repository.BuiltInWidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuiltInWidgetRepositoryImpl @Inject constructor(
    private val dao: BuiltInWidgetDao,
) : BuiltInWidgetRepository {

    override fun observeAll(): Flow<List<BuiltInWidget>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(widget: BuiltInWidget) {
        dao.insert(widget.toEntity())
    }

    override suspend fun update(widget: BuiltInWidget) {
        dao.update(widget.toEntity())
    }

    override suspend fun initDefaults() {
        val existing = dao.observeAll().first()
        if (existing.isNotEmpty()) return
        val defaults = listOf(
            BuiltInWidgetEntity("clock", BuiltInWidgetType.CLOCK.name, 0),
            BuiltInWidgetEntity("calendar", BuiltInWidgetType.CALENDAR.name, 1),
            BuiltInWidgetEntity("task_list", BuiltInWidgetType.TASK_LIST.name, 2),
            BuiltInWidgetEntity("weather", BuiltInWidgetType.WEATHER.name, 3),
        )
        defaults.forEach { dao.insert(it) }
    }

    private fun BuiltInWidgetEntity.toDomain(): BuiltInWidget {
        val configMap = try {
            val json = JSONObject(config)
            json.keys().asSequence().associateWith { json.getString(it) }
        } catch (_: Exception) {
            emptyMap()
        }
        return BuiltInWidget(
            id = id,
            type = BuiltInWidgetType.valueOf(type),
            sortOrder = sortOrder,
            isVisible = isVisible,
            config = configMap,
        )
    }

    private fun BuiltInWidget.toEntity(): BuiltInWidgetEntity {
        val json = JSONObject(config).toString()
        return BuiltInWidgetEntity(
            id = id, type = type.name, sortOrder = sortOrder,
            isVisible = isVisible, config = json,
        )
    }
}
