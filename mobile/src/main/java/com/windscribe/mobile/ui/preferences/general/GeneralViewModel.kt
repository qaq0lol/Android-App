package com.windscribe.mobile.ui.preferences.general

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windscribe.mobile.BuildConfig
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.vpn.R
import com.windscribe.vpn.Windscribe.Companion.appContext
import com.windscribe.vpn.apppreference.PreferencesHelper
import com.windscribe.vpn.commonutils.WindUtilities
import com.windscribe.vpn.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class GeneralViewModel : ViewModel() {
    abstract val orderByItems: List<DropDownStringItem>

    abstract fun onOrderByItemSelected(item: DropDownStringItem)

    abstract val selectedOrderBy: StateFlow<String>

    abstract val languageItems: List<DropDownStringItem>

    abstract fun onLanguageItemSelected(item: DropDownStringItem)

    abstract val selectedLanguage: String

    abstract val reloadApp: SharedFlow<Boolean>
    abstract val isHapticEnabled: StateFlow<Boolean>

    abstract fun onHapticToggleButtonClicked()

    abstract fun onLocationLoadItemClicked()

    abstract val isLocationLoadEnabled: StateFlow<Boolean>

    abstract val isNotificationStatEnabled: StateFlow<Boolean>

    abstract fun onNotificationStatEnabledClick()

    abstract val isAutoReconnectEnabled: StateFlow<Boolean>

    abstract fun onAutoReconnectClicked()

    abstract val isKeepAliveEnabled: StateFlow<Boolean>

    abstract fun onKeepAliveClicked(context: Context)
    abstract fun refreshKeepAliveStatus(context: Context)

    abstract val versionName: String
    abstract val buildChannel: String
    abstract val compileTarget: String
    abstract val coreArch: String
}

@HiltViewModel
class GeneralViewModelImpl
    @Inject
    constructor(
        private val preferenceHelper: PreferencesHelper,
        private val userRepository: UserRepository,
    ) : GeneralViewModel() {
        private val _reloadApp = MutableSharedFlow<Boolean>()
        override val reloadApp: SharedFlow<Boolean> = _reloadApp

        override val orderByItems: List<DropDownStringItem>
            get() =
                with(appContext.resources) {
                    val values = getStringArray(R.array.order_list)
                    val keys = getStringArray(R.array.order_list_keys)
                    keys.zip(values) { key, value -> DropDownStringItem(key, value) }
                }

        private val _selectedOrderBy = MutableStateFlow(preferenceHelper.selection)
        override val selectedOrderBy: StateFlow<String> = _selectedOrderBy

        override fun onOrderByItemSelected(item: DropDownStringItem) {
            preferenceHelper.selection = item.key
            _selectedOrderBy.value = item.key
        }

        override val languageItems: List<DropDownStringItem>
            get() =
                with(appContext.resources) {
                    val entries = getStringArray(R.array.language)
                    entries.zip(entries) { key, value -> DropDownStringItem(key, value) }
                }

        override val selectedLanguage: String
            get() = preferenceHelper.savedLanguage

        override fun onLanguageItemSelected(item: DropDownStringItem) {
            viewModelScope.launch {
                preferenceHelper.savedLanguage = item.key
                _reloadApp.emit(true)
            }
        }

        private val _isLocationLoadEnabled = MutableStateFlow(preferenceHelper.isShowLocationHealthEnabled)
        override val isLocationLoadEnabled: StateFlow<Boolean> = _isLocationLoadEnabled

        override fun onLocationLoadItemClicked() {
            viewModelScope.launch {
                val updatedValue = !_isLocationLoadEnabled.value
                preferenceHelper.isShowLocationHealthEnabled = updatedValue
                _isLocationLoadEnabled.value = updatedValue
            }
        }

        private val _isHapticEnabled = MutableStateFlow(preferenceHelper.isHapticFeedbackEnabled)
        override val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled

        override fun onHapticToggleButtonClicked() {
            val newValue = !_isHapticEnabled.value
            preferenceHelper.isHapticFeedbackEnabled = newValue
            _isHapticEnabled.value = newValue
        }

        private val _isNotificationStatEnabled = MutableStateFlow(preferenceHelper.notificationStat)
        override val isNotificationStatEnabled: StateFlow<Boolean> = _isNotificationStatEnabled

        override fun onNotificationStatEnabledClick() {
            val newValue = !_isNotificationStatEnabled.value
            preferenceHelper.notificationStat = newValue
            _isNotificationStatEnabled.value = newValue
        }

        private val _isAutoReconnectEnabled = MutableStateFlow(preferenceHelper.autoConnect)
        override val isAutoReconnectEnabled: StateFlow<Boolean> = _isAutoReconnectEnabled

        override fun onAutoReconnectClicked() {
            val updated = !_isAutoReconnectEnabled.value
            preferenceHelper.autoConnect = updated
            _isAutoReconnectEnabled.value = updated
        }

        private val _isKeepAliveEnabled = MutableStateFlow(
            (appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager)
                ?.isIgnoringBatteryOptimizations(appContext.packageName) == true
        )
        override val isKeepAliveEnabled: StateFlow<Boolean> = _isKeepAliveEnabled

        override fun refreshKeepAliveStatus(context: Context) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            _isKeepAliveEnabled.value = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        }

        override fun onKeepAliveClicked(context: Context) {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                if (!isIgnoring) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } else {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            } catch (_: Exception) {
            }
        }

        override val versionName: String
            get() = "${WindUtilities.getVersionName()} · ${BuildConfig.VERSION_CODE}"

        override val buildChannel: String
            get() = "${BuildConfig.FLAVOR} / ${BuildConfig.BUILD_TYPE}"

        override val compileTarget: String
            get() = "Android 16（API 36）"

        override val coreArch: String
            get() = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    }
