package com.windscribe.mobile.ui.nav

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

typealias EnterTransform = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
typealias ExitTransform = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

/** Smooth, cyber-style fade transitions shared across all navigation screens */
val fadeEnter: EnterTransform = {
    fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)) +
    scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = LinearOutSlowInEasing))
}
val fadeExit: ExitTransform = {
    fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)) +
    scaleOut(targetScale = 0.98f, animationSpec = tween(180, easing = FastOutLinearInEasing))
}
val fadePopEnter: EnterTransform = {
    fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)) +
    scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = LinearOutSlowInEasing))
}
val fadePopExit: ExitTransform = {
    fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)) +
    scaleOut(targetScale = 0.98f, animationSpec = tween(180, easing = FastOutLinearInEasing))
}

/**
 * Standard composable with smooth fade-in/fade-out transitions.
 */
fun NavGraphBuilder.slidingComposable(
    route: String,
    enterTransition: EnterTransform = fadeEnter,
    exitTransition: ExitTransform = fadeExit,
    popEnterTransition: EnterTransform = fadePopEnter,
    popExitTransition: ExitTransform = fadePopExit,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    content = content,
)
