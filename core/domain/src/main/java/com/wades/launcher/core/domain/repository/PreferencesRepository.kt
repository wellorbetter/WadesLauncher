package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.AnimationSpeed
import com.wades.launcher.core.domain.model.AppLanguage
import com.wades.launcher.core.domain.model.GestureAction
import com.wades.launcher.core.domain.model.IconSize
import com.wades.launcher.core.domain.model.SearchBarPosition
import com.wades.launcher.core.domain.model.ThemeMode
import com.wades.launcher.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setBlurIntensity(intensity: Float)
    suspend fun setAnimationSpeed(speed: AnimationSpeed)
    suspend fun setDoubleTapAction(action: GestureAction)
    suspend fun setSwipeUpAction(action: GestureAction)
    suspend fun setLongPressAction(action: GestureAction)
    suspend fun setDrawerDefaultViewMode(mode: String)
    suspend fun setHiddenApps(packages: Set<String>)
    suspend fun setIconSize(size: IconSize)
    suspend fun setGridColumns(columns: Int)
    suspend fun setShowLabels(show: Boolean)
    suspend fun setSearchBarPosition(position: SearchBarPosition)
    suspend fun setStackGroups(json: String)
    fun observeStackGroups(): Flow<String>
}
