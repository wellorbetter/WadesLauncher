package com.wades.launcher.feature.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.ui.component.AppIcon
import com.wades.launcher.core.ui.component.AppIconImage
import com.wades.launcher.core.ui.component.SearchBar
import com.wades.launcher.core.ui.component.consumeHorizontalScroll
import com.wades.launcher.core.ui.component.rememberAppIconBitmap
import com.wades.launcher.feature.drawer.R as DrawerR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DrawerScreen(
    onAppLaunch: (packageName: String, componentName: String) -> Unit,
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DrawerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is DrawerSideEffect.LaunchApp -> {
                    onAppLaunch(effect.packageName, effect.componentName)
                }
            }
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val imePadding = WindowInsets.ime.asPaddingValues()
    val bottomPadding = maxOf(
        navBarPadding.calculateBottomPadding(),
        imePadding.calculateBottomPadding(),
    )

    // Shared list state for alphabet bar scrolling
    val listState = rememberLazyListState()

    // Compute letter -> item index mapping
    val letterIndexMap = remember(state.filteredApps, state.viewMode) {
        buildLetterIndexMap(state.filteredApps, state.viewMode)
    }

    // Dark scrim background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = statusBarPadding.calculateTopPadding() + 8.dp,
                        bottom = bottomPadding + 8.dp,
                    ),
            ) {
                // Search bar + view mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.dispatch(DrawerIntent.SearchQueryChanged(it)) },
                        placeholder = stringResource(DrawerR.string.drawer_search_hint),
                        shape = RoundedCornerShape(12.dp),
                        containerColor = Color.White.copy(alpha = 0.08f),
                        borderColor = Color.White.copy(alpha = 0.10f),
                        textColor = Color.White.copy(alpha = 0.85f),
                        placeholderColor = Color.White.copy(alpha = 0.35f),
                        iconTint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f),
                    )
                    // Toggle button
                    IconButton(
                        onClick = { viewModel.dispatch(DrawerIntent.ToggleViewMode) },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(
                            imageVector = when (state.viewMode) {
                                DrawerViewMode.LIST -> Icons.Default.GridView
                                DrawerViewMode.GRID -> Icons.Default.ViewList
                            },
                            contentDescription = stringResource(DrawerR.string.drawer_toggle_view),
                            tint = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content area
                Box(modifier = Modifier.weight(1f)) {
                    when (state.viewMode) {
                        DrawerViewMode.LIST -> {
                            DrawerListView(
                                apps = state.filteredApps,
                                listState = listState,
                                onAppClick = { viewModel.dispatch(DrawerIntent.AppClicked(it)) },
                            )
                        }
                        DrawerViewMode.GRID -> {
                            DrawerGridView(
                                apps = state.filteredApps,
                                listState = listState,
                                onAppClick = { viewModel.dispatch(DrawerIntent.AppClicked(it)) },
                            )
                        }
                    }

                    // Alphabet index bar on right
                    AlphabetIndexBar(
                        apps = state.filteredApps,
                        listState = listState,
                        letterIndexMap = letterIndexMap,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }
        }

        // Left-edge swipe gesture to navigate back to Home
        EdgeSwipeBack(
            onSwipeRight = onSwipeRight,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

/**
 * Build a mapping from letter char to LazyColumn item index.
 * LIST mode: header items + individual app rows
 * GRID mode: header items + single grid row per letter
 */
private fun buildLetterIndexMap(
    apps: List<AppInfo>,
    viewMode: DrawerViewMode,
): Map<Char, Int> {
    val grouped = apps.groupBy {
        val key = it.sortKey.ifEmpty { it.label.lowercase() }
        val first = key.firstOrNull()?.uppercaseChar() ?: '#'
        if (first.isLetter()) first else '#'
    }.toSortedMap()

    val map = mutableMapOf<Char, Int>()
    var index = 0
    grouped.forEach { (letter, letterApps) ->
        map[letter] = index
        when (viewMode) {
            DrawerViewMode.LIST -> {
                index++ // header
                index += letterApps.size // app rows
            }
            DrawerViewMode.GRID -> {
                index++ // header
                index++ // grid row
            }
        }
    }
    return map
}

// ── LIST view (vertical compact rows) ──

@Composable
private fun DrawerListView(
    apps: List<AppInfo>,
    listState: LazyListState,
    onAppClick: (AppInfo) -> Unit,
) {
    val grouped = remember(apps) {
        apps.groupBy {
            val key = it.sortKey.ifEmpty { it.label.lowercase() }
            val first = key.firstOrNull()?.uppercaseChar() ?: '#'
            if (first.isLetter()) first else '#'
        }.toSortedMap()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 36.dp),
    ) {
        grouped.forEach { (letter, letterApps) ->
            item(key = "letter_$letter", contentType = "header") {
                LetterBadgeHeader(letter = letter)
            }
            items(
                items = letterApps,
                key = { it.componentName },
                contentType = { "app_row" },
            ) { app ->
                CompactAppRow(app = app, onClick = { onAppClick(app) })
            }
        }
    }
}

// ── GRID view (horizontal letter-grouped rows) ──

@Composable
private fun DrawerGridView(
    apps: List<AppInfo>,
    listState: LazyListState,
    onAppClick: (AppInfo) -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = screenWidth / 5

    val grouped = remember(apps) {
        apps.groupBy {
            val key = it.sortKey.ifEmpty { it.label.lowercase() }
            val first = key.firstOrNull()?.uppercaseChar() ?: '#'
            if (first.isLetter()) first else '#'
        }.toSortedMap()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 36.dp),
    ) {
        grouped.forEach { (letter, letterApps) ->
            item(key = "grid_letter_$letter", contentType = "header") {
                Text(
                    text = letter.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.35f),
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 2.dp),
                )
            }
            item(key = "grid_row_$letter", contentType = "grid_row") {
                val rowState = rememberLazyListState()
                LazyRow(
                    state = rowState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .consumeHorizontalScroll(rowState),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    items(
                        items = letterApps,
                        key = { "g_${it.componentName}" },
                    ) { app ->
                        AppIcon(
                            label = app.label,
                            packageName = app.packageName,
                            componentName = app.componentName,
                            onClick = { onAppClick(app) },
                            modifier = Modifier.width(itemWidth),
                        )
                    }
                }
            }
        }
    }
}

// ── Badge letter header + divider ──

@Composable
private fun LetterBadgeHeader(letter: Char) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 10.dp, bottom = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            thickness = 0.5.dp,
            color = Color.White.copy(alpha = 0.06f),
        )
    }
}

// ── Compact app row (32dp icon, 6dp vertical padding) ──

@Composable
private fun CompactAppRow(
    app: AppInfo,
    onClick: () -> Unit,
) {
    val iconBitmap = rememberAppIconBitmap(app.packageName, app.componentName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (iconBitmap != null) {
                AppIconImage(bitmap = iconBitmap, modifier = Modifier.size(32.dp))
            }
        }

        Text(
            text = app.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f),
        )
    }
}

// ── Alphabet index bar with drag/tap interaction ──

@Composable
private fun AlphabetIndexBar(
    apps: List<AppInfo>,
    listState: LazyListState,
    letterIndexMap: Map<Char, Int>,
    modifier: Modifier = Modifier,
) {
    val letters = remember(apps) {
        val set = mutableSetOf<Char>()
        apps.forEach { app ->
            val key = app.sortKey.ifEmpty { app.label.lowercase() }
            val first = key.firstOrNull()?.uppercaseChar() ?: '#'
            set.add(if (first.isLetter()) first else '#')
        }
        set.sorted()
    }

    if (letters.isEmpty()) return

    val scope = rememberCoroutineScope()
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var barHeightPx by remember { mutableStateOf(0f) }

    // Auto-hide the floating indicator after drag ends
    LaunchedEffect(activeLetter) {
        if (activeLetter != null) {
            delay(600)
            activeLetter = null
        }
    }

    fun scrollToLetter(touchY: Float) {
        if (barHeightPx <= 0f || letters.isEmpty()) return
        val index = ((touchY / barHeightPx) * letters.size)
            .toInt()
            .coerceIn(0, letters.size - 1)
        val letter = letters[index]
        activeLetter = letter
        val itemIndex = letterIndexMap[letter] ?: 0
        scope.launch { listState.scrollToItem(itemIndex) }
    }

    Box(modifier = modifier) {
        // Floating letter indicator — left of the bar
        AnimatedVisibility(
            visible = activeLetter != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-48).dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (activeLetter ?: ' ').toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }

        // The letter bar itself
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(vertical = 4.dp)
                .onGloballyPositioned { coords ->
                    barHeightPx = coords.size.height.toFloat()
                }
                .pointerInput(letters) {
                    detectTapGestures { offset ->
                        scrollToLetter(offset.y)
                    }
                }
                .pointerInput(letters) {
                    detectVerticalDragGestures(
                        onDragStart = { offset -> scrollToLetter(offset.y) },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            scrollToLetter(change.position.y)
                        },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            letters.forEach { letter ->
                val isActive = letter == activeLetter
                Text(
                    text = letter.toString(),
                    fontSize = if (isActive) 12.sp else 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 1.dp),
                )
            }
        }
    }
}

// ── Left-edge swipe back gesture ──

@Composable
private fun EdgeSwipeBack(
    onSwipeRight: () -> Unit,
    edgeWidth: Dp = 48.dp,
    threshold: Dp = 60.dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    var dragOffset by remember { mutableStateOf(0f) }
    var triggered by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(edgeWidth)
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { _ ->
                        dragOffset = 0f
                        triggered = false
                    },
                    onDragEnd = {
                        if (dragOffset > thresholdPx && !triggered) {
                            triggered = true
                            onSwipeRight()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        if (dragAmount > 0) {
                            change.consume()
                            dragOffset += dragAmount
                        }
                    },
                )
            },
    ) {
        // Visual hint: subtle arrow indicator when dragging
        if (dragOffset > 20f) {
            val alpha = (dragOffset / thresholdPx).coerceIn(0f, 0.6f)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = alpha)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = alpha),
                )
            }
        }
    }
}
