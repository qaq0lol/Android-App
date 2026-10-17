package com.windscribe.mobile.ui.theme

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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.windscribe.mobile.ui.helper.MultiDevicePreview

private val LocalMaterialYouDarkTheme = staticCompositionLocalOf { false }

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BB5FF), onPrimary = Color(0xFF092044),
    primaryContainer = Color(0xFF173765), onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFF59DED2), onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF124A47), onSecondaryContainer = Color(0xFFB5FFF6),
    tertiary = Color(0xFFBCA8FF), background = Color(0xFF08111F),
    onBackground = Color(0xFFEAF1FF), surface = Color(0xFF111D30),
    onSurface = Color(0xFFEAF1FF), surfaceVariant = Color(0xFF1D2B42),
    onSurfaceVariant = Color(0xFFB7C6DD), outline = Color(0xFF52647D),
    outlineVariant = Color(0xFF2C3E58), error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF245CD6), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE7FF), onPrimaryContainer = Color(0xFF12336F),
    secondary = Color(0xFF007F79), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1F5F0), onSecondaryContainer = Color(0xFF004D49),
    tertiary = Color(0xFF6750B5), background = Color(0xFFF3F6FC),
    onBackground = Color(0xFF14233A), surface = Color(0xFFFCFDFF),
    onSurface = Color(0xFF14233A), surfaceVariant = Color(0xFFE5ECF6),
    onSurfaceVariant = Color(0xFF4E5E76), outline = Color(0xFF788AA5),
    outlineVariant = Color(0xFFD4DEEC), error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
)

private val MaterialYouShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AndroidTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalMaterialYouDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography(), shapes = MaterialYouShapes, content = content)
    }
}

val ColorScheme.backgroundColor: Color @Composable get() = background
val ColorScheme.primaryTextColor: Color @Composable get() = onBackground
val ColorScheme.preferencesBackgroundColor: Color @Composable get() = background
val ColorScheme.preferencesSubtitleColor: Color @Composable get() = onSurfaceVariant
val ColorScheme.serverListBackgroundColor: Color @Composable get() = surface.copy(alpha = 0.88f)
val ColorScheme.serverItemTextColor: Color @Composable get() = onSurfaceVariant
val ColorScheme.serverListNavigationGradientEnd: Color @Composable get() = surface.copy(alpha = 0.90f)
val ColorScheme.serverListSecondaryColor: Color @Composable get() = onSurface
val ColorScheme.expandedServerItemTextColor: Color @Composable get() = onSurfaceVariant
val ColorScheme.isDark: Boolean @Composable get() = LocalMaterialYouDarkTheme.current

@Composable
fun ColorScheme.colorForTheme(dark: Color, light: Color): Color =
    if (LocalMaterialYouDarkTheme.current) dark else light

@Composable
@MultiDevicePreview
fun ThemeColorPreview() {
    AndroidTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.backgroundColor)) {
            Text("Windscribe", color = MaterialTheme.colorScheme.primaryTextColor, modifier = Modifier.align(Alignment.Center))
        }
    }
}
