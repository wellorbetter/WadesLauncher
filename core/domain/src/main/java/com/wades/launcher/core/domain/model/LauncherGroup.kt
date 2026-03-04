package com.wades.launcher.core.domain.model

data class LauncherGroup(
    val id: String,
    val name: String,
    val sortOrder: Int,
    val appPackageNames: List<String> = emptyList(),
    val type: GroupType = GroupType.USER,
    val isExpanded: Boolean = true,
)

enum class GroupType {
    USER,
    RECENT,
    FREQUENT,
    SYSTEM,
    CATEGORY,
}
