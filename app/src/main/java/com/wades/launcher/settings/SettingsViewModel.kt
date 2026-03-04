package com.wades.launcher.settings

import androidx.lifecycle.viewModelScope
import com.wades.launcher.core.domain.model.AppLanguage
import com.wades.launcher.core.domain.repository.PreferencesRepository
import com.wades.launcher.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : MviViewModel<SettingsIntent, SettingsState, SettingsSideEffect>(SettingsState()) {

    init {
        dispatch(SettingsIntent.LoadPreferences)
    }

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadPreferences -> observePreferences()
            is SettingsIntent.SetLanguage -> setLanguage(intent.language)
            is SettingsIntent.SetThemeMode -> preferencesRepository.setThemeMode(intent.mode)
            is SettingsIntent.SetBlurIntensity -> preferencesRepository.setBlurIntensity(intent.intensity)
            is SettingsIntent.SetAnimationSpeed -> preferencesRepository.setAnimationSpeed(intent.speed)
            is SettingsIntent.SetDoubleTapAction -> preferencesRepository.setDoubleTapAction(intent.action)
            is SettingsIntent.SetSwipeUpAction -> preferencesRepository.setSwipeUpAction(intent.action)
            is SettingsIntent.SetLongPressAction -> preferencesRepository.setLongPressAction(intent.action)
            is SettingsIntent.SetDrawerViewMode -> preferencesRepository.setDrawerDefaultViewMode(intent.mode)
            is SettingsIntent.SetHiddenApps -> preferencesRepository.setHiddenApps(intent.packages)
            is SettingsIntent.SetIconSize -> preferencesRepository.setIconSize(intent.size)
            is SettingsIntent.SetGridColumns -> preferencesRepository.setGridColumns(intent.columns)
            is SettingsIntent.SetShowLabels -> preferencesRepository.setShowLabels(intent.show)
            is SettingsIntent.SetSearchBarPosition -> preferencesRepository.setSearchBarPosition(intent.position)
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.observePreferences().collectLatest { prefs ->
                updateState {
                    copy(
                        language = prefs.language,
                        themeMode = prefs.themeMode,
                        blurIntensity = prefs.blurIntensity,
                        animationSpeed = prefs.animationSpeed,
                        doubleTapAction = prefs.doubleTapAction,
                        swipeUpAction = prefs.swipeUpAction,
                        longPressAction = prefs.longPressAction,
                        drawerDefaultViewMode = prefs.drawerDefaultViewMode,
                        hiddenApps = prefs.hiddenApps,
                        iconSize = prefs.iconSize,
                        gridColumns = prefs.gridColumns,
                        showLabels = prefs.showLabels,
                        searchBarPosition = prefs.searchBarPosition,
                    )
                }
            }
        }
    }

    private suspend fun setLanguage(language: AppLanguage) {
        preferencesRepository.setLanguage(language)
        emitSideEffect(SettingsSideEffect.LanguageChanged(language))
    }
}
