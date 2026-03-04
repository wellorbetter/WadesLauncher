package com.wades.launcher.feature.home

import androidx.lifecycle.viewModelScope
import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.GroupType
import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.domain.model.SearchQuery
import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.repository.LayoutRepository
import com.wades.launcher.core.domain.repository.PreferencesRepository
import com.wades.launcher.core.domain.usecase.ClassifyAppUseCase
import com.wades.launcher.core.domain.usecase.ManageGroupUseCase
import com.wades.launcher.core.domain.usecase.SearchAppsUseCase
import com.wades.launcher.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

private const val MAX_FREQUENT = 8
private const val MAX_RECENT = 4
private const val MAX_NEW = 4
private const val NEW_APP_WINDOW_MS = 7L * 24 * 60 * 60 * 1000
private const val SELF_PACKAGE = "com.wades.launcher"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val searchApps: SearchAppsUseCase,
    private val layoutRepository: LayoutRepository,
    private val manageGroup: ManageGroupUseCase,
    private val classifyApp: ClassifyAppUseCase,
    private val preferencesRepository: PreferencesRepository,
) : MviViewModel<HomeIntent, HomeState, HomeSideEffect>(HomeState()) {

    init {
        dispatch(HomeIntent.LoadApps)
        loadStackGroups()
    }

    override suspend fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadApps -> observeApps()
            is HomeIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is HomeIntent.AppClicked -> onAppClicked(intent)
            is HomeIntent.AppLongClicked -> updateState { copy(longPressedApp = intent.app) }
            is HomeIntent.DismissAppMenu -> updateState { copy(longPressedApp = null) }
            is HomeIntent.CreateFolder -> createFolder(intent)
            is HomeIntent.OpenFolder -> openFolder(intent.folderId)
            is HomeIntent.CloseFolder -> updateState { copy(openFolder = null) }
            is HomeIntent.AddToFolder -> addToFolder(intent)
            is HomeIntent.RemoveFromFolder -> removeFromFolder(intent)
            is HomeIntent.RenameFolder -> renameFolder(intent)
            is HomeIntent.DeleteFolder -> deleteFolder(intent.folderId)
            is HomeIntent.EnterEditMode -> updateState { copy(isEditMode = true) }
            is HomeIntent.ExitEditMode -> updateState { copy(isEditMode = false) }
            is HomeIntent.DeleteCategory -> deleteCategory(intent.categoryId)
            is HomeIntent.RenameCategory -> renameCategory(intent)
            is HomeIntent.ReorderCategories -> reorderCategories(intent.categoryIds)
            is HomeIntent.AddCategory -> addCategory(intent.name)
            is HomeIntent.OpenSettings -> emitSideEffect(HomeSideEffect.OpenSettings)
            is HomeIntent.ReorderSections -> reorderSections(intent.fromIndex, intent.toIndex)
            is HomeIntent.MergeSections -> mergeSections(intent.draggedKey, intent.targetKey)
            is HomeIntent.UnstackSection -> unstackSection(intent.stackIndex, intent.sectionKey)
            is HomeIntent.ExpandStack -> updateState { copy(expandedStackIndex = intent.index) }
            is HomeIntent.CollapseStack -> updateState { copy(expandedStackIndex = null) }
            is HomeIntent.ReorderInStack -> reorderInStack(intent.stackIndex, intent.fromPos, intent.toPos)
        }
    }

    private fun observeApps() {
        viewModelScope.launch {
            // Must await category init before observing, otherwise combine emits empty categories
            classifyApp.initCategoryGroups()

            combine(
                appRepository.observeAllApps(),
                layoutRepository.observeLayout(),
            ) { apps, layout ->
                val visible = apps.filter { !it.isHidden }
                val categoryGroups = layout.groups.filter { it.type == GroupType.CATEGORY }
                val sections = buildSections(visible, categoryGroups)
                val folders = layout.groups.filter { it.type == GroupType.USER }
                Triple(sections, folders, visible)
            }.collectLatest { (sections, folders, _) ->
                val hasRealUsage = sections.frequent.any { it.usageCount > 0 }
                updateState {
                    copy(
                        frequentApps = sections.frequent,
                        recentApps = sections.recent,
                        newApps = sections.newlyInstalled,
                        categoryApps = sections.categories,
                        allApps = sections.all,
                        folders = folders,
                        isLoading = false,
                        isColdStart = !hasRealUsage,
                    )
                }
            }
        }
    }

    private fun buildSections(apps: List<AppInfo>, categoryGroups: List<LauncherGroup>): HomeSections {
        val now = System.currentTimeMillis()
        val filtered = apps.filter { it.packageName != SELF_PACKAGE }

        val frequent = filtered
            .filter { it.usageCount > 0 }
            .sortedByDescending { it.usageCount }
            .take(MAX_FREQUENT)
        val frequentPkgs = frequent.map { it.packageName }.toSet()

        // Build categories from DB groups (always, not just cold-start)
        val categories = categoryGroups
            .sortedBy { it.sortOrder }
         .mapNotNull { group ->
                val matched = filtered.filter { it.packageName in group.appPackageNames }
                if (matched.isNotEmpty()) {
                    AppCategory(
                        id = group.id,
                        name = group.name,
                        apps = matched,
                        sortOrder = group.sortOrder,
                    )
                } else {
                    null
                }
            }
        val categoryPkgs = categories.flatMap { it.apps.map { a -> a.packageName } }.toSet()

        // Recently used (by timestamp, not count)
        val usedPkgs = frequentPkgs + categoryPkgs
        val recent = filtered
            .filter { it.lastUsedTimestamp > 0L && it.packageName !in usedPkgs }
            .sortedByDescending { it.lastUsedTimestamp }
            .take(MAX_RECENT)
        val recentPkgs = recent.map { it.packageName }.toSet()

        val newlyInstalled = filtered
            .filter {
                it.installedTimestamp > 0L &&
                    (now - it.installedTimestamp) < NEW_APP_WINDOW_MS &&
                    it.packageName !in usedPkgs &&
                    it.packageName !in recentPkgs
            }
            .sortedByDescending { it.installedTimestamp }
            .take(MAX_NEW)
        val topPkgs = usedPkgs + recentPkgs + newlyInstalled.map { it.packageName }.toSet()

        val all = filtered
            .filter { it.packageName !in topPkgs }
            .sortedBy { it.sortKey.ifEmpty { it.label.lowercase() } }

        return HomeSections(frequent, recent, newlyInstalled, categories, all)
    }

    private suspend fun onSearchQueryChanged(query: String) {
        updateState { copy(searchQuery = query, isSearchActive = query.isNotBlank()) }
        if (query.isBlank()) {
            updateState { copy(searchResults = emptyList()) }
            return
        }
        val results = searchApps(SearchQuery(keyword = query))
        updateState { copy(searchResults = results.map { it.app }) }
    }

    private fun onAppClicked(intent: HomeIntent.AppClicked) {
        viewModelScope.launch {
            appRepository.updateUsageCount(intent.app.packageName)
        }
        emitSideEffect(
            HomeSideEffect.LaunchApp(
                packageName = intent.app.packageName,
                componentName = intent.app.componentName,
            ),
        )
    }

    private suspend fun createFolder(intent: HomeIntent.CreateFolder) {
        val currentFolders = state.value.folders
        val sortOrder = if (currentFolders.isEmpty()) 0 else currentFolders.maxOf { it.sortOrder } + 1
        val group = manageGroup.createGroup(intent.name, sortOrder)
        manageGroup.addAppToGroup(group.id, intent.initialApp.packageName)
        updateState { copy(longPressedApp = null) }
    }

    private fun openFolder(folderId: String) {
        val folder = state.value.folders.find { it.id == folderId }
        updateState { copy(openFolder = folder) }
    }

    private suspend fun addToFolder(intent: HomeIntent.AddToFolder) {
        manageGroup.addAppToGroup(intent.folderId, intent.app.packageName)
        updateState { copy(longPressedApp = null) }
    }

    private suspend fun removeFromFolder(intent: HomeIntent.RemoveFromFolder) {
        manageGroup.removeAppFromGroup(intent.folderId, intent.packageName)
    }

    private suspend fun renameFolder(intent: HomeIntent.RenameFolder) {
        manageGroup.renameGroup(intent.folderId, intent.name)
    }

    private suspend fun deleteFolder(folderId: String) {
        manageGroup.deleteGroup(folderId)
        updateState { copy(openFolder = null) }
    }

    // ── Edit mode operations ──

    private suspend fun deleteCategory(categoryId: String) {
        manageGroup.deleteGroup(categoryId)
    }

    private suspend fun renameCategory(intent: HomeIntent.RenameCategory) {
        manageGroup.renameGroup(intent.categoryId, intent.name)
    }

    private suspend fun reorderCategories(categoryIds: List<String>) {
        manageGroup.reorderGroups(categoryIds)
    }

    private suspend fun addCategory(name: String) {
        val currentCategories = state.value.categoryApps
        val sortOrder = if (currentCategories.isEmpty()) 100 else currentCategories.maxOf { it.sortOrder } + 1
        manageGroup.createGroup(name, sortOrder).let { group ->
            // Save as CATEGORY type
            layoutRepository.updateGroup(
                LauncherGroup(
                    id = group.id,
                    name = group.name,
                    sortOrder = group.sortOrder,
                    type = GroupType.CATEGORY,
                ),
            )
        }
    }

    // ── Stack operations ──

    private fun loadStackGroups() {
        viewModelScope.launch {
            val json = preferencesRepository.observeStackGroups().first()
            val groups = parseStackGroupsJson(json)
            updateState { copy(stackGroups = groups) }
        }
    }

    private fun persistStackGroups(groups: List<List<String>>) {
        viewModelScope.launch {
            val json = JSONArray(groups.map { JSONArray(it) }).toString()
            preferencesRepository.setStackGroups(json)
        }
    }

    private fun reorderSections(fromIndex: Int, toIndex: Int) {
        val current = state.value.sectionOrder.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        updateState { copy(sectionOrder = current) }
    }

    private fun mergeSections(draggedKey: String, targetKey: String) {
        if (draggedKey == targetKey) return
        val currentGroups = state.value.stackGroups.toMutableList()

        // Find existing groups containing these keys
        val draggedGroupIdx = currentGroups.indexOfFirst { draggedKey in it }
        val targetGroupIdx = currentGroups.indexOfFirst { targetKey in it }

        // Remove dragged key from its current group (if in one)
        if (draggedGroupIdx >= 0) {
            val updated = currentGroups[draggedGroupIdx].filter { it != draggedKey }
            if (updated.size < 2) {
                currentGroups.removeAt(draggedGroupIdx)
            } else {
                currentGroups[draggedGroupIdx] = updated
            }
        }

        // Re-find target group index (may have shifted)
        val newTargetIdx = currentGroups.indexOfFirst { targetKey in it }
        if (newTargetIdx >= 0) {
            // Add dragged to existing target group
            currentGroups[newTargetIdx] = currentGroups[newTargetIdx] + draggedKey
        } else {
            // Create new stack group with target + dragged
            currentGroups.add(listOf(targetKey, draggedKey))
        }

        updateState { copy(stackGroups = currentGroups) }
        persistStackGroups(currentGroups)
    }

    private fun unstackSection(stackIndex: Int, sectionKey: String) {
        val currentGroups = state.value.stackGroups.toMutableList()
        if (stackIndex !in currentGroups.indices) return

        val group = currentGroups[stackIndex].filter { it != sectionKey }
        if (group.size < 2) {
            currentGroups.removeAt(stackIndex)
        } else {
            currentGroups[stackIndex] = group
        }

        updateState { copy(stackGroups = currentGroups, expandedStackIndex = null) }
        persistStackGroups(currentGroups)
    }

    private fun reorderInStack(stackIndex: Int, fromPos: Int, toPos: Int) {
        val currentGroups = state.value.stackGroups.toMutableList()
        if (stackIndex !in currentGroups.indices) return
        val group = currentGroups[stackIndex].toMutableList()
        if (fromPos !in group.indices || toPos !in group.indices) return
        val item = group.removeAt(fromPos)
        group.add(toPos, item)
        currentGroups[stackIndex] = group
        updateState { copy(stackGroups = currentGroups) }
        persistStackGroups(currentGroups)
    }

    companion object {
        fun parseStackGroupsJson(json: String): List<List<String>> {
            if (json.isBlank() || json == "[]") return emptyList()
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val inner = arr.getJSONArray(i)
                    (0 until inner.length()).map { j -> inner.getString(j) }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

private data class HomeSections(
    val frequent: List<AppInfo>,
    val recent: List<AppInfo>,
    val newlyInstalled: List<AppInfo>,
    val categories: List<AppCategory>,
    val all: List<AppInfo>,
)
