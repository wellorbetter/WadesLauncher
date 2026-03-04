package com.wades.launcher.core.domain.model

data class AppInfo(
    val packageName: String,
    val label: String,
    val componentName: String,
    val sortKey: String = "",
    val installedTimestamp: Long = 0L,
    val shortcuts: List<AppShortcut> = emptyList(),
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val isHidden: Boolean = false,
)
