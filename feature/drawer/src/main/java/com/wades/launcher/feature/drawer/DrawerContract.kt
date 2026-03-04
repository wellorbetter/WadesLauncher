package com.wades.launcher.feature.drawer

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.ui.mvi.MviIntent
import com.wades.launcher.core.ui.mvi.MviSideEffect
import com.wades.launcher.core.ui.mvi.MviState

enum class DrawerViewMode { LIST, GRID }

sealed interface DrawerIntent : MviIntent {
    data object LoadApps : DrawerIntent
    data class SearchQueryChanged(val query: String) : DrawerIntent
    data class AppClicked(val app: AppInfo) : DrawerIntent
    data object ToggleViewMode : DrawerIntent
}

data class DrawerState(
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val viewMode: DrawerViewMode = DrawerViewMode.LIST,
) : MviState

sealed interface DrawerSideEffect : MviSideEffect {
    data class LaunchApp(val packageName: String, val componentName: String) : DrawerSideEffect
}
