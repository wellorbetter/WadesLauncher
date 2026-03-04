package com.wades.launcher.feature.drawer

import androidx.lifecycle.viewModelScope
import com.wades.launcher.core.domain.model.SearchQuery
import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.usecase.SearchAppsUseCase
import com.wades.launcher.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val searchApps: SearchAppsUseCase,
) : MviViewModel<DrawerIntent, DrawerState, DrawerSideEffect>(DrawerState()) {

    init {
        dispatch(DrawerIntent.LoadApps)
    }

    override suspend fun handleIntent(intent: DrawerIntent) {
        when (intent) {
            is DrawerIntent.LoadApps -> observeApps()
            is DrawerIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is DrawerIntent.AppClicked -> onAppClicked(intent)
            is DrawerIntent.ToggleViewMode -> toggleViewMode()
        }
    }

    private fun observeApps() {
        viewModelScope.launch {
            appRepository.observeAllApps().collectLatest { apps ->
                val sorted = apps.sortedBy { it.label.lowercase() }
                updateState {
                    copy(allApps = sorted, filteredApps = sorted, isLoading = false)
                }
            }
        }
    }

    private suspend fun onSearchQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
        if (query.isBlank()) {
            updateState { copy(filteredApps = allApps) }
            return
        }
        val results = searchApps(SearchQuery(keyword = query))
        updateState { copy(filteredApps = results.map { it.app }) }
    }

    private fun onAppClicked(intent: DrawerIntent.AppClicked) {
        viewModelScope.launch {
            appRepository.updateUsageCount(intent.app.packageName)
        }
        emitSideEffect(
            DrawerSideEffect.LaunchApp(
                packageName = intent.app.packageName,
                componentName = intent.app.componentName,
            )
        )
    }

    private suspend fun toggleViewMode() {
        updateState {
            copy(
                viewMode = when (viewMode) {
                    DrawerViewMode.LIST -> DrawerViewMode.GRID
                    DrawerViewMode.GRID -> DrawerViewMode.LIST
                },
            )
        }
    }
}
