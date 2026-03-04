package com.wades.launcher.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Pull-down panel that hides above the screen.
 *
 * A small handle bar sits at the top. Dragging it down reveals the panel content
 * (like pulling a curtain). Releasing past 30% of screen height auto-expands;
 * otherwise it springs back. Tapping the scrim behind the panel collapses it.
 */
@Composable
fun PullDownPanel(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    handleHeight: Dp = 28.dp,
    panelContent: @Composable () -> Unit,
    behindContent: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightPx = with(density) { screenHeightDp.toPx() }
    val expandThreshold = screenHeightPx * 0.3f

    // Panel offset: 0 = fully expanded (panel visible), -screenHeightPx = fully hidden
    val panelOffset = remember { Animatable(-screenHeightPx) }
    var panelHeightPx by remember { mutableFloatStateOf(screenHeightPx) }

    // Sync with external isExpanded state
    androidx.compose.runtime.LaunchedEffect(isExpanded) {
        val target = if (isExpanded) 0f else -panelHeightPx
        panelOffset.animateTo(
            target,
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main content behind the panel
        behindContent()

        // Handle bar at the very top — always visible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(handleHeight)
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            scope.launch {
                                // Current drag distance from hidden position
                                val dragDistance = panelOffset.value + panelHeightPx
                                if (dragDistance > expandThreshold) {
                                    // Expand
                                    panelOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    onExpandedChange(true)
                                } else {
                                    // Collapse
                                    panelOffset.animateTo(-panelHeightPx, spring(stiffness = Spring.StiffnessMediumLow))
                                    onExpandedChange(false)
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                panelOffset.animateTo(-panelHeightPx, spring())
                                onExpandedChange(false)
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount > 0 || panelOffset.value > -panelHeightPx) {
                                change.consume()
                                scope.launch {
                                    val newOffset = (panelOffset.value + dragAmount)
                                        .coerceIn(-panelHeightPx, 0f)
                                    panelOffset.snapTo(newOffset)
                                }
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            // Rope handle: vertical line + small ball
            val ropeProgress = ((panelOffset.value + panelHeightPx) / panelHeightPx).coerceIn(0f, 1f)
            val lineHeight = (16 + 16 * ropeProgress).dp // 16dp → 32dp as panel opens
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(lineHeight)
                        .background(Color.White.copy(alpha = 0.2f)),
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f)),
                )
            }
        }

        // Scrim overlay — visible when panel is partially/fully open
        val progress = ((panelOffset.value + panelHeightPx) / panelHeightPx).coerceIn(0f, 1f)
        if (progress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress * 0.6f }
                    .background(Color.Black)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        // Tap scrim to collapse
                        scope.launch {
                            panelOffset.animateTo(-panelHeightPx, tween(300))
                            onExpandedChange(false)
                        }
                    },
            )
        }

        // The panel itself — slides down from above
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { panelHeightPx = it.size.height.toFloat() }
                .offset { IntOffset(0, panelOffset.value.roundToInt()) }
                .background(Color.Black.copy(alpha = 0.92f))
                .pointerInput(Unit) {
                    // Panel content can also be dragged up to collapse
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val dragDistance = panelOffset.value + panelHeightPx
                                if (dragDistance < panelHeightPx * 0.7f) {
                                    panelOffset.animateTo(-panelHeightPx, spring(stiffness = Spring.StiffnessMediumLow))
                                    onExpandedChange(false)
                                } else {
                                    panelOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount < 0 || panelOffset.value < 0f) {
                                change.consume()
                                scope.launch {
                                    val newOffset = (panelOffset.value + dragAmount)
                                        .coerceIn(-panelHeightPx, 0f)
                                    panelOffset.snapTo(newOffset)
                                }
                            }
                        },
                    )
                },
        ) {
            panelContent()
        }
    }
}
