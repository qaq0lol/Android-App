package com.windscribe.mobile.ui.preferences.lipstick

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * High-performance GPU-backed Wallpaper Canvas.
 * Directly renders the complete, uncropped source bitmap using transformation matrix.
 * Eliminates Compose ContentScale pre-clipping and avoids ugly black voids.
 */
@Composable
fun WallpaperCanvas(
    bitmap: Bitmap?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotation: Int,
    darkness: Float = 0f,
    modifier: Modifier = Modifier,
    fillMode: Boolean = true, // true = Cover/Fill, false = Fit/Contain
) {
    if (bitmap == null) return

    val imageBitmap: ImageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bmpWidth = imageBitmap.width.toFloat()
            val bmpHeight = imageBitmap.height.toFloat()

            if (bmpWidth <= 0 || bmpHeight <= 0 || canvasWidth <= 0 || canvasHeight <= 0) return@Canvas

            // 1. Ambient Background Layer:
            // When foreground is smaller or in Fit mode, draw a darkened ambient extension
            // to eliminate harsh black letterboxes.
            val ambientScale = maxOf(canvasWidth / bmpWidth, canvasHeight / bmpHeight)
            val ambW = bmpWidth * ambientScale
            val ambH = bmpHeight * ambientScale
            val ambLeft = (canvasWidth - ambW) / 2f
            val ambTop = (canvasHeight - ambH) / 2f

            drawImage(
                image = imageBitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                dstOffset = IntOffset(ambLeft.roundToInt(), ambTop.roundToInt()),
                dstSize = IntSize(ambW.roundToInt(), ambH.roundToInt()),
                alpha = 0.30f
            )

            // Dim the ambient background
            drawRect(
                color = Color.Black.copy(alpha = 0.60f),
                size = size
            )

            // 2. Main Transformable Wallpaper:
            // Calculate base scale according to fillMode (Cover vs Contain)
            val baseScale = if (fillMode) {
                maxOf(canvasWidth / bmpWidth, canvasHeight / bmpHeight)
            } else {
                minOf(canvasWidth / bmpWidth, canvasHeight / bmpHeight)
            }

            val baseW = bmpWidth * baseScale
            val baseH = bmpHeight * baseScale
            val baseLeft = (canvasWidth - baseW) / 2f
            val baseTop = (canvasHeight - baseH) / 2f

            withTransform({
                translate(left = offsetX, top = offsetY)
                scale(scaleX = scale, scaleY = scale, pivot = center)
                rotate(degrees = rotation.toFloat(), pivot = center)
            }) {
                drawImage(
                    image = imageBitmap,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                    dstOffset = IntOffset(baseLeft.roundToInt(), baseTop.roundToInt()),
                    dstSize = IntSize(baseW.roundToInt(), baseH.roundToInt())
                )
            }

            // 3. User-configured Darkness Mask Overlay
            if (darkness > 0f) {
                drawRect(
                    color = Color.Black.copy(alpha = darkness.coerceIn(0f, 1f)),
                    size = size
                )
            }
        }
    }
}
