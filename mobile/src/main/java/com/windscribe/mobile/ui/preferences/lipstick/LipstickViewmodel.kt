package com.windscribe.mobile.ui.preferences.lipstick

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.model.ThemeItem
import com.windscribe.mobile.ui.preferences.icons.AppIconManager
import com.windscribe.vpn.Windscribe.Companion.appContext
import com.windscribe.vpn.apppreference.PreferencesHelper
import com.windscribe.vpn.repository.ServerListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

data class GlobalBackgroundConfig(
    val hasBackground: Boolean = false,
    val bitmap: Bitmap? = null,
    val scale: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Int = 0,
    val darkness: Float = 0.15f,
)

abstract class LipstickViewmodel : ViewModel() {
    abstract val globalBackgroundConfig: StateFlow<GlobalBackgroundConfig>
    abstract fun setGlobalBackgroundFromUri(context: Context, fileUri: Uri)
    abstract fun updateGlobalBackgroundTransform(scale: Float, offsetX: Float, offsetY: Float, rotation: Int)
    abstract fun updateGlobalBackgroundDarkness(darkness: Float)
    abstract fun resetGlobalBackgroundPosition()
    abstract fun saveAndApplyGlobalBackground()
    abstract fun cancelGlobalBackgroundEdit()
    abstract fun clearGlobalBackground(context: Context)

    abstract fun loadServerListFile(
        context: Context,
        fileUri: Uri,
    )

    abstract fun onResetClick()

    abstract fun exportServerListFile(
        context: Context,
        uri: Uri,
    )

    abstract val toastMessage: StateFlow<ToastMessage>

    abstract fun clearToast()

    abstract fun onThemeItemSelected(theme: ThemeItem)

    abstract val themeItem: StateFlow<ThemeItem>
    abstract val selectedAppIcon: StateFlow<Int>
}

@HiltViewModel
class LipstickViewmodelImpl
    @Inject
    constructor(
        private val preferenceHelper: PreferencesHelper,
        private val serverListRepository: ServerListRepository,
        private val appIconManager: AppIconManager,
    ) : LipstickViewmodel() {
        private val _globalBackgroundConfig = MutableStateFlow(GlobalBackgroundConfig())
        override val globalBackgroundConfig: StateFlow<GlobalBackgroundConfig> = _globalBackgroundConfig

        private val _toastMessage = MutableStateFlow<ToastMessage>(ToastMessage.None)
        override val toastMessage: StateFlow<ToastMessage> = _toastMessage
        private val _themeItem = MutableStateFlow(LookAndFeelHelper.getThemeOptions().first())
        override val themeItem: StateFlow<ThemeItem> = _themeItem
        private val _selectedAppIcon = MutableStateFlow(com.windscribe.vpn.R.mipmap.ws_launcher)
        override val selectedAppIcon: StateFlow<Int> = _selectedAppIcon
        private val logger = LoggerFactory.getLogger("LipstickViewmodel")

        private var savedSnapshot: GlobalBackgroundConfig? = null
        private var pendingNewBitmap: Bitmap? = null

        init {
            loadGlobalBackground()
            loadThemePreferences()
            observeSelectedAppIcon()
        }

        private fun loadGlobalBackground() {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    if (preferenceHelper.hasGlobalBackground) {
                        val file = File(appContext.filesDir, "global_background.png")
                        val bitmap = if (file.exists()) {
                            BitmapFactory.decodeFile(file.absolutePath)
                        } else null
                        val config = GlobalBackgroundConfig(
                            hasBackground = bitmap != null,
                            bitmap = bitmap,
                            scale = preferenceHelper.globalBackgroundScale,
                            offsetX = preferenceHelper.globalBackgroundOffsetX,
                            offsetY = preferenceHelper.globalBackgroundOffsetY,
                            rotation = preferenceHelper.globalBackgroundRotation,
                            darkness = preferenceHelper.globalBackgroundDarkness
                        )
                        _globalBackgroundConfig.value = config
                        savedSnapshot = config
                    } else {
                        val config = GlobalBackgroundConfig()
                        _globalBackgroundConfig.value = config
                        savedSnapshot = config
                    }
                } catch (e: Exception) {
                    logger.error("Error loading global background: ${e.message}")
                }
            }
        }

        override fun setGlobalBackgroundFromUri(context: Context, fileUri: Uri) {
            if (savedSnapshot == null) {
                savedSnapshot = _globalBackgroundConfig.value
            }
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // 1. Detect EXIF orientation to correct camera photos
                    var exifOrientation = ExifInterface.ORIENTATION_NORMAL
                    try {
                        context.contentResolver.openInputStream(fileUri)?.use { stream ->
                            val exif = ExifInterface(stream)
                            exifOrientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                        }
                    } catch (e: Exception) {
                        logger.warn("Could not read EXIF orientation: ${e.message}")
                    }

                    // 2. Decode bounds & calculate sample size
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(fileUri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    val maxSize = 2560
                    var sampleSize = 1
                    while (options.outWidth / sampleSize > maxSize || options.outHeight / sampleSize > maxSize) {
                        sampleSize *= 2
                    }
                    options.inJustDecodeBounds = false
                    options.inSampleSize = sampleSize
                    val rawBitmap = context.contentResolver.openInputStream(fileUri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }

                    // 3. Apply orientation rotation if needed
                    val finalBitmap = if (rawBitmap != null && exifOrientation != ExifInterface.ORIENTATION_NORMAL) {
                        val matrix = Matrix()
                        when (exifOrientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                        }
                        try {
                            val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                            if (rotated != rawBitmap) {
                                rawBitmap.recycle()
                            }
                            rotated
                        } catch (_: Exception) {
                            rawBitmap
                        }
                    } else {
                        rawBitmap
                    }

                    finalBitmap?.let {
                        pendingNewBitmap = it
                        _globalBackgroundConfig.value = _globalBackgroundConfig.value.copy(
                            hasBackground = true,
                            bitmap = it,
                            scale = 1.0f,
                            offsetX = 0f,
                            offsetY = 0f,
                            rotation = 0,
                            darkness = preferenceHelper.globalBackgroundDarkness
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error setting global background: ${e.message}")
                }
            }
        }

        override fun updateGlobalBackgroundTransform(scale: Float, offsetX: Float, offsetY: Float, rotation: Int) {
            if (savedSnapshot == null) {
                savedSnapshot = _globalBackgroundConfig.value
            }
            _globalBackgroundConfig.value = _globalBackgroundConfig.value.copy(
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                rotation = rotation
            )
        }

        override fun updateGlobalBackgroundDarkness(darkness: Float) {
            if (savedSnapshot == null) {
                savedSnapshot = _globalBackgroundConfig.value
            }
            _globalBackgroundConfig.value = _globalBackgroundConfig.value.copy(
                darkness = darkness
            )
        }

        override fun resetGlobalBackgroundPosition() {
            if (savedSnapshot == null) {
                savedSnapshot = _globalBackgroundConfig.value
            }
            _globalBackgroundConfig.value = _globalBackgroundConfig.value.copy(
                scale = 1.0f,
                offsetX = 0f,
                offsetY = 0f,
                rotation = 0
            )
        }

        override fun saveAndApplyGlobalBackground() {
            val current = _globalBackgroundConfig.value
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    pendingNewBitmap?.let { bmp ->
                        appContext.openFileOutput("global_background.png", Context.MODE_PRIVATE).use { output ->
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
                        }
                        bmp.recycle()
                        pendingNewBitmap = null
                    }
                } catch (e: Exception) {
                    logger.error("Error saving global background image: ${e.message}")
                }
                preferenceHelper.hasGlobalBackground = current.hasBackground
                preferenceHelper.globalBackgroundPath = if (current.hasBackground) "global_background.png" else null
                preferenceHelper.globalBackgroundScale = current.scale
                preferenceHelper.globalBackgroundOffsetX = current.offsetX
                preferenceHelper.globalBackgroundOffsetY = current.offsetY
                preferenceHelper.globalBackgroundRotation = current.rotation
                preferenceHelper.globalBackgroundDarkness = current.darkness
                savedSnapshot = current
                showToast("✓ 已保存并应用至全局界面！")
            }
        }

        override fun cancelGlobalBackgroundEdit() {
            savedSnapshot?.let {
                _globalBackgroundConfig.value = it
            }
            pendingNewBitmap = null
        }

        override fun clearGlobalBackground(context: Context) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val file = File(context.filesDir, "global_background.png")
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    logger.error("Error deleting global background file: ${e.message}")
                }
                preferenceHelper.hasGlobalBackground = false
                preferenceHelper.globalBackgroundPath = null
                pendingNewBitmap = null
                val resetConfig = GlobalBackgroundConfig(
                    hasBackground = false,
                    bitmap = null,
                    scale = 1.0f,
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0,
                    darkness = 0.15f
                )
                _globalBackgroundConfig.value = resetConfig
                savedSnapshot = resetConfig
                showToast("已恢复默认背景")
            }
        }

        private fun loadThemePreferences() {
            _themeItem.value = LookAndFeelHelper
                .getThemeOptions()
                .firstOrNull { it.id == preferenceHelper.selectedTheme }
                ?: LookAndFeelHelper.getThemeOptions().first()
        }

        override fun loadServerListFile(
            context: Context,
            fileUri: Uri,
        ) {
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val jsonString = inputStream?.bufferedReader().use { it?.readText() }
                if (jsonString != null) {
                    serverListRepository.saveLocationsJson(jsonString)
                    showToast("Imported successfully!")
                } else {
                    showToast("Failed to read file")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }

        override fun exportServerListFile(
            context: Context,
            uri: Uri,
        ) {
            val jsonData = serverListRepository.locationJsonToExport.value
            if (jsonData.isEmpty()) {
                return
            }
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonData.toByteArray())
                    showToast("Exported successfully!")
                }
            } catch (e: Exception) {
                showToast("Failed to export: ${e.message}")
            }
        }

        override fun onResetClick() {
            serverListRepository.deleteLocationsJson()
            showToast("Server list reset successfully!")
        }

        private fun observeSelectedAppIcon() {
            viewModelScope.launch {
                appIconManager.selectedAppIcon.collect { appIcon ->
                    if (appIcon != null) {
                        _selectedAppIcon.value = appIcon.icon
                    }
                }
            }
        }

        private fun showToast(message: String) {
            viewModelScope.launch {
                _toastMessage.emit(ToastMessage.Raw(message))
            }
        }

        override fun clearToast() {
            _toastMessage.value = ToastMessage.None
        }

        override fun onThemeItemSelected(theme: ThemeItem) {
            _themeItem.value = theme
            preferenceHelper.selectedTheme = theme.id
            appContext.applicationInterface.setTheme()
        }
    }
