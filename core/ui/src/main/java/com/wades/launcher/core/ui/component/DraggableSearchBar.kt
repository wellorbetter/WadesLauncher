package com.wades.launcher.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { 400.dp.toPx() }
    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(0f) }
    var isExpanded by remember { mutableStateOf(false) }

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            val newValue = (offsetY.value + delta).coerceIn(-expandedHeightPx, 0f)
            offsetY.snapTo(newValue)
        }
    }

    fun snapToAnchor() {
        scope.launch {
            val threshold = -expandedHeightPx * 0.3f
            if (offsetY.value < threshold) {
                offsetY.animateTo(
                    targetValue = -expandedHeightPx,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
                isExpanded = true
            } else {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
                isExpanded = false
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 72.dp),
                ) {
                    expandedContent()
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { snapToAnchor() },
                )
                .padding(bottom = 16.dp),
        ) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
            )
        }
    }
}
