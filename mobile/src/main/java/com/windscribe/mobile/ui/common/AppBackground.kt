package com.windscribe.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.windscribe.mobile.ui.helper.ForceStatusBarIcons
import com.windscribe.mobile.ui.theme.AppColors
import com.windscribe.mobile.ui.theme.preferencesBackgroundColor

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    ForceStatusBarIcons()
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0A1016), Color(0xFF04060A)))),
    ) {
        content()
    }
}

@Composable
fun PreferenceBackground(content: @Composable BoxScope.() -> Unit) {
    ForceStatusBarIcons()
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0A1016), Color(0xFF04060A)))),
    ) {
        content()
    }
}
