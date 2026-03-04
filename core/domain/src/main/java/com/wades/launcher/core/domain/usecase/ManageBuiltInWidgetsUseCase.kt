package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.BuiltInWidget
import com.wades.launcher.core.domain.repository.BuiltInWidgetRepository
import kotlinx.coroutines.flow.Flow

class ManageBuiltInWidgetsUseCase(
    private val repository: BuiltInWidgetRepository,
) {
    fun observeAll(): Flow<List<BuiltInWidget>> = repository.observeAll()

    suspend fun initDefaults() {
        repository.initDefaults()
    }

    suspend fun toggleVisibility(widget: BuiltInWidget) {
        repository.update(widget.copy(isVisible = !widget.isVisible))
    }

    suspend fun updateConfig(widget: BuiltInWidget, config: Map<String, String>) {
        repository.update(widget.copy(config = config))
    }
}
