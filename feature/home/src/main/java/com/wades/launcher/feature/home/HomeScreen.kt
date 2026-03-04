package com.wades.launcher.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.ui.component.SwipeCardStack
import com.wades.launcher.core.ui.component.PullDownPanel
import com.wades.launcher.feature.assistant.AssistantScreen
import com.wades.launcher.feature.home.R as HomeR
import com.wades.launcher.core.ui.component.AppIcon
import com.wades.launcher.core.ui.component.AppIconImage
import com.wades.launcher.core.ui.component.GlassCard
import com.wades.launcher.core.ui.component.SearchBar
import com.wades.launcher.core.ui.component.rememberAppIconBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onAppLaunch: (packageName: String, componentName: String) -> Unit,
    onOpenSettings: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    currentPage: Int = 1,
    pageCount: Int = 3,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.LaunchApp -> {
                    onAppLaunch(effect.packageName, effect.componentName)
                }
                is HomeSideEffect.ShowToast -> { /* TODO */ }
                is HomeSideEffect.OpenSettings -> onOpenSettings()
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

    var isAiPanelExpanded by remember { mutableStateOf(false) }

    PullDownPanel(
        isExpanded = isAiPanelExpanded,
        onExpandedChange = { isAiPanelExpanded = it },
        modifier = modifier,
        panelContent = { AssistantScreen() },
        behindContent = {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isSearchActive -> {
                        SearchResultsGrid(
                            apps = state.searchResults,
                            onAppClick = { viewModel.dispatch(HomeIntent.AppClicked(it)) },
                            topPadding = statusBarPadding.calculateTopPadding() + 8.dp,
                            bottomPadding = bottomPadding + 72.dp,
                        )
                    }
                    else -> {
                        HomeSections(
                            frequentApps = state.frequentApps,
                            recentApps = state.recentApps,
                            newApps = state.newApps,
                            categoryApps = state.categoryApps,
                            allApps = state.allApps,
                            folders = state.folders,
                            isColdStart = state.isColdStart,
                            isEditMode = state.isEditMode,
                            allInstalledApps = state.frequentApps + state.recentApps + state.newApps + state.allApps,
                            stackGroups = state.stackGroups,
                            expandedStackIndex = state.expandedStackIndex,
                            onAppClick = { viewModel.dispatch(HomeIntent.AppClicked(it)) },
                            onAppLongClick = { viewModel.dispatch(HomeIntent.AppLongClicked(it)) },
                            onFolderClick = { viewModel.dispatch(HomeIntent.OpenFolder(it)) },
                            onEnterEditMode = { viewModel.dispatch(HomeIntent.EnterEditMode) },
                            onExitEditMode = { viewModel.dispatch(HomeIntent.ExitEditMode) },
                            onDeleteCategory = { viewModel.dispatch(HomeIntent.DeleteCategory(it)) },
                            onAddCategory = { viewModel.dispatch(HomeIntent.AddCategory(it)) },
                            onMergeSections = { dragged, target -> viewModel.dispatch(HomeIntent.MergeSections(dragged, target)) },
                            onExpandStack = { viewModel.dispatch(HomeIntent.ExpandStack(it)) },
                            onCollapseStack = { viewModel.dispatch(HomeIntent.CollapseStack) },
                            onUnstackSection = { si, sk -> viewModel.dispatch(HomeIntent.UnstackSection(si, sk)) },
                            onReorderInStack = { si, from, to -> viewModel.dispatch(HomeIntent.ReorderInStack(si, from, to)) },
                            topPadding = statusBarPadding.calculateTopPadding(),
                            bottomPadding = bottomPadding + 72.dp,
                        )
                    }
                }

                SwipeableSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.dispatch(HomeIntent.SearchQueryChanged(it)) },
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                    currentPage = currentPage,
                    pageCount = pageCount,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomPadding + 8.dp),
                )

                // Settings gear icon — top right
                if (!state.isEditMode && !state.isSearchActive) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                top = statusBarPadding.calculateTopPadding() + 12.dp,
                                end = 16.dp,
                            )
                            .size(24.dp)
                            .clickable { viewModel.dispatch(HomeIntent.OpenSettings) },
                    )
                }
            }
        },
    )

    // Long-press app menu dialog
    state.longPressedApp?.let { app ->
        AppActionDialog(
            app = app,
            folders = state.folders,
            onCreateFolder = { name ->
                viewModel.dispatch(HomeIntent.CreateFolder(name = name, initialApp = app))
            },
            onAddToFolder = { folderId ->
                viewModel.dispatch(HomeIntent.AddToFolder(folderId = folderId, app = app))
         },
            onDismiss = { viewModel.dispatch(HomeIntent.DismissAppMenu) },
        )
    }

    // Open folder dialog
    state.openFolder?.let { folder ->
        val folderApps = (state.frequentApps + state.newApps + state.allApps)
            .filter { it.packageName in folder.appPackageNames }
            .distinctBy { it.packageName }

        FolderDialog(
            folder = folder,
            apps = folderApps,
            onAppClick = { viewModel.dispatch(HomeIntent.AppClicked(it)) },
            onRemoveApp = { pkg ->
                viewModel.dispatch(HomeIntent.RemoveFromFolder(folder.id, pkg))
            },
            onRename = { name ->
                viewModel.dispatch(HomeIntent.RenameFolder(folder.id, name))
            },
            onDelete = { viewModel.dispatch(HomeIntent.DeleteFolder(folder.id)) },
            onDismiss = { viewModel.dispatch(HomeIntent.CloseFolder) },
        )
    }
}

// ── Time Display ──

@Composable
private fun ClockWidget() {
    val dateFormat = stringResource(HomeR.string.date_format)
    var timeText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    LaunchedEffect(dateFormat) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat(dateFormat, Locale.getDefault())
        while (true) {
            val now = Date()
            timeText = timeFmt.format(now)
            dateText = dateFmt.format(now)
            delay(10_000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = timeText,
            style = TextStyle(
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraLight,
                color = Color.White,
                letterSpacing = 2.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.3f),
                    offset = Offset(0f, 2f),
                    blurRadius = 8f,
                ),
            ),
        )
        Text(
            text = dateText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 3.sp,
        )
    }
}

// ── Section model for overflow stacking ──

private sealed class HomeSection {
    abstract val key: String

    data class Frequent(val apps: List<AppInfo>) : HomeSection() {
        override val key = "section_frequent"
    }

    data class Category(val category: AppCategory) : HomeSection() {
        override val key get() = "section_cat_${category.id}"
    }

    data class Recent(val apps: List<AppInfo>) : HomeSection() {
        override val key = "section_recent"
    }

    data class New(val apps: List<AppInfo>) : HomeSection() {
        override val key = "section_new"
    }

    data class Folders(
        val folders: List<LauncherGroup>,
        val allApps: List<AppInfo>,
    ) : HomeSection() {
        override val key = "section_folders"
    }
}

private const val MAX_NORMAL_SECTIONS = 3

// ── Stack group model ──

private data class StackGroup(val index: Int, val sections: List<HomeSection>)

// ── Sections ──

@Composable
private fun HomeSections(
    frequentApps: List<AppInfo>,
    recentApps: List<AppInfo>,
    newApps: List<AppInfo>,
    categoryApps: List<AppCategory>,
    allApps: List<AppInfo>,
    folders: List<LauncherGroup>,
    isColdStart: Boolean,
    isEditMode: Boolean,
    allInstalledApps: List<AppInfo>,
    stackGroups: List<List<String>>,
    expandedStackIndex: Int?,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onFolderClick: (String) -> Unit,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onDeleteCategory: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    onMergeSections: (draggedKey: String, targetKey: String) -> Unit,
    onExpandStack: (Int) -> Unit,
    onCollapseStack: () -> Unit,
    onUnstackSection: (stackIndex: Int, sectionKey: String) -> Unit,
    onReorderInStack: (stackIndex: Int, fromPos: Int, toPos: Int) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    if (isEditMode) {
        BackHandler(onBack = onExitEditMode)
    }

    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = (screenWidth - 48.dp) / 4
    var showAddDialog by remember { mutableStateOf(false) }

    // Build ordered list of non-empty sections
    val sections = remember(frequentApps, categoryApps, recentApps, newApps, folders, isColdStart) {
        buildList {
            if (frequentApps.isNotEmpty() && !isColdStart) {
                add(HomeSection.Frequent(frequentApps))
            }
            categoryApps.forEach { add(HomeSection.Category(it)) }
            if (recentApps.isNotEmpty()) add(HomeSection.Recent(recentApps))
            if (newApps.isNotEmpty()) add(HomeSection.New(newApps))
            if (folders.isNotEmpty()) add(HomeSection.Folders(folders, allInstalledApps))
        }
    }

    val stackedKeys = remember(stackGroups) { stackGroups.flatten().toSet() }

    if (isEditMode) {
        // ── Edit mode: all sections flat, with drag-to-stack ──
        val scope = rememberCoroutineScope()
        var draggedKey by remember { mutableStateOf<String?>(null) }
        var dragOffsetY by remember { mutableFloatStateOf(0f) }
        var hoverTargetKey by remember { mutableStateOf<String?>(null) }
        var hoverStartTime by remember { mutableStateOf(0L) }
        val sectionPositions = remember { mutableMapOf<String, Float>() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
        ) {
            item(key = "clock") { ClockWidget() }

            item(key = "drag_hint") {
                Text(
                    text = stringResource(HomeR.string.edit_drag_hint),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            sections.forEach { section ->
                item(key = section.key) {
                    val isDragging = draggedKey == section.key
                    val isHoverTarget = hoverTargetKey == section.key

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                sectionPositions[section.key] = coords.localToRoot(Offset.Zero).y
                            }
                            .then(
                                if (isDragging) {
                                    Modifier
                                        .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                                        .zIndex(10f)
                                        .graphicsLayer { alpha = 0.8f }
                                } else Modifier,
                            )
                            .then(
                                if (isHoverTarget) {
                                    Modifier.border(
                                        2.dp,
                                        Color(0xFF4A90D9),
                                        RoundedCornerShape(16.dp),
                                    )
                                } else Modifier,
                            )
                            .pointerInput(section.key) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedKey = section.key
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                        // Find hover target
                                        val myPos = sectionPositions[section.key] ?: 0f
                                        val currentY = myPos + dragOffsetY
                                        val target = sectionPositions.entries
                                            .filter { it.key != section.key }
                                            .minByOrNull { abs(it.value - currentY) }
                                            ?.takeIf { abs(it.value - currentY) < with(density) { 80.dp.toPx() } }
                                            ?.key
                                        if (target != hoverTargetKey) {
                                            hoverTargetKey = target
                                            hoverStartTime = if (target != null) System.currentTimeMillis() else 0L
                                        }
                                    },
                                    onDragEnd = {
                                        val target = hoverTargetKey
                                        val dragged = draggedKey
                                        if (dragged != null && target != null &&
                                            System.currentTimeMillis() - hoverStartTime > 500L
                                        ) {
                                            onMergeSections(dragged, target)
                                        }
                                        draggedKey = null
                                        dragOffsetY = 0f
                                        hoverTargetKey = null
                                        hoverStartTime = 0L
                                    },
                                    onDragCancel = {
                                        draggedKey = null
                                        dragOffsetY = 0f
                                        hoverTargetKey = null
                                        hoverStartTime = 0L
                                    },
                                )
                            },
                    ) {
                        SectionContent(
                            section = section,
                            itemWidth = itemWidth,
                            isEditMode = true,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick,
                            onFolderClick = onFolderClick,
                            onEnterEditMode = onEnterEditMode,
                            onDeleteCategory = onDeleteCategory,
                        )
                    }
                }
            }

            item(key = "edit_toolbar") {
                EditModeToolbar(
                    onAddCategory = { showAddDialog = true },
                    onDone = onExitEditMode,
                )
            }
            item(key = "edit_blank_exit") {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable(onClick = onExitEditMode),
                )
            }
        }
    } else {
        // ── Normal mode: respect stack groups + overflow ──
        val displayItems = remember(sections, stackGroups, stackedKeys) {
            buildList<Any> {
                val processedStackIndices = mutableSetOf<Int>()
                for (section in sections) {
                    if (section.key in stackedKeys) {
                        val stackIdx = stackGroups.indexOfFirst { section.key in it }
                        if (stackIdx >= 0 && stackIdx !in processedStackIndices) {
                            processedStackIndices.add(stackIdx)
                            val stackSections = stackGroups[stackIdx].mapNotNull { key ->
                                sections.find { it.key == key }
                            }
                            if (stackSections.size >= 2) {
                                add(StackGroup(stackIdx, stackSections))
                            } else {
                                stackSections.forEach { add(it) }
                            }
                        }
                    } else {
                        add(section)
                    }
                }
            }
        }

        val showAllNormal = displayItems.size <= MAX_NORMAL_SECTIONS
        val normalItems = if (showAllNormal) displayItems else displayItems.take(MAX_NORMAL_SECTIONS)
        val overflowItems = if (showAllNormal) emptyList() else displayItems.drop(MAX_NORMAL_SECTIONS)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
        ) {
            item(key = "clock") { ClockWidget() }

            normalItems.forEach { displayItem ->
                when (displayItem) {
                    is HomeSection -> {
                        item(key = displayItem.key) {
                            SectionContent(
                                section = displayItem,
                                itemWidth = itemWidth,
                                isEditMode = false,
                                onAppClick = onAppClick,
                                onAppLongClick = onAppLongClick,
                                onFolderClick = onFolderClick,
                                onEnterEditMode = onEnterEditMode,
                                onDeleteCategory = onDeleteCategory,
                            )
                        }
                    }
                    is StackGroup -> {
                        item(key = "stack_${displayItem.index}") {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .clickable { onExpandStack(displayItem.index) },
                            ) {
                                SwipeCardStack(
                                    items = displayItem.sections,
                                ) { section ->
                                    SectionContent(
                                        section = section,
                                        itemWidth = itemWidth,
                                        isEditMode = false,
                                        onAppClick = onAppClick,
                                        onAppLongClick = onAppLongClick,
                                        onFolderClick = onFolderClick,
                                        onEnterEditMode = onEnterEditMode,
                                        onDeleteCategory = onDeleteCategory,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overflow → card stack for remaining items
            if (overflowItems.isNotEmpty()) {
                val overflowSections = overflowItems.filterIsInstance<HomeSection>()
                if (overflowSections.isNotEmpty()) {
                    item(key = "overflow_stack") {
                        SwipeCardStack(
                            items = overflowSections,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        ) { section ->
                            SectionContent(
                                section = section,
                                itemWidth = itemWidth,
                                isEditMode = false,
                                onAppClick = onAppClick,
                                onAppLongClick = onAppLongClick,
                                onFolderClick = onFolderClick,
                                onEnterEditMode = onEnterEditMode,
                                onDeleteCategory = onDeleteCategory,
                            )
                        }
                    }
                }
                overflowItems.filterIsInstance<StackGroup>().forEach { sg ->
                    item(key = "stack_overflow_${sg.index}") {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clickable { onExpandStack(sg.index) },
                        ) {
                            SwipeCardStack(
                                items = sg.sections,
                            ) { section ->
                                SectionContent(
                                    section = section,
                                    itemWidth = itemWidth,
                                    isEditMode = false,
                                    onAppClick = onAppClick,
                                    onAppLongClick = onAppLongClick,
                                    onFolderClick = onFolderClick,
                                    onEnterEditMode = onEnterEditMode,
                                    onDeleteCategory = onDeleteCategory,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onConfirm = onAddCategory,
            onDismiss = { showAddDialog = false },
        )
    }

    // Stack detail dialog
    if (expandedStackIndex != null) {
        val stackSections = stackGroups.getOrNull(expandedStackIndex)
            ?.mapNotNull { key -> sections.find { it.key == key } }
            ?: emptyList()
        StackDetailDialog(
            stackIndex = expandedStackIndex,
            sections = stackSections,
            itemWidth = itemWidth,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onFolderClick = onFolderClick,
            onEnterEditMode = onEnterEditMode,
            onDeleteCategory = onDeleteCategory,
            onUnstack = { sectionKey -> onUnstackSection(expandedStackIndex, sectionKey) },
            onDismiss = onCollapseStack,
        )
    }
}

// ── Section content renderer ──

@Composable
private fun SectionContent(
    section: HomeSection,
    itemWidth: androidx.compose.ui.unit.Dp,
    isEditMode: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onFolderClick: (String) -> Unit,
    onEnterEditMode: () -> Unit,
    onDeleteCategory: (String) -> Unit,
) {
    when (section) {
        is HomeSection.Frequent -> {
            GlassCard {
                Column {
                    SectionHeader(title = stringResource(HomeR.string.section_frequent))
                    AppRow(apps = section.apps, itemWidth = itemWidth, onAppClick = onAppClick, onAppLongClick = onAppLongClick)
                }
            }
        }
        is HomeSection.Category -> {
            EditableCategoryCard(
                category = section.category,
                isEditMode = isEditMode,
                itemWidth = itemWidth,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                onEnterEditMode = onEnterEditMode,
                onDelete = { onDeleteCategory(section.category.id) },
            )
        }
        is HomeSection.Recent -> {
            GlassCard {
                Column {
                    SectionHeader(title = stringResource(HomeR.string.section_recent))
                    AppRow(apps = section.apps, itemWidth = itemWidth, onAppClick = onAppClick, onAppLongClick = onAppLongClick)
                }
            }
        }
        is HomeSection.New -> {
            GlassCard {
                Column {
                    SectionHeader(title = stringResource(HomeR.string.section_new))
                    AppRow(apps = section.apps, itemWidth = itemWidth, onAppClick = onAppClick, onAppLongClick = onAppLongClick)
                }
            }
        }
        is HomeSection.Folders -> {
            GlassCard {
                Column {
                    SectionHeader(title = stringResource(HomeR.string.section_folders))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        items(items = section.folders, key = { "folder_${it.id}" }) { folder ->
                            FolderIcon(
                                folder = folder,
                                allApps = section.allApps,
                                onClick = { onFolderClick(folder.id) },
                                modifier = Modifier.width(itemWidth),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable app row: <=4 static, >4 scrollable ──

@Composable
private fun AppRow(
    apps: List<AppInfo>,
    itemWidth: androidx.compose.ui.unit.Dp,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
) {
    if (apps.size <= 4) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            apps.forEach { app ->
                AppIcon(
                    label = app.label,
                    packageName = app.packageName,
                    componentName = app.componentName,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.width(itemWidth),
                )
            }
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            items(items = apps, key = { "a_${it.componentName}" }) { app ->
                AppIcon(
                    label = app.label,
                    packageName = app.packageName,
                    componentName = app.componentName,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.width(itemWidth),
                )
            }
        }
    }
}

// ── Folder Icon (2x2 mini preview) ──

@Composable
private fun FolderIcon(
    folder: LauncherGroup,
    allApps: List<AppInfo>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val folderApps = remember(folder.appPackageNames, allApps) {
        folder.appPackageNames
            .mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
            .take(4)
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            // 2x2 mini grid
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (row in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (col in 0..1) {
                            val idx = row * 2 + col
                            if (idx < folderApps.size) {
                                val app = folderApps[idx]
                                val bitmap = rememberAppIconBitmap(app.packageName, app.componentName)
                                Box(modifier = Modifier.size(22.dp)) {
                                    if (bitmap != null) {
                                        AppIconImage(
                                            bitmap = bitmap,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = folder.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp),
        )
    }
}

// ── Folder Dialog ──

@Composable
private fun FolderDialog(
    folder: LauncherGroup,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onRemoveApp: (String) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var isRenaming by remember { mutableStateOf(false) }
    var renameText by remember(folder.name) { mutableStateOf(folder.name) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
        ) {
            // Header
            if (isRenaming) {
                TextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { isRenaming = false }) {
                        Text(stringResource(HomeR.string.action_cancel))
                    }
                    TextButton(onClick = {
                        if (renameText.isNotBlank()) {
                            onRename(renameText)
                            isRenaming = false
                        }
                    }) {
                        Text(stringResource(HomeR.string.action_confirm))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { isRenaming = true }) {
                        Text(stringResource(HomeR.string.edit_rename))
                    }
                    TextButton(onClick = onDelete) {
                        Text(stringResource(HomeR.string.edit_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App grid
            if (apps.isEmpty()) {
                Text(
                    text = stringResource(HomeR.string.folder_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                val rows = apps.chunked(4)
                rows.forEach { rowApps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        rowApps.forEach { app ->
                            AppIcon(
                                label = app.label,
                                packageName = app.packageName,
                                componentName = app.componentName,
                                onClick = {
                                    onAppClick(app)
                                    onDismiss()
                                },
                                onLongClick = { onRemoveApp(app.packageName) },
                                modifier = Modifier.width(72.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── App Action Dialog (long-press menu) ──

@Composable
private fun AppActionDialog(
    app: AppInfo,
    folders: List<LauncherGroup>,
    onCreateFolder: (String) -> Unit,
    onAddToFolder: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showCreateInput by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }
    if (showCreateInput) {
        AlertDialog(
            onDismissRequest = { showCreateInput = false },
            title = { Text(stringResource(HomeR.string.edit_create_folder)) },
            text = {
                TextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    placeholder = { Text(stringResource(HomeR.string.edit_folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (folderName.isNotBlank()) {
                        onCreateFolder(folderName)
                        showCreateInput = false
                    }
                }) {
                    Text(stringResource(HomeR.string.action_create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateInput = false }) {
                    Text(stringResource(HomeR.string.action_cancel))
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(app.label) },
            text = {
                Column {
                    TextButton(
                        onClick = { showCreateInput = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(HomeR.string.edit_create_folder))
                    }
                    folders.forEach { folder ->
                        TextButton(
                            onClick = { onAddToFolder(folder.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(HomeR.string.add_to_folder, folder.name))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(HomeR.string.action_cancel))
                }
            },
        )
    }
}

// ── Search Results ──

@Composable
private fun SearchResultsGrid(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = screenWidth / 4

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
    ) {
        if (apps.isEmpty()) {
            item {
                Text(
                    text = stringResource(HomeR.string.search_no_results),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
            }
        } else {
            val rows = apps.chunked(4)
            items(count = rows.size, key = { "s_$it" }) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                      .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    rows[index].forEach { app ->
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

// ── Section Header ──

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.6f),
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp),
    )
}

// ── Editable Category Card (wobble + delete badge) ──

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditableCategoryCard(
    category: AppCategory,
    isEditMode: Boolean,
    itemWidth: androidx.compose.ui.unit.Dp,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onEnterEditMode: () -> Unit,
    onDelete: () -> Unit,
) {
    // Smooth iOS-style wobble — deterministic phase offset per card
    val wobbleDuration = remember { 150 + (category.id.hashCode().and(0x7F) % 40) }
    val transition = rememberInfiniteTransition(label = "wobble_${category.id}")
    val rawRotation by transition.animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(wobbleDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wobble_rot_${category.id}",
    )
    val rawScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.985f,
        animationSpec = infiniteRepeatable(
            animation = tween(wobbleDuration * 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wobble_scale_${category.id}",
    )
    // Smooth enter/exit multiplier so wobble fades in/out gracefully
    val multiplier by animateFloatAsState(
        targetValue = if (isEditMode) 1f else 0f,
        animationSpec = tween(300),
        label = "wobble_mult_${category.id}",
    )

    Box(modifier = Modifier.padding(horizontal = 0.dp)) {
        GlassCard(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = rawRotation * multiplier
                    val s = 1f + (rawScale - 1f) * multiplier
                    scaleX = s
                    scaleY = s
                }
                .combinedClickable(
                    onClick = {},
                    onLongClick = { if (!isEditMode) onEnterEditMode() },
                ),
        ) {
            Column {
                SectionHeader(title = category.name)
                AppRow(
                    apps = category.apps,
                    itemWidth = itemWidth,
                    onAppClick = onAppClick,
                    onAppLongClick = onAppLongClick,
                )
            }
        }

        // Delete badge — red circle at top-right
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .zIndex(1f)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✕",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// ── Edit Mode Toolbar (GlassCard wrapped) ──

@Composable
private fun EditModeToolbar(
    onAddCategory: () -> Unit,
    onDone: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(HomeR.string.edit_add_category),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAddCategory)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Text(
                text = stringResource(HomeR.string.edit_done),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF81C784),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDone)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

// ── Category Expand/Collapse Pill ──

@Composable
private fun CategoryExpandPill(
    isExpanded: Boolean,
    hiddenCount: Int,
    onClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "pill_arrow",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (isExpanded) stringResource(HomeR.string.category_collapse) else stringResource(HomeR.string.category_expand_more, hiddenCount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation),
            )
        }
    }
}

// ── Add Category Dialog ──

@Composable
private fun AddCategoryDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(HomeR.string.add_category_title)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(HomeR.string.edit_category_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(name.trim())
                    onDismiss()
                }
            }) {
                Text(stringResource(HomeR.string.action_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(HomeR.string.action_cancel))
            }
        },
    )
}

// ── Stack Detail Dialog ──

@Composable
private fun StackDetailDialog(
    stackIndex: Int,
    sections: List<HomeSection>,
    itemWidth: androidx.compose.ui.unit.Dp,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onFolderClick: (String) -> Unit,
    onEnterEditMode: () -> Unit,
    onDeleteCategory: (String) -> Unit,
    onUnstack: (sectionKey: String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(HomeR.string.stack_detail_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(HomeR.string.action_confirm),
                        color = Color(0xFF81C784),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (sections.isEmpty()) {
                Text(
                    text = stringResource(HomeR.string.stack_empty),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                // Scrollable list of stacked sections
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        count = sections.size,
                        key = { sections[it].key },
                    ) { index ->
                        val section = sections[index]
                        Column {
                            // Unstack button row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = sectionTitle(section),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { onUnstack(section.key) }) {
                                    Text(
                                        text = stringResource(HomeR.string.stack_unstack),
                                        fontSize = 12.sp,
                                        color = Color(0xFFE57373),
                                    )
                                }
                            }
                            // Section content
                            SectionContent(
                                section = section,
                                itemWidth = itemWidth,
                                isEditMode = false,
                                onAppClick = onAppClick,
                                onAppLongClick = onAppLongClick,
                                onFolderClick = onFolderClick,
                                onEnterEditMode = onEnterEditMode,
                                onDeleteCategory = onDeleteCategory,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun sectionTitle(section: HomeSection): String = when (section) {
    is HomeSection.Frequent -> stringResource(HomeR.string.section_frequent)
    is HomeSection.Category -> section.category.name
    is HomeSection.Recent -> stringResource(HomeR.string.section_recent)
    is HomeSection.New -> stringResource(HomeR.string.section_new)
    is HomeSection.Folders -> stringResource(HomeR.string.section_folders)
}

// ── Page Indicator (3 dots) ──

@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val widthFloat by animateFloatAsState(
                targetValue = if (isSelected) 16f else 6f,
                animationSpec = tween(300),
                label = "dot_w_$index",
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 0.8f else 0.3f,
                animationSpec = tween(300),
                label = "dot_a_$index",
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(widthFloat.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha)),
            )
        }
    }
}

// ── Swipeable Search Bar with direction arrows ──

@Composable
private fun SwipeableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    currentPage: Int = 1,
    pageCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 100.dp.toPx() }
    val velocityThreshold = 800f // px/s
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    // Direction hint arrows — compute threshold once
    val arrowThresholdPx = with(density) { 20.dp.toPx() }
    val leftArrowAlpha by animateFloatAsState(
        targetValue = if (accumulatedDrag > arrowThresholdPx) 0.6f else 0f,
        animationSpec = tween(150),
        label = "left_arrow",
    )
    val rightArrowAlpha by animateFloatAsState(
        targetValue = if (accumulatedDrag < -arrowThresholdPx) 0.6f else 0f,
        animationSpec = tween(150),
        label = "right_arrow",
    )

    val draggableState = rememberDraggableState { delta ->
        accumulatedDrag += delta
        scope.launch { offsetX.snapTo(accumulatedDrag * 0.4f) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageIndicator(currentPage = currentPage, pageCount = pageCount)

        Box(contentAlignment = Alignment.Center) {
            // Left arrow hint (swipe right → widget screen)
            Text(
                text = "‹",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = leftArrowAlpha),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-40).dp),
            )

            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        onDragStarted = {
                            accumulatedDrag = 0f
                        },
                        onDragStopped = { velocity ->
                            val absVelocity = abs(velocity)
                            if (accumulatedDrag < -swipeThresholdPx || (accumulatedDrag < 0 && absVelocity > velocityThreshold)) {
                                onSwipeLeft()
                            } else if (accumulatedDrag > swipeThresholdPx || (accumulatedDrag > 0 && absVelocity > velocityThreshold)) {
                                onSwipeRight()
                            }
                            accumulatedDrag = 0f
                            scope.launch {
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            }
                        },
                    ),
            )

            // Right arrow hint (swipe left → drawer)
            Text(
                text = "›",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = rightArrowAlpha),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp),
            )
        }
    }
}
