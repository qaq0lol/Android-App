package com.windscribe.mobile.ui.common

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stores an imported wallpaper in app-private storage, independent of photo-picker URI grants. */
object WallpaperStore {
    private const val PREFERENCES_NAME = "appearance_preferences"
    private const val WALLPAPER_PATH_KEY = "custom_wallpaper_path"
    private val mutableImagePath = MutableStateFlow<String?>(null)
    val imagePath: StateFlow<String?> = mutableImagePath.asStateFlow()
    @Volatile private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val storedPath = context.applicationContext
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(WALLPAPER_PATH_KEY, null)
            mutableImagePath.value = storedPath?.takeIf { File(it).isFile }
            initialized = true
        }
    }

    fun setImagePath(context: Context, path: String?) {
        initialize(context)
        val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences.edit().apply {
            if (path.isNullOrBlank()) remove(WALLPAPER_PATH_KEY) else putString(WALLPAPER_PATH_KEY, path)
        }.apply()
        mutableImagePath.value = path?.takeIf { File(it).isFile }
    }
}

@Composable
fun AppWallpaperBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val context = LocalContext.current
    WallpaperStore.initialize(context)
    val wallpaperPath by WallpaperStore.imagePath.collectAsState()
    val baseColor = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxSize().background(baseColor)) {
        wallpaperPath?.let { path ->
            if (File(path).isFile) {
                AsyncImage(
                    model = File(path),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.32f),
                                baseColor.copy(alpha = 0.49f),
                                baseColor.copy(alpha = 0.70f),
                            ),
                        ),
                    ),
                )
            }
        }
        content()
    }
}
