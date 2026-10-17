package com.windscribe.mobile.ui.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.windscribe.mobile.ui.helper.ForceStatusBarIcons

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    ForceStatusBarIcons()
    AppWallpaperBackground(modifier = Modifier.fillMaxSize(), content = content)
}

@Composable
fun PreferenceBackground(content: @Composable BoxScope.() -> Unit) {
    ForceStatusBarIcons()
    AppWallpaperBackground(modifier = Modifier.fillMaxSize(), content = content)
}
