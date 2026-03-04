package com.wades.launcher.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wades.launcher.core.domain.model.AnimationSpeed
import com.wades.launcher.core.domain.model.AppLanguage
import com.wades.launcher.core.domain.model.GestureAction
import com.wades.launcher.core.domain.model.IconSize
import com.wades.launcher.core.domain.model.SearchBarPosition
import com.wades.launcher.core.domain.model.ThemeMode
import com.wades.launcher.core.domain.model.UserPreferences
import com.wades.launcher.core.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PreferencesRepository {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val DOUBLE_TAP_ACTION = stringPreferencesKey("double_tap_action")
        val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
        val LONG_PRESS_ACTION = stringPreferencesKey("long_press_action")
        val DRAWER_VIEW_MODE = stringPreferencesKey("drawer_view_mode")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val ICON_SIZE = stringPreferencesKey("icon_size")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val SEARCH_BAR_POSITION = stringPreferencesKey("search_bar_position")
        val STACK_GROUPS = stringPreferencesKey("stack_groups")
    }

    override fun observePreferences(): Flow<UserPreferences> =
        context.dataStore.data.map { prefs ->
            UserPreferences(
                language = prefs.enumOrDefault(Keys.LANGUAGE, AppLanguage.SYSTEM),
                themeMode = prefs.enumOrDefault(Keys.THEME_MODE, ThemeMode.SYSTEM),
                blurIntensity = prefs[Keys.BLUR_INTENSITY] ?: 0.5f,
                animationSpeed = prefs.enumOrDefault(Keys.ANIMATION_SPEED, AnimationSpeed.NORMAL),
                doubleTapAction = prefs.enumOrDefault(Keys.DOUBLE_TAP_ACTION, GestureAction.LOCK_SCREEN),
                swipeUpAction = prefs.enumOrDefault(Keys.SWIPE_UP_ACTION, GestureAction.OPEN_DRAWER),
                longPressAction = prefs.enumOrDefault(Keys.LONG_PRESS_ACTION, GestureAction.NONE),
                drawerDefaultViewMode = prefs[Keys.DRAWER_VIEW_MODE] ?: "LIST",
                hiddenApps = prefs[Keys.HIDDEN_APPS] ?: emptySet(),
                iconSize = prefs.enumOrDefault(Keys.ICON_SIZE, IconSize.MEDIUM),
                gridColumns = prefs[Keys.GRID_COLUMNS] ?: 4,
                showLabels = prefs[Keys.SHOW_LABELS] ?: true,
                searchBarPosition = prefs.enumOrDefault(Keys.SEARCH_BAR_POSITION, SearchBarPosition.TOP),
            )
        }

    override suspend fun setLanguage(language: AppLanguage) = setEnum(Keys.LANGUAGE, language)
    override suspend fun setThemeMode(mode: ThemeMode) = setEnum(Keys.THEME_MODE, mode)
    override suspend fun setAnimationSpeed(speed: AnimationSpeed) = setEnum(Keys.ANIMATION_SPEED, speed)
    override suspend fun setDoubleTapAction(action: GestureAction) = setEnum(Keys.DOUBLE_TAP_ACTION, action)
    override suspend fun setSwipeUpAction(action: GestureAction) = setEnum(Keys.SWIPE_UP_ACTION, action)
    override suspend fun setLongPressAction(action: GestureAction) = setEnum(Keys.LONG_PRESS_ACTION, action)
    override suspend fun setIconSize(size: IconSize) = setEnum(Keys.ICON_SIZE, size)
    override suspend fun setSearchBarPosition(position: SearchBarPosition) = setEnum(Keys.SEARCH_BAR_POSITION, position)

    override suspend fun setBlurIntensity(intensity: Float) {
        context.dataStore.edit { it[Keys.BLUR_INTENSITY] = intensity.coerceIn(0f, 1f) }
    }

    override suspend fun setDrawerDefaultViewMode(mode: String) {
        context.dataStore.edit { it[Keys.DRAWER_VIEW_MODE] = mode }
    }

    override suspend fun setHiddenApps(packages: Set<String>) {
        context.dataStore.edit { it[Keys.HIDDEN_APPS] = packages }
    }

    override suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { it[Keys.GRID_COLUMNS] = columns.coerceIn(3, 6) }
    }

    override suspend fun setShowLabels(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_LABELS] = show }
    }

    override suspend fun setStackGroups(json: String) {
        context.dataStore.edit { it[Keys.STACK_GROUPS] = json }
    }

    override fun observeStackGroups(): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[Keys.STACK_GROUPS] ?: "[]" }

    private suspend fun <T : Enum<T>> setEnum(key: Preferences.Key<String>, value: T) {
        context.dataStore.edit { it[key] = value.name }
    }

    private inline fun <reified T : Enum<T>> Preferences.enumOrDefault(
        key: Preferences.Key<String>,
        default: T,
    ): T = this[key]?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
