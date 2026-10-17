package com.windscribe.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * iOS-inspired palette: restrained system blue, near-black surfaces, and readable secondary text.
 * Keep semantic colors (success, warning, error) distinct for connection-state feedback.
 */
object AppColors {
    // Core surfaces and system blue
    val deepBlue = Color(0xFF0B0B0D)
    var charcoalBlue = Color(0xFF1C1C1E)
    val midnightNavy = Color(0xFF000000)
    val mediumBlue = Color(0xFF5E9BFF)
    val primaryBlue = Color(0xFF0A84FF)
    val darkBlueAccent = Color(0xFF006EDB)
    val cyberBlue = Color(0xFF64D2FF)

    // Text and subtle separators
    val white = Color(0xFFFFFFFF)
    val slateGray = Color(0xFF8E8E93)
    val grayText = Color(0xFFAEAEB2)
    val tabGradientTop = Color(0x33FFFFFF)
    val tabGradientBottom = Color(0x1FFFFFFF)

    // Connection status
    val green = Color(0xFF123D2A)
    val neonGreen = Color(0xFF30D158)
    val actionGreen = Color(0xFF30D158)
    val mintGreen = Color(0xFF30D158)

    // Semantic and utility colors
    val yellow = Color(0xFFFFD60A)
    val red = Color(0xFFFF453A)
    val black = Color(0xFF000000)
    val midnight = Color(0xFF000000)
}
