package com.wades.launcher.feature.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wades.launcher.core.domain.model.WidgetInfo
import com.wades.launcher.core.ui.component.GlassCard
import com.wades.launcher.core.ui.component.rememberAppIcon
import com.wades.launcher.feature.widget.builtin.BuiltInWidgetSlot
import com.wades.launcher.feature.widget.host.LauncherWidgetHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    viewModel: WidgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val widgetHost = remember { LauncherWidgetHost(context, LauncherWidgetHost.HOST_ID) }
    val widgetManager = remember { AppWidgetManager.getInstance(context) }

    DisposableEffect(Unit) {
        widgetHost.startListening()
        onDispose { widgetHost.stopListening() }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is WidgetSideEffect.DeallocateWidget -> {
                    widgetHost.deleteAppWidgetId(effect.appWidgetId)
                }
                is WidgetSideEffect.ShowToast -> { /* handled by parent */ }
            }
        }
    }

    var pendingAppWidgetId by remember { mutableIntStateOf(-1) }
    var pendingProvider by remember { mutableStateOf<AppWidgetProviderInfo?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val provider = pendingProvider
        if (result.resultCode == Activity.RESULT_OK && provider != null) {
            val label = provider.loadLabel(context.packageManager)?.toString() ?: "Widget"
            viewModel.dispatch(
                WidgetIntent.SaveWidget(
                    appWidgetId = pendingAppWidgetId,
                    packageName = provider.provider.packageName,
                    label = label,
             ),
            )
        } else if (pendingAppWidgetId != -1) {
            widgetHost.deleteAppWidgetId(pendingAppWidgetId)
        }
        pendingAppWidgetId = -1
        pendingProvider = null
    }

    val bindLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val provider = pendingProvider
        if (result.resultCode == Activity.RESULT_OK && provider != null) {
            if (provider.configure != null) {
                val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = provider.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId)
                }
                configureLauncher.launch(configIntent)
            } else {
                val label = provider.loadLabel(context.packageManager)?.toString() ?: "Widget"
                viewModel.dispatch(
                    WidgetIntent.SaveWidget(
                        appWidgetId = pendingAppWidgetId,
                        packageName = provider.provider.packageName,
                        label = label,
                    ),
                )
                pendingAppWidgetId = -1
                pendingProvider = null
            }
        } else {
            if (pendingAppWidgetId != -1) {
                widgetHost.deleteAppWidgetId(pendingAppWidgetId)
            }
            pendingAppWidgetId = -1
            pendingProvider = null
        }
    }

    fun onWidgetSelected(provider: AppWidgetProviderInfo) {
        showPicker = false
        val appWidgetId = widgetHost.allocateAppWidgetId()
        pendingAppWidgetId = appWidgetId
        pendingProvider = provider

        if (widgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider.provider)) {
            if (provider.configure != null) {
                val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = provider.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                configureLauncher.launch(configIntent)
            } else {
                val label = provider.loadLabel(context.packageManager)?.toString() ?: "Widget"
                viewModel.dispatch(
                    WidgetIntent.SaveWidget(
                        appWidgetId = appWidgetId,
                        packageName = provider.provider.packageName,
                        label = label,
                    ),
                )
                pendingAppWidgetId = -1
                pendingProvider = null
            }
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
            }
            bindLauncher.launch(intent)
        }
    }

    // --- UI ---

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarPadding.calculateTopPadding() + 16.dp,
                bottom = navBarPadding.calculateBottomPadding() + 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Scrolling title
            item(key = "header") {
                Text(
                    text = "负一屏",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                )
            }

            // Built-in widgets
            items(
                items = state.builtInWidgets,
                key = { "builtin_${it.id}" },
            ) { builtIn ->
                BuiltInWidgetSlot(
                    widget = builtIn,
                    tasks = state.tasks,
                    weather = state.weather,
                    onAddTask = { viewModel.dispatch(WidgetIntent.AddTask(it)) },
                    onToggleTask = { viewModel.dispatch(WidgetIntent.ToggleTask(it)) },
                    onDeleteTask = { viewModel.dispatch(WidgetIntent.DeleteTask(it)) },
                    onRefreshWeather = { viewModel.dispatch(WidgetIntent.RefreshWeather) },
                )
            }

            // System widgets with swipe-to-delete
            items(
                items = state.widgets,
                key = { "sys_${it.id}" },
            ) { widget ->
                SwipeToDeleteWidgetCard(
                    widget = widget,
                    widgetHost = widgetHost,
                    widgetManager = widgetManager,
                    onRemove = {
                        viewModel.dispatch(
                            WidgetIntent.RemoveWidget(
                                dbId = widget.id,
                                appWidgetId = widget.appWidgetId,
                            ),
                        )
                    },
                )
            }
        }

        FloatingActionButton(
            onClick = { showPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp,
                    bottom = navBarPadding.calculateBottomPadding() + 20.dp,
                ),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加小部件")
        }
    }

    if (showPicker) {
        WidgetPickerSheet(
            widgetManager = widgetManager,
            onSelect = { onWidgetSelected(it) },
            onDismiss = { showPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWidgetCard(
    widget: WidgetInfo,
    widgetHost: LauncherWidgetHost,
    widgetManager: AppWidgetManager,
    onRemove: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val alpha by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0f,
                label = "swipe_alpha",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE57373).copy(alpha = alpha * 0.6f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "移除",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 24.dp),
                )
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        SystemWidgetCard(
            widget = widget,
            widgetHost = widgetHost,
            widgetManager = widgetManager,
        )
    }
}

@Composable
private fun SystemWidgetCard(
    widget: WidgetInfo,
    widgetHost: LauncherWidgetHost,
    widgetManager: AppWidgetManager,
) {
    val providerInfo = remember(widget.appWidgetId) {
        try {
            widgetManager.getAppWidgetInfo(widget.appWidgetId)
        } catch (_: Exception) {
            null
        }
    }

    GlassCard {
        if (providerInfo != null) {
            AndroidView(
                factory = { ctx ->
                    widgetHost.createView(ctx, widget.appWidgetId, providerInfo).apply {
                        setAppWidget(widget.appWidgetId, providerInfo)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            )
        } else {
            Text(
                text = "${widget.label} (不可用)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

// ── Widget Picker — two-level accordion with app icons ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetPickerSheet(
    widgetManager: AppWidgetManager,
    onSelect: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pm = context.packageManager

    // Group by package name, sorted by app label
    val grouped = remember {
        widgetManager.installedProviders
            .groupBy { it.provider.packageName }
            .map { (pkg, providers) ->
                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (_: Exception) {
                    pkg
                }
                Triple(pkg, appName, providers)
            }
            .sortedBy { it.second.lowercase() }
    }

    // Accordion state: only one app expanded at a time
    var expandedApp by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "选择小部件",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            LazyColumn(
                modifier = Modifier.height(480.dp),
            ) {
                grouped.forEach { (pkg, appName, providers) ->
                    // Level 1: App row
                    item(key = "app_$pkg") {
                        AppAccordionHeader(
                            packageName = pkg,
                            appName = appName,
                            widgetCount = providers.size,
                            isExpanded = expandedApp == pkg,
                            onClick = {
                                expandedApp = if (expandedApp == pkg) null else pkg
                            },
                        )
                    }

                    // Level 2: Widget details (inline expand)
                    item(key = "widgets_$pkg") {
                        AnimatedVisibility(
                            visible = expandedApp == pkg,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    )
                                    .padding(start = 72.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                            ) {
                                providers.forEach { provider ->
                                    WidgetDetailRow(
                                        provider = provider,
                                        pm = pm,
                                        onClick = { onSelect(provider) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppAccordionHeader(
    packageName: String,
    appName: String,
    widgetCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "accordion_arrow",
    )

    val appIcon: Bitmap? = rememberAppIcon(packageName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp),
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // App name
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Widget count badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$widgetCount",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Expand arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .size(20.dp)
                .rotate(arrowRotation),
        )
    }
}

@Composable
private fun WidgetDetailRow(
    provider: AppWidgetProviderInfo,
    pm: android.content.pm.PackageManager,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val label = remember(provider) {
        provider.loadLabel(pm)?.toString() ?: "Widget"
    }
    val description = remember(provider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            provider.loadDescription(context)?.toString()
        } else null
    }
    val sizeText = remember(provider) {
        "${provider.minWidth}x${provider.minHeight} dp"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = sizeText,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
