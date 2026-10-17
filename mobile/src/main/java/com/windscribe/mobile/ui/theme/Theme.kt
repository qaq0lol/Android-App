package com.windscribe.mobile.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.windscribe.mobile.ui.helper.MultiDevicePreview

private val LocalMaterialYouDarkTheme = staticCompositionLocalOf { false }

val DarkColorScheme =
    darkColorScheme(
        primary = AppColors.primaryBlue,
        secondary = AppColors.mediumBlue,
        tertiary = AppColors.cyberBlue,
        background = AppColors.deepBlue,
        surface = AppColors.charcoalBlue,
        onPrimary = AppColors.white,
        onBackground = AppColors.white,
        onSurface = AppColors.white,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = AppColors.primaryBlue,
        secondary = AppColors.mediumBlue,
        tertiary = AppColors.cyberBlue,
        background = Color(0xFFF7F2FA),
        surface = Color(0xFFFFFBFE),
        onPrimary = AppColors.white,
        onBackground = Color(0xFF1D1B20),
        onSurface = Color(0xFF1D1B20),
    )

private val MaterialYouShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

@Composable
fun AndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme =
        when {
            supportsDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
            supportsDynamicColor -> dynamicLightColorScheme(context)
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    CompositionLocalProvider(LocalMaterialYouDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            shapes = MaterialYouShapes,
            content = content,
        )
    }
}

val ColorScheme.backgroundColor: Color
    @Composable get() = background

val ColorScheme.primaryTextColor: Color
    @Composable get() = onBackground

val ColorScheme.preferencesBackgroundColor: Color
    @Composable get() = background

val ColorScheme.preferencesSubtitleColor: Color
    @Composable get() = onSurfaceVariant

val ColorScheme.serverListBackgroundColor: Color
    @Composable get() = surface

val ColorScheme.serverItemTextColor: Color
    @Composable get() = onSurfaceVariant

val ColorScheme.serverListNavigationGradientEnd: Color
    @Composable get() = surface.copy(alpha = 0.90f)

val ColorScheme.serverListSecondaryColor: Color
    @Composable get() = onSurface

val ColorScheme.expandedServerItemTextColor: Color
    @Composable get() = onSurfaceVariant

val ColorScheme.isDark: Boolean
    @Composable get() = LocalMaterialYouDarkTheme.current

@Composable
fun ColorScheme.colorForTheme(
    dark: Color,
    light: Color,
): Color = if (LocalMaterialYouDarkTheme.current) dark else light

@Composable
@MultiDevicePreview
fun ThemeColorPreview() {
    AndroidTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.backgroundColor),
        ) {
            Text(
                text = "Material You",
                color = MaterialTheme.colorScheme.primaryTextColor,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
