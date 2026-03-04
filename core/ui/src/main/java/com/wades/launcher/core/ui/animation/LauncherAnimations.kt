package com.wades.launcher.core.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

object LauncherAnimations {

    val pageEnter: EnterTransition = fadeIn(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )

    val pageExit: ExitTransition = fadeOut(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
    )

    val iconPressScale = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    val groupExpandEnter: EnterTransition = fadeIn(
        animationSpec = tween(200),
    ) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(200),
    )

    val groupExpandExit: ExitTransition = fadeOut(
        animationSpec = tween(150),
    ) + scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(150),
    )

    val drawerSlideIn: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300),
    )

    val drawerSlideOut: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300),
    )
}
