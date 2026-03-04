package com.wades.launcher.core.domain.model

enum class AppLanguage {
    SYSTEM,
    ZH_CN,
    EN,
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

enum class AnimationSpeed {
    SLOW,
    NORMAL,
    FAST,
}

enum class GestureAction {
    NONE,
    LOCK_SCREEN,
    OPEN_DRAWER,
    OPEN_SEARCH,
    OPEN_SETTINGS,
}

enum class IconSize {
    SMALL,
    MEDIUM,
    LARGE,
}

data class UserPreferences(
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
)
