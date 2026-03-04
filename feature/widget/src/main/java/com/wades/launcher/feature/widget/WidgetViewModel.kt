package com.wades.launcher.feature.widget

import androidx.lifecycle.viewModelScope
import com.wades.launcher.core.domain.model.BuiltInWidgetType
import com.wades.launcher.core.domain.model.WidgetInfo
import com.wades.launcher.core.domain.usecase.GetWeatherUseCase
import com.wades.launcher.core.domain.usecase.ManageBuiltInWidgetsUseCase
import com.wades.launcher.core.domain.usecase.ManageTasksUseCase
import com.wades.launcher.core.domain.usecase.ManageWidgetsUseCase
import com.wades.launcher.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val manageWidgets: ManageWidgetsUseCase,
    private val manageBuiltInWidgets: ManageBuiltInWidgetsUseCase,
    private val manageTasks: ManageTasksUseCase,
    private val getWeather: GetWeatherUseCase,
) : MviViewModel<WidgetIntent, WidgetState, WidgetSideEffect>(WidgetState()) {

    init {
        dispatch(WidgetIntent.LoadWidgets)
        dispatch(WidgetIntent.LoadBuiltInWidgets)
    }

    override suspend fun handleIntent(intent: WidgetIntent) {
        when (intent) {
            is WidgetIntent.LoadWidgets -> observeWidgets()
            is WidgetIntent.SaveWidget -> saveWidget(intent)
            is WidgetIntent.RemoveWidget -> removeWidget(intent)
            is WidgetIntent.LoadBuiltInWidgets -> loadBuiltInWidgets()
            is WidgetIntent.AddTask -> addTask(intent.text)
            is WidgetIntent.ToggleTask -> toggleTask(intent.taskId)
            is WidgetIntent.DeleteTask -> deleteTask(intent.taskId)
            is WidgetIntent.RefreshWeather -> refreshWeather()
        }
    }

    private fun observeWidgets() {
        viewModelScope.launch {
            manageWidgets.observeWidgets().collectLatest { widgets ->
                updateState { copy(widgets = widgets, isLoading = false) }
            }
        }
    }

    private var weatherFetched = false

    private fun loadBuiltInWidgets() {
        viewModelScope.launch {
            manageBuiltInWidgets.initDefaults()

            // Load cached weather immediately
            val cached = getWeather.getCached()
            if (cached != null) {
                updateState { copy(weather = cached) }
            }

            // Observe built-in widgets + tasks
            combine(
                manageBuiltInWidgets.observeAll(),
                manageTasks.observeAll(),
            ) { builtIn, tasks ->
                Pair(builtIn, tasks)
            }.collectLatest { (builtIn, tasks) ->
                updateState { copy(builtInWidgets = builtIn, tasks = tasks) }

                // Fetch fresh weather once on first load
                if (!weatherFetched && builtIn.isNotEmpty()) {
                    weatherFetched = true
                    fetchWeatherAsync(builtIn)
                }
            }
        }
    }

    private fun fetchWeatherAsync(builtInWidgets: List<com.wades.launcher.core.domain.model.BuiltInWidget> = state.value.builtInWidgets) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val city = builtInWidgets
                    .find { it.type == BuiltInWidgetType.WEATHER }
                    ?.config?.get("city") ?: "Beijing"
                val weather = getWeather.fetch(city)
                updateState { copy(weather = weather) }
            } catch (e: Exception) {
                // Log for debugging, keep cached or null
                e.printStackTrace()
            }
        }
    }

    private suspend fun addTask(text: String) {
        manageTasks.add(text)
    }

    private suspend fun toggleTask(taskId: String) {
        val task = state.value.tasks.find { it.id == taskId } ?: return
        manageTasks.toggleComplete(taskId, task.isCompleted)
    }

    private suspend fun deleteTask(taskId: String) {
        manageTasks.delete(taskId)
    }

    private fun refreshWeather() {
        fetchWeatherAsync()
    }

    private suspend fun saveWidget(intent: WidgetIntent.SaveWidget) {
        val currentWidgets = state.value.widgets
        val nextOrder = if (currentWidgets.isEmpty()) 0 else currentWidgets.maxOf { it.sortOrder } + 1
        manageWidgets.addWidget(
            WidgetInfo(
                id = 0,
                appWidgetId = intent.appWidgetId,
                packageName = intent.packageName,
                label = intent.label,
                sortOrder = nextOrder,
            ),
        )
    }

    private suspend fun removeWidget(intent: WidgetIntent.RemoveWidget) {
        manageWidgets.removeWidget(intent.dbId)
        emitSideEffect(WidgetSideEffect.DeallocateWidget(intent.appWidgetId))
        emitSideEffect(WidgetSideEffect.ShowToast("小部件已移除"))
    }
}
