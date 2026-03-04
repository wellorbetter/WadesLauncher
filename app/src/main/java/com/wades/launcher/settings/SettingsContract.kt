package com.wades.launcher.settings

import com.wades.launcher.core.domain.model.AnimationSpeed
import com.wades.launcher.core.domain.model.AppLanguage
import com.wades.launcher.core.domain.model.GestureAction
import com.wades.launcher.core.domain.model.IconSize
import com.wades.launcher.core.domain.model.SearchBarPosition
import com.wades.launcher.core.domain.model.ThemeMode
import com.wades.launcher.core.ui.mvi.MviIntent
import com.wades.launcher.core.ui.mvi.MviSideEffect
import com.wades.launcher.core.ui.mvi.MviState

sealed interface SettingsIntent : MviIntent {
    data object LoadPreferences : SettingsIntent
    data class SetLanguage(val language: AppLanguage) : SettingsIntent
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    data class SetBlurIntensity(val intensity: Float) : SettingsIntent
    data class SetAnimationSpeed(val speed: AnimationSpeed) : SettingsIntent
    data class SetDoubleTapAction(val action: GestureAction) : SettingsIntent
    data class SetSwipeUpAction(val action: GestureAction) : SettingsIntent
    data class SetLongPressAction(val action: GestureAction) : SettingsIntent
    data class SetDrawerViewMode(val mode: String) : SettingsIntent
    data class SetHiddenApps(val packages: Set<String>) : SettingsIntent
    data class SetIconSize(val size: IconSize) : SettingsIntent
    data class SetGridColumns(val columns: Int) : SettingsIntent
    data class SetShowLabels(val show: Boolean) : SettingsIntent
    data class SetSearchBarPosition(val position: SearchBarPosition) : SettingsIntent
}

data class SettingsState(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val blurIntensity: Float = 0.5f,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val doubleTapAction: GestureAction = GestureAction.LOCK_SCREEN,
    val swipeUpAction: GestureAction = GestureAction.OPEN_DRAWER,
    val longPressAction: GestureAction = GestureAction.NONE,
    val drawerDefaultViewMode: String = "LIST",
    val hiddenApps: Set<String> = emptySet(),
    val iconSize: IconSize = IconSize.MEDIUM,
    val gridColumns: Int = 4,
    val showLabels: Boolean = true,
    val searchBarPosition: SearchBarPosition = SearchBarPosition.TOP,
    val versionName: String = "",
) : MviState

sealed interface SettingsSideEffect : MviSideEffect {
    data class LanguageChanged(val language: AppLanguage) : SettingsSideEffect
}
