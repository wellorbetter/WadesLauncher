package com.wades.launcher.core.domain.model

data class BuiltInWidget(
    val id: String,
    val type: BuiltInWidgetType,
    val sortOrder: Int,
    val isVisible: Boolean = true,
    val config: Map<String, String> = emptyMap(),
)

enum class BuiltInWidgetType {
    CLOCK,
    CALENDAR,
    TASK_LIST,
    WEATHER,
}
