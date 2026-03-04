package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.BuiltInWidget
import kotlinx.coroutines.flow.Flow

interface BuiltInWidgetRepository {
    fun observeAll(): Flow<List<BuiltInWidget>>
    suspend fun save(widget: BuiltInWidget)
    suspend fun update(widget: BuiltInWidget)
    suspend fun initDefaults()
}
