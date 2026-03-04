package com.wades.launcher.core.domain.model

data class LauncherLayout(
    val groups: List<LauncherGroup> = emptyList(),
    val config: LayoutConfig = LayoutConfig(),
)

data class LayoutConfig(
    val columnsPerRow: Int = 4,
    val searchBarPosition: SearchBarPosition = SearchBarPosition.BOTTOM,
    val showGroupHeaders: Boolean = true,
)

enum class SearchBarPosition {
    TOP,
    BOTTOM,
    HIDDEN,
}
