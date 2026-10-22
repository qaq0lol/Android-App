package com.windscribe.mobile.ui.preferences.anticensorship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.vpn.apppreference.PreferencesHelper
import com.windscribe.vpn.apppreference.PreferencesKeyConstants
import com.windscribe.vpn.decoytraffic.DecoyTrafficController
import com.windscribe.vpn.decoytraffic.FakeTrafficVolume
import com.windscribe.vpn.repository.UnblockWgParamsRepository
import com.windscribe.vpn.state.VPNConnectionStateManager
import com.windscribe.vpn.workers.WindScribeWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class AntiCensorshipViewModel : ViewModel() {
    abstract val protocolTweaksModes: StateFlow<List<DropDownStringItem>>
    abstract val selectedProtocolTweaksMode: StateFlow<String>
    abstract val amneziaPresets: StateFlow<List<DropDownStringItem>>
    abstract val selectedPreset: StateFlow<String>
    abstract val extraTlsPaddingEnabled: StateFlow<Boolean>
    abstract val serverRoutingModes: StateFlow<List<DropDownStringItem>>
    abstract val selectedServerRouting: StateFlow<String>
    abstract val tlsServerName: StateFlow<String>
    abstract val decoyTraffic: StateFlow<Boolean>
    abstract val trafficMultiplier: StateFlow<String>
    abstract val trafficMultipliers: StateFlow<List<DropDownStringItem>>

    abstract fun onProtocolTweaksModeSelected(mode: String)

    abstract fun onAmneziaPresetSelected(presetId: String)

    abstract fun onExtraTlsPaddingToggled()

    abstract fun onServerRoutingSelected(mode: String)

    abstract fun onTlsServerNameChanged(value: String)

    abstract fun onDecoyTrafficToggled()

    abstract fun onFakeTrafficVolumeSelected(volume: DropDownStringItem)

    abstract fun refreshPreferences()
}

@HiltViewModel
class AntiCensorshipViewModelImpl
    @Inject
    constructor(
        private val preferencesHelper: PreferencesHelper,
        private val unblockWgParamsRepository: UnblockWgParamsRepository,
        private val workManager: WindScribeWorkManager,
        private val decoyTrafficController: DecoyTrafficController,
        private val vpnConnectionStateManager: VPNConnectionStateManager,
    ) : AntiCensorshipViewModel() {
        private val _protocolTweaksModes =
            MutableStateFlow(
                listOf(
                    DropDownStringItem(PreferencesKeyConstants.PROTOCOL_TWEAKS_AUTO, "Auto"),
                    DropDownStringItem(PreferencesKeyConstants.PROTOCOL_TWEAKS_MANUAL, "Enabled"),
                    DropDownStringItem(PreferencesKeyConstants.PROTOCOL_TWEAKS_DISABLED, "Disabled"),
                ),
            )
        override val protocolTweaksModes: StateFlow<List<DropDownStringItem>> = _protocolTweaksModes

        private val _selectedProtocolTweaksMode = MutableStateFlow(preferencesHelper.protocolTweaksMode)
        override val selectedProtocolTweaksMode: StateFlow<String> = _selectedProtocolTweaksMode

        private val _amneziaPresets = MutableStateFlow(emptyList<DropDownStringItem>())
        override val amneziaPresets: StateFlow<List<DropDownStringItem>> = _amneziaPresets

        private val _selectedPreset =
            MutableStateFlow(
                unblockWgParamsRepository.getSelectedUnblockWgParam()?.id ?: "",
            )
        override val selectedPreset: StateFlow<String> = _selectedPreset

        private val _extraTlsPaddingEnabled = MutableStateFlow(preferencesHelper.extraTlsPaddingEnabled)
        override val extraTlsPaddingEnabled: StateFlow<Boolean> = _extraTlsPaddingEnabled

        private val _serverRoutingModes =
            MutableStateFlow(
                listOf(
                    DropDownStringItem(PreferencesKeyConstants.SERVER_ROUTING_AUTO, "Auto"),
                    DropDownStringItem(PreferencesKeyConstants.SERVER_ROUTING_REGULAR, "Regular"),
                    DropDownStringItem(PreferencesKeyConstants.SERVER_ROUTING_ALTERNATE, "Alternate"),
                ),
            )
        override val serverRoutingModes: StateFlow<List<DropDownStringItem>> = _serverRoutingModes

        private val _selectedServerRouting = MutableStateFlow(preferencesHelper.serverRoutingMode)
        override val selectedServerRouting: StateFlow<String> = _selectedServerRouting

        private val _tlsServerName = MutableStateFlow(preferencesHelper.tlsServerName)
        override val tlsServerName: StateFlow<String> = _tlsServerName

        private val _decoyTraffic = MutableStateFlow(preferencesHelper.isDecoyTrafficOn)
        override val decoyTraffic: StateFlow<Boolean> = _decoyTraffic

        private val _trafficMultiplier = MutableStateFlow(preferencesHelper.fakeTrafficVolume.name)
        override val trafficMultiplier: StateFlow<String> = _trafficMultiplier

        private val _trafficMultipliers = MutableStateFlow(
            FakeTrafficVolume.entries.map { DropDownStringItem(it.name, it.name) }
        )
        override val trafficMultipliers: StateFlow<List<DropDownStringItem>> = _trafficMultipliers

        init {
            loadAmneziaPresets()
        }

        private fun loadAmneziaPresets() {
            viewModelScope.launch {
                unblockWgParamsRepository.unblockWgParams.collectLatest { params ->
                    val presets = params.map { DropDownStringItem(it.id, it.title) }
                    _amneziaPresets.emit(presets)
                    val selected = params.firstOrNull { it.id == _selectedPreset.value }
                    if (selected == null && presets.isNotEmpty()) {
                        _selectedPreset.emit(presets.first().key)
                    }
                }
            }
        }

        override fun onProtocolTweaksModeSelected(mode: String) {
            viewModelScope.launch {
                _selectedProtocolTweaksMode.emit(mode)
                preferencesHelper.protocolTweaksMode = mode

                // Mark as manually configured only if not Auto
                if (mode != PreferencesKeyConstants.PROTOCOL_TWEAKS_AUTO) {
                    if (!preferencesHelper.isAntiCensorshipManualMode) {
                        preferencesHelper.isAntiCensorshipManualMode = true
                    }
                }
            }
        }

        override fun onAmneziaPresetSelected(presetId: String) {
            viewModelScope.launch {
                _selectedPreset.emit(presetId)
                unblockWgParamsRepository.setSelectedUnblockWgParam(presetId)

                // Mark as manually configured
                if (!preferencesHelper.isAntiCensorshipManualMode) {
                    preferencesHelper.isAntiCensorshipManualMode = true
                }
            }
        }

        override fun onExtraTlsPaddingToggled() {
            viewModelScope.launch {
                val newValue = !_extraTlsPaddingEnabled.value
                _extraTlsPaddingEnabled.emit(newValue)
                preferencesHelper.extraTlsPaddingEnabled = newValue
            }
        }

        override fun onServerRoutingSelected(mode: String) {
            viewModelScope.launch {
                val changed = preferencesHelper.serverRoutingMode != mode
                _selectedServerRouting.emit(mode)
                preferencesHelper.serverRoutingMode = mode
                // Mark as manually configured
                if (!preferencesHelper.isAntiCensorshipManualMode) {
                    preferencesHelper.isAntiCensorshipManualMode = true
                }
                if (changed) {
                    workManager.updateServerList()
                }
            }
        }

        override fun onTlsServerNameChanged(value: String) {
            viewModelScope.launch {
                _tlsServerName.emit(value)
                preferencesHelper.tlsServerName = value
            }
        }

        override fun onDecoyTrafficToggled() {
            viewModelScope.launch {
                val newValue = !_decoyTraffic.value
                _decoyTraffic.emit(newValue)
                preferencesHelper.isDecoyTrafficOn = newValue
                if (newValue && vpnConnectionStateManager.isVPNConnected()) {
                    decoyTrafficController.load()
                    decoyTrafficController.start()
                }
            }
        }

        override fun onFakeTrafficVolumeSelected(volume: DropDownStringItem) {
            viewModelScope.launch {
                _trafficMultiplier.emit(volume.key)
                preferencesHelper.fakeTrafficVolume = FakeTrafficVolume.valueOf(volume.key)
                if (_decoyTraffic.value && vpnConnectionStateManager.isVPNConnected()) {
                    decoyTrafficController.load()
                    decoyTrafficController.start()
                }
            }
        }

        override fun refreshPreferences() {
            viewModelScope.launch {
                _selectedProtocolTweaksMode.emit(preferencesHelper.protocolTweaksMode)
                _extraTlsPaddingEnabled.emit(preferencesHelper.extraTlsPaddingEnabled)
                _selectedServerRouting.emit(preferencesHelper.serverRoutingMode)
                _tlsServerName.emit(preferencesHelper.tlsServerName)
                _decoyTraffic.emit(preferencesHelper.isDecoyTrafficOn)
                _trafficMultiplier.emit(preferencesHelper.fakeTrafficVolume.name)
            }
        }
    }
