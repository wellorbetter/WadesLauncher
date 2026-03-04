package com.wades.launcher.core.ui.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Consumes leftover horizontal scroll when the LazyRow still has scrollable content,
 * preventing it from leaking to an outer HorizontalPager.
 */
fun Modifier.consumeHorizontalScroll(state: LazyListState): Modifier =
    this.nestedScroll(object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (state.canScrollForward || state.canScrollBackward) {
                return available.copy(y = 0f)
            }
            return Offset.Zero
        }
    })
