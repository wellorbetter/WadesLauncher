package com.wades.launcher.feature.widget

import com.wades.launcher.core.domain.model.BuiltInWidget
import com.wades.launcher.core.domain.model.Task
import com.wades.launcher.core.domain.model.WeatherInfo
import com.wades.launcher.core.domain.model.WidgetInfo
import com.wades.launcher.core.ui.mvi.MviIntent
import com.wades.launcher.core.ui.mvi.MviSideEffect
import com.wades.launcher.core.ui.mvi.MviState

sealed interface WidgetIntent : MviIntent {
    data object LoadWidgets : WidgetIntent
    data class SaveWidget(
        val appWidgetId: Int,
        val packageName: String,
        val label: String,
    ) : WidgetIntent
    data class RemoveWidget(val dbId: Int, val appWidgetId: Int) : WidgetIntent

    // Built-in widget intents
    data object LoadBuiltInWidgets : WidgetIntent
    data class AddTask(val text: String) : WidgetIntent
    data class ToggleTask(val taskId: String) : WidgetIntent
    data class DeleteTask(val taskId: String) : WidgetIntent
    data object RefreshWeather : WidgetIntent
}

data class WidgetState(
    val widgets: List<WidgetInfo> = emptyList(),
    val builtInWidgets: List<BuiltInWidget> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = true,
) : MviState

sealed interface WidgetSideEffect : MviSideEffect {
    data class ShowToast(val message: String) : WidgetSideEffect
    data class DeallocateWidget(val appWidgetId: Int) : WidgetSideEffect
}
