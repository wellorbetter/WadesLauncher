package com.wades.launcher.feature.home

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.ui.mvi.MviIntent
import com.wades.launcher.core.ui.mvi.MviSideEffect
import com.wades.launcher.core.ui.mvi.MviState

sealed interface HomeIntent : MviIntent {
    data object LoadApps : HomeIntent
    data class SearchQueryChanged(val query: String) : HomeIntent
    data class AppClicked(val app: AppInfo) : HomeIntent
    data class AppLongClicked(val app: AppInfo) : HomeIntent

    // Folder intents
    data class CreateFolder(val name: String, val initialApp: AppInfo) : HomeIntent
    data class OpenFolder(val folderId: String) : HomeIntent
    data object CloseFolder : HomeIntent
    data class AddToFolder(val folderId: String, val app: AppInfo) : HomeIntent
    data class RemoveFromFolder(val folderId: String, val packageName: String) : HomeIntent
    data class RenameFolder(val folderId: String, val name: String) : HomeIntent
    data class DeleteFolder(val folderId: String) : HomeIntent
    data object DismissAppMenu : HomeIntent

    // Edit mode intents
    data object EnterEditMode : HomeIntent
    data object ExitEditMode : HomeIntent
    data class DeleteCategory(val categoryId: String) : HomeIntent
    data class RenameCategory(val categoryId: String, val name: String) : HomeIntent
    data class ReorderCategories(val categoryIds: List<String>) : HomeIntent
    data class AddCategory(val name: String) : HomeIntent
    data object OpenSettings : HomeIntent

    // Stack intents
    data class ReorderSections(val fromIndex: Int, val toIndex: Int) : HomeIntent
    data class MergeSections(val draggedKey: String, val targetKey: String) : HomeIntent
    data class UnstackSection(val stackIndex: Int, val sectionKey: String) : HomeIntent
    data class ExpandStack(val index: Int) : HomeIntent
    data object CollapseStack : HomeIntent
    data class ReorderInStack(val stackIndex: Int, val fromPos: Int, val toPos: Int) : HomeIntent
}

data class HomeState(
    val frequentApps: List<AppInfo> = emptyList(),
    val recentApps: List<AppInfo> = emptyList(),
    val newApps: List<AppInfo> = emptyList(),
    val categoryApps: List<AppCategory> = emptyList(),
    val allApps: List<AppInfo> = emptyList(),
    val folders: List<LauncherGroup> = emptyList(),
    val searchResults: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = true,
    val isColdStart: Boolean = true,
    val isEditMode: Boolean = false,
    val openFolder: LauncherGroup? = null,
    val longPressedApp: AppInfo? = null,
    val sectionOrder: List<String> = emptyList(),
    val stackGroups: List<List<String>> = emptyList(),
    val expandedStackIndex: Int? = null,
) : MviState

data class AppCategory(
    val id: String,
    val name: String,
    val apps: List<AppInfo>,
    val sortOrder: Int = 0,
)

sealed interface HomeSideEffect : MviSideEffect {
    data class LaunchApp(val packageName: String, val componentName: String) : HomeSideEffect
    data class ShowToast(val message: String) : HomeSideEffect
    data object OpenSettings : HomeSideEffect
}
