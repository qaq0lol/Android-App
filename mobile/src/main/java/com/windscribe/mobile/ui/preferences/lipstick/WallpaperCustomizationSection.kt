package com.windscribe.mobile.ui.preferences.lipstick

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.WallpaperStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WallpaperCustomizationSection() {
    val context = LocalContext.current
    WallpaperStore.initialize(context)
    val currentPath by WallpaperStore.imagePath.collectAsState()
    val scope = rememberCoroutineScope()
    var isImporting by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && !isImporting) {
            scope.launch {
                isImporting = true
                try {
                    val importedFile = withContext(Dispatchers.IO) { persistWallpaper(context, uri) }
                    val previousPath = currentPath
                    WallpaperStore.setImagePath(context, importedFile.absolutePath)
                    if (previousPath != null && previousPath != importedFile.absolutePath) {
                        withContext(Dispatchers.IO) { File(previousPath).delete() }
                    }
                    Toast.makeText(context, context.getString(R.string.wallpaper_applied), Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, context.getString(R.string.wallpaper_error), Toast.LENGTH_LONG).show()
                } finally {
                    isImporting = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.wallpaper_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.wallpaper_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (currentPath != null && File(currentPath!!).isFile) {
                AsyncImage(
                    model = File(currentPath!!),
                    contentDescription = stringResource(R.string.wallpaper_title),
                    modifier = Modifier.fillMaxWidth().height(148.dp).clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePicker.launch("image/*") }, enabled = !isImporting) {
                    Text(stringResource(if (isImporting) R.string.wallpaper_importing else R.string.wallpaper_choose))
                }
                if (currentPath != null) {
                    TextButton(
                        onClick = {
                            val oldPath = currentPath
                            WallpaperStore.setImagePath(context, null)
                            oldPath?.let { File(it).delete() }
                        },
                        enabled = !isImporting,
                    ) { Text(stringResource(R.string.wallpaper_reset)) }
                }
            }
        }
    }
}

private fun persistWallpaper(context: Context, uri: Uri): File {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val boundsStream = resolver.openInputStream(uri) ?: throw IOException("Unable to open image")
    boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || bounds.outWidth > 30000 || bounds.outHeight > 30000) {
        throw IOException("Unsupported image dimensions")
    }

    val extension = when (resolver.getType(uri)?.lowercase()) {
        "image/jpeg", "image/jpg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "img"
    }
    val tempFile = File(context.filesDir, "custom_wallpaper.tmp")
    val targetFile = File(context.filesDir, "custom_wallpaper_${System.currentTimeMillis()}.$extension")
    try {
        val input = resolver.openInputStream(uri) ?: throw IOException("Unable to read image")
        input.use { source ->
            FileOutputStream(tempFile).use { target ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0L
                while (true) {
                    val count = source.read(buffer)
                    if (count < 0) break
                    totalBytes += count
                    if (totalBytes > 25L * 1024L * 1024L) throw IOException("Image is larger than 25 MB")
                    target.write(buffer, 0, count)
                }
                target.fd.sync()
            }
        }
        if (tempFile.length() == 0L) throw IOException("Empty image file")
        if (!tempFile.renameTo(targetFile)) {
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
        }
        return targetFile
    } finally {
        tempFile.delete()
    }
}
