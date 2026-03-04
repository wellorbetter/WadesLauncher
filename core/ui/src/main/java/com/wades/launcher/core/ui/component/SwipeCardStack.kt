package com.wades.launcher.core.ui.component

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Tinder-style swipeable card stack.
 *
 * Cards stack from the left. The right edge shows peeking layers.
 * The top card can be swiped right to dismiss, revealing the next card.
 */
@Composable
fun <T> SwipeCardStack(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxVisibleCards: Int = 3,
    cardOffset: Dp = 8.dp,
    swipeThreshold: Dp = 100.dp,
    onSwiped: (T) -> Unit = {},
    cardContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) return

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val thresholdPx = with(density) { swipeThreshold.toPx() }
    val cardOffsetPx = with(density) { cardOffset.toPx() }

    val currentIndex = remember { mutableIntStateOf(0) }

    // Top card drag state
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val remaining = items.size - currentIndex.intValue
    if (remaining <= 0) return

    val visibleCount = minOf(maxVisibleCards, remaining)

    Box(modifier = modifier.fillMaxWidth()) {
        // Left-side "thickness" shadow (book spine effect)
        repeat(minOf(3, remaining - 1)) { i ->
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = -(i + 1).dp)
                    .zIndex(-10f - i)
                    .graphicsLayer { alpha = 0.15f - i * 0.04f }
                    .background(Color.White.copy(alpha = 0.1f)),
            )
        }

        // Render cards from back to front
        for (reverseIdx in (visibleCount - 1) downTo 0) {
            val itemIdx = currentIndex.intValue + reverseIdx
            if (itemIdx >= items.size) continue
            val item = items[itemIdx]
            val isTop = reverseIdx == 0

            val stackTranslateX = reverseIdx * cardOffsetPx
            val stackAlpha = 1f - reverseIdx * 0.15f

            key(itemIdx) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex((visibleCount - reverseIdx).toFloat())
                        .then(
                            if (isTop) {
                                Modifier
                                    .offset {
                                        IntOffset(
                                            offsetX.value.roundToInt(),
                                            offsetY.value.roundToInt(),
                                        )
                                    }
                                    .graphicsLayer {
                                        rotationZ = offsetX.value / 25f
                                    }
                                    .pointerInput(currentIndex.intValue) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                scope.launch {
                                                    if (offsetX.value.absoluteValue > thresholdPx) {
                                                        // Fly out
                                                        val targetX =
                                                            if (offsetX.value > 0) screenWidthPx * 1.5f
                                                            else -screenWidthPx * 1.5f
                                                        launch { offsetX.animateTo(targetX, tween(200)) }
                                                        launch { offsetY.animateTo(offsetY.value * 1.5f, tween(200)) }
                                                        onSwiped(item)
                                                        currentIndex.intValue++
                                                        offsetX.snapTo(0f)
                                                        offsetY.snapTo(0f)
                                                    } else {
                                                        // Spring back
                                                        launch {
                                                            offsetX.animateTo(
                                                              0f,
                                                                spring(
                                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                    stiffness = Spring.StiffnessMedium,
                                                                ),
                                                            )
                                                        }
                                                        launch {
                                                            offsetY.animateTo(
                                                                0f,
                                                                spring(
                                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                    stiffness = Spring.StiffnessMedium,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                scope.launch {
                                                    launch { offsetX.animateTo(0f, spring()) }
                                                    launch { offsetY.animateTo(0f, spring()) }
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                scope.launch {
                                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                                }
                                            },
                                        )
                                    }
                            } else {
                                Modifier.graphicsLayer {
                                    translationX = stackTranslateX
                                    alpha = stackAlpha
                                }
                            },
                        )
                        .padding(end = (maxVisibleCards * cardOffset.value).dp),
                ) {
                    cardContent(item)
                }
            }
        }
    }
}
