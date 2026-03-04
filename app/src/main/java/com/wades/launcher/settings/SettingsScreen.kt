package com.wades.launcher.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wades.launcher.R
import com.wades.launcher.core.domain.model.AnimationSpeed
import com.wades.launcher.core.domain.model.AppLanguage
import com.wades.launcher.core.domain.model.GestureAction
import com.wades.launcher.core.domain.model.IconSize
import com.wades.launcher.core.domain.model.SearchBarPosition
import com.wades.launcher.core.domain.model.ThemeMode
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SettingsSideEffect.LanguageChanged -> onLanguageChanged(effect.language)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding.calculateTopPadding())
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        HorizontalDivider()

        // ── 1. General ──
        SectionTitle(text = stringResource(R.string.settings_general))
        SettingsLabel(text = stringResource(R.string.settings_language))
        RadioOption("System default", state.language == AppLanguage.SYSTEM) {
            viewModel.dispatch(SettingsIntent.SetLanguage(AppLanguage.SYSTEM))
        }
        RadioOption("中文", state.language == AppLanguage.ZH_CN) {
            viewModel.dispatch(SettingsIntent.SetLanguage(AppLanguage.ZH_CN))
        }
        RadioOption("English", state.language == AppLanguage.EN) {
            viewModel.dispatch(SettingsIntent.SetLanguage(AppLanguage.EN))
        }

        SectionDivider()

        // ── 2. Theme & Appearance ──
        SectionTitle(text = stringResource(R.string.settings_theme))
        SettingsLabel(text = stringResource(R.string.settings_theme_mode))
        RadioOption(stringResource(R.string.settings_theme_system), state.themeMode == ThemeMode.SYSTEM) {
            viewModel.dispatch(SettingsIntent.SetThemeMode(ThemeMode.SYSTEM))
        }
        RadioOption(stringResource(R.string.settings_theme_light), state.themeMode == ThemeMode.LIGHT) {
            viewModel.dispatch(SettingsIntent.SetThemeMode(ThemeMode.LIGHT))
        }
        RadioOption(stringResource(R.string.settings_theme_dark), state.themeMode == ThemeMode.DARK) {
            viewModel.dispatch(SettingsIntent.SetThemeMode(ThemeMode.DARK))
        }

        SettingsLabel(text = stringResource(R.string.settings_blur_intensity))
        Slider(
            value = state.blurIntensity,
            onValueChange = { viewModel.dispatch(SettingsIntent.SetBlurIntensity(it)) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        SettingsLabel(text = stringResource(R.string.settings_animation_speed))
        RadioOption(stringResource(R.string.settings_speed_slow), state.animationSpeed == AnimationSpeed.SLOW) {
            viewModel.dispatch(SettingsIntent.SetAnimationSpeed(AnimationSpeed.SLOW))
        }
        RadioOption(stringResource(R.string.settings_speed_normal), state.animationSpeed == AnimationSpeed.NORMAL) {
            viewModel.dispatch(SettingsIntent.SetAnimationSpeed(AnimationSpeed.NORMAL))
        }
        RadioOption(stringResource(R.string.settings_speed_fast), state.animationSpeed == AnimationSpeed.FAST) {
            viewModel.dispatch(SettingsIntent.SetAnimationSpeed(AnimationSpeed.FAST))
        }

        SectionDivider()

        // ── 3. Gestures ──
        SectionTitle(text = stringResource(R.string.settings_gestures))
        GestureDropdown(
            label = stringResource(R.string.settings_double_tap),
            current = state.doubleTapAction,
            onSelect = { viewModel.dispatch(SettingsIntent.SetDoubleTapAction(it)) },
        )
        GestureDropdown(
            label = stringResource(R.string.settings_swipe_up),
            current = state.swipeUpAction,
            onSelect = { viewModel.dispatch(SettingsIntent.SetSwipeUpAction(it)) },
        )
        GestureDropdown(
            label = stringResource(R.string.settings_long_press),
            current = state.longPressAction,
            onSelect = { viewModel.dispatch(SettingsIntent.SetLongPressAction(it)) },
        )

        SectionDivider()

        // ── 4. Drawer ──
        SectionTitle(text = stringResource(R.string.settings_drawer))
        SettingsLabel(text = stringResource(R.string.settings_default_view))
        RadioOption(stringResource(R.string.settings_view_list), state.drawerDefaultViewMode == "LIST") {
            viewModel.dispatch(SettingsIntent.SetDrawerViewMode("LIST"))
        }
        RadioOption(stringResource(R.string.settings_view_grid), state.drawerDefaultViewMode == "GRID") {
            viewModel.dispatch(SettingsIntent.SetDrawerViewMode("GRID"))
        }

        SectionDivider()

        // ── 5. Home Layout ──
        SectionTitle(text = stringResource(R.string.settings_home_layout))

        SettingsLabel(text = stringResource(R.string.settings_icon_size))
        RadioOption(stringResource(R.string.settings_size_small), state.iconSize == IconSize.SMALL) {
            viewModel.dispatch(SettingsIntent.SetIconSize(IconSize.SMALL))
        }
        RadioOption(stringResource(R.string.settings_size_medium), state.iconSize == IconSize.MEDIUM) {
            viewModel.dispatch(SettingsIntent.SetIconSize(IconSize.MEDIUM))
        }
        RadioOption(stringResource(R.string.settings_size_large), state.iconSize == IconSize.LARGE) {
            viewModel.dispatch(SettingsIntent.SetIconSize(IconSize.LARGE))
        }

        SettingsLabel(text = stringResource(R.string.settings_grid_columns, state.gridColumns))
        Slider(
            value = state.gridColumns.toFloat(),
            onValueChange = { viewModel.dispatch(SettingsIntent.SetGridColumns(it.roundToInt())) },
            valueRange = 3f..6f,
            steps = 2,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        SwitchOption(
            label = stringResource(R.string.settings_show_labels),
            checked = state.showLabels,
            onCheckedChange = { viewModel.dispatch(SettingsIntent.SetShowLabels(it)) },
        )

        SettingsLabel(text = stringResource(R.string.settings_search_position))
        RadioOption(stringResource(R.string.settings_position_top), state.searchBarPosition == SearchBarPosition.TOP) {
            viewModel.dispatch(SettingsIntent.SetSearchBarPosition(SearchBarPosition.TOP))
        }
        RadioOption(stringResource(R.string.settings_position_bottom), state.searchBarPosition == SearchBarPosition.BOTTOM) {
            viewModel.dispatch(SettingsIntent.SetSearchBarPosition(SearchBarPosition.BOTTOM))
        }
        RadioOption(stringResource(R.string.settings_position_hidden), state.searchBarPosition == SearchBarPosition.HIDDEN) {
            viewModel.dispatch(SettingsIntent.SetSearchBarPosition(SearchBarPosition.HIDDEN))
        }

        SectionDivider()

        // ── 6. About ──
        SectionTitle(text = stringResource(R.string.settings_about))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_version),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Reusable components ──

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun RadioOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SwitchOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun GestureDropdown(
    label: String,
    current: GestureAction,
    onSelect: (GestureAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = gestureActionLabel(current),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GestureAction.entries.forEach { action ->
                DropdownMenuItem(
                    text = { Text(gestureActionLabel(action)) },
                    onClick = {
                        onSelect(action)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun gestureActionLabel(action: GestureAction): String = when (action) {
    GestureAction.NONE -> stringResource(R.string.gesture_none)
    GestureAction.LOCK_SCREEN -> stringResource(R.string.gesture_lock_screen)
    GestureAction.OPEN_DRAWER -> stringResource(R.string.gesture_open_drawer)
    GestureAction.OPEN_SEARCH -> stringResource(R.string.gesture_open_search)
    GestureAction.OPEN_SETTINGS -> stringResource(R.string.gesture_open_settings)
}
