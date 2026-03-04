package com.wades.launcher.feature.widget.builtin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wades.launcher.core.domain.model.BuiltInWidget
import com.wades.launcher.core.domain.model.BuiltInWidgetType
import com.wades.launcher.core.domain.model.Task
import com.wades.launcher.core.domain.model.WeatherInfo

@Composable
fun BuiltInWidgetSlot(
    widget: BuiltInWidget,
    tasks: List<Task>,
    weather: WeatherInfo?,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onRefreshWeather: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!widget.isVisible) return

    when (widget.type) {
        BuiltInWidgetType.CLOCK -> BuiltInClockWidget(modifier = modifier)
        BuiltInWidgetType.CALENDAR -> BuiltInCalendarWidget(modifier = modifier)
        BuiltInWidgetType.TASK_LIST -> TaskListWidget(
            tasks = tasks,
            onAdd = onAddTask,
            onToggle = onToggleTask,
            onDelete = onDeleteTask,
            modifier = modifier,
        )
        BuiltInWidgetType.WEATHER -> WeatherWidget(
            weather = weather,
            onRefresh = onRefreshWeather,
            modifier = modifier,
        )
    }
}
