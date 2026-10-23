package com.windscribe.mobile.ui.preferences.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.vpn.Windscribe.Companion.appContext
import com.windscribe.vpn.api.IApiCallManager
import com.windscribe.vpn.apppreference.PreferencesHelper
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.CONNECTION_MODE_AUTO
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.PROTO_IKev2
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.PROTO_STEALTH
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.PROTO_TCP
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.PROTO_UDP
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.PROTO_WIRE_GUARD
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.PROTO_WS_TUNNEL
import com.windscribe.vpn.autoconnection.AutoConnectionManager
import com.windscribe.vpn.backend.ProxyDNSManager
import com.windscribe.vpn.exceptions.WindScribeException
import com.windscribe.vpn.repository.PortMapRepository
import com.windscribe.vpn.state.VPNConnectionStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.NetworkInterface
import java.util.Locale
import javax.inject.Inject

abstract class ConnectionViewModel : ViewModel() {
    abstract fun onProtocolSelected(protocol: DropDownStringItem)

    abstract fun onPortSelected(port: DropDownStringItem)

    abstract fun onModeSelected(mode: String)

    abstract fun onDNSModeSelected(mode: String)

    abstract fun onCustomDNSAddressChanged(address: String)

    abstract fun onPacketSizedChanged(size: Int)

    abstract fun saveCustomDNSAddress()

    abstract val showProgress: StateFlow<Boolean>
    abstract val mode: StateFlow<String>
    abstract val dnsMode: StateFlow<String>
    abstract val packetSizeAuto: StateFlow<Boolean>
    abstract val customDnsAddress: StateFlow<String>
    abstract val selectedProtocol: StateFlow<String>
    abstract val selectedPort: StateFlow<String>
    abstract val protocols: StateFlow<List<DropDownStringItem>>
    abstract val ports: StateFlow<List<DropDownStringItem>>
    abstract val autoConnect: StateFlow<Boolean>

    abstract fun onAutoConnectToggleClicked()

    abstract val allowLan: StateFlow<Boolean>

    abstract fun onAllowLanToggleClicked()

    abstract val gpsSpoofing: StateFlow<Boolean>

    abstract fun onGPSSpoofingToggleClicked()

    abstract fun onPacketSizeModeSelected(auto: Boolean)

    abstract val packetSize: StateFlow<Int>

    abstract fun onPacketSizeSaved()

    abstract fun onAutoDetectClicked()

    abstract val autoDetecting: StateFlow<Boolean>
    abstract val toastMessage: SharedFlow<ToastMessage>

    abstract fun refreshPreferences()

    abstract fun testAndSaveCustomDns(address: String, onComplete: (Boolean) -> Unit)
}

data class ProtoItem(
    val proto: String,
    val heading: String,
)

data class PortMapItem(
    val protoItem: ProtoItem,
    val ports: List<String>,
    val use: String,
)

@HiltViewModel
class ConnectionViewModelImpl
    @Inject
    constructor(
        val preferencesHelper: PreferencesHelper,
        val api: IApiCallManager,
        val autoConnectionManager: AutoConnectionManager,
        val vpnManagerStateManager: VPNConnectionStateManager,
        val proxyDNSManager: ProxyDNSManager,
        val portMapRepository: PortMapRepository,
    ) : ConnectionViewModel() {
        private val _showProgress = MutableStateFlow(false)
        override val showProgress: StateFlow<Boolean> = _showProgress
        private val _mode = MutableStateFlow(CONNECTION_MODE_AUTO)
        override val mode: StateFlow<String> = _mode
        private val _selectedProtocol = MutableStateFlow("")
        override val selectedProtocol: StateFlow<String> = _selectedProtocol
        private val _selectedPort = MutableStateFlow("")
        override val selectedPort: StateFlow<String> = _selectedPort
        private val _protocols = MutableStateFlow(emptyList<DropDownStringItem>())
        override val protocols: StateFlow<List<DropDownStringItem>> = _protocols
        private val _ports = MutableStateFlow(emptyList<DropDownStringItem>())
        override val ports: StateFlow<List<DropDownStringItem>> = _ports
        private val logger = LoggerFactory.getLogger("basic")
        private val portMapItems = mutableListOf<PortMapItem>()
        private val _dnsMode = MutableStateFlow(preferencesHelper.dnsMode)
        override val dnsMode: StateFlow<String> = _dnsMode
        private val _customDnsAddress = MutableStateFlow(preferencesHelper.dnsAddress ?: "")
        override val customDnsAddress: StateFlow<String> = _customDnsAddress
        private val _autoConnect = MutableStateFlow(preferencesHelper.autoConnect)
        override val autoConnect: StateFlow<Boolean> = _autoConnect
        private val _allowLan = MutableStateFlow(preferencesHelper.lanByPass)
        override val allowLan: StateFlow<Boolean> = _allowLan
        private val _gpsSpoofing = MutableStateFlow(preferencesHelper.isGpsSpoofingOn)
        override val gpsSpoofing: StateFlow<Boolean> = _gpsSpoofing
        private val _packetSizeAuto = MutableStateFlow(preferencesHelper.isPackageSizeModeAuto)
        override val packetSizeAuto: StateFlow<Boolean> = _packetSizeAuto
        private val _packetSize = MutableStateFlow(preferencesHelper.packetSize)
        override val packetSize: StateFlow<Int> = _packetSize
        private val _autoDetecting = MutableStateFlow(false)
        override val autoDetecting: StateFlow<Boolean> = _autoDetecting
        private val _toastMessage = MutableSharedFlow<ToastMessage>(replay = 0)
        override val toastMessage: SharedFlow<ToastMessage> = _toastMessage
        private var currentPoint = 1500

        init {
            loadPortMapItems()
        }

        private fun loadPortMapItems() {
            viewModelScope.launch(Dispatchers.IO) {
                val portMapResult = portMapRepository.getPortMap()
                if (portMapResult.isSuccess) {
                    val portMap = portMapResult.getOrNull()
                    portMap
                        ?.portmap
                        ?.map {
                            PortMapItem(ProtoItem(it.protocol ?: "", it.heading ?: ""), it.ports ?: emptyList(), it.use ?: "")
                        }?.let {
                            portMapItems.addAll(it)
                            buildProtocolInfo()
                        }
                }
                val connectionMode = preferencesHelper.connectionMode
                if (connectionMode != null) {
                    _mode.emit(connectionMode)
                }
                val dnsMode = preferencesHelper.dnsMode
                if (dnsMode.isNotEmpty()) {
                    _dnsMode.emit(dnsMode)
                }
            }
        }

        override fun refreshPreferences() {
            viewModelScope.launch {
                _gpsSpoofing.emit(preferencesHelper.isGpsSpoofingOn)
            }
        }

        private fun buildProtocolInfo() {
            portMapItems
                .map {
                    DropDownStringItem(it.protoItem.proto, it.protoItem.heading)
                }.let {
                    _protocols.value = it
                    _selectedProtocol.value = preferencesHelper.savedProtocol
                    _selectedPort.value = getSavedPort(preferencesHelper.savedProtocol)
                }
            portMapItems
                .first {
                    it.protoItem.proto == preferencesHelper.savedProtocol
                }.ports
                .let {
                    _ports.value = it.map { DropDownStringItem(it, it) }
                }
        }

        private fun getSavedPort(protocol: String): String =
            when (protocol) {
                PROTO_IKev2 -> preferencesHelper.iKEv2Port
                PROTO_UDP -> preferencesHelper.savedUDPPort
                PROTO_TCP -> preferencesHelper.savedTCPPort
                PROTO_STEALTH -> preferencesHelper.savedSTEALTHPort
                PROTO_WS_TUNNEL -> preferencesHelper.savedWSTunnelPort
                PROTO_WIRE_GUARD -> preferencesHelper.wireGuardPort
                else -> "443"
            }

        override fun onProtocolSelected(protocol: DropDownStringItem) {
            viewModelScope.launch {
                preferencesHelper.savedProtocol = protocol.key
                _selectedProtocol.emit(protocol.key)
                autoConnectionManager.reset()
                buildProtocolInfo()
            }
        }

        private fun savePort(
            proto: String,
            port: String,
        ) {
            when (proto) {
                PROTO_IKev2 -> {
                    logger.info("Saving selected IKev2 port...")
                    preferencesHelper.iKEv2Port = port
                }

                PROTO_UDP -> {
                    logger.info("Saving selected udp port...")
                    preferencesHelper.savedUDPPort = port
                }

                PROTO_TCP -> {
                    logger.info("Saving selected tcp port...")
                    preferencesHelper.savedTCPPort = port
                }

                PROTO_STEALTH -> {
                    logger.info("Saving selected stealth port...")
                    preferencesHelper.savedSTEALTHPort = port
                }

                PROTO_WS_TUNNEL -> {
                    logger.info("Saving selected ws tunnel port...")
                    preferencesHelper.savedWSTunnelPort = port
                }

                PROTO_WIRE_GUARD -> {
                    logger.info("Saving selected wire guard port...")
                    preferencesHelper.wireGuardPort = port
                }

                else -> {
                    logger.info("Saving default port (udp)...")
                    preferencesHelper.savedUDPPort = port
                }
            }
        }

        override fun onPortSelected(port: DropDownStringItem) {
            val proto = _selectedProtocol.value
            viewModelScope.launch {
                savePort(proto, port.key)
                preferencesHelper.selectedPort = port.key
                _selectedPort.emit(port.key)
                buildProtocolInfo()
            }
        }

        override fun onModeSelected(mode: String) {
            viewModelScope.launch {
                _mode.emit(mode)
                preferencesHelper.connectionMode = mode
            }
        }

        override fun onDNSModeSelected(mode: String) {
            viewModelScope.launch {
                _dnsMode.emit(mode)
                preferencesHelper.dnsMode = mode
            }
        }

        override fun onCustomDNSAddressChanged(address: String) {
            viewModelScope.launch {
                _customDnsAddress.emit(address)
            }
        }

        override fun saveCustomDNSAddress() {
            if (preferencesHelper.dnsAddress == _customDnsAddress.value) return
            preferencesHelper.dnsAddress = _customDnsAddress.value
            proxyDNSManager.invalidConfig = true
            if (!vpnManagerStateManager.isVPNConnected()) {
                viewModelScope.launch {
                    proxyDNSManager.stopControlD()
                }
            }
        }

        override fun onAutoConnectToggleClicked() {
            viewModelScope.launch {
                _autoConnect.emit(!_autoConnect.value)
                preferencesHelper.autoConnect = _autoConnect.value
            }
        }

        override fun onAllowLanToggleClicked() {
            viewModelScope.launch {
                _allowLan.emit(!_allowLan.value)
                preferencesHelper.lanByPass = _allowLan.value
            }
        }

        override fun onGPSSpoofingToggleClicked() {
            viewModelScope.launch {
                _gpsSpoofing.emit(!_gpsSpoofing.value)
                preferencesHelper.isGpsSpoofingOn = _gpsSpoofing.value
            }
        }

        override fun testAndSaveCustomDns(address: String, onComplete: (Boolean) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                val trimmed = address.trim()
                if (trimmed.isEmpty()) {
                    withContext(Dispatchers.Main) { onComplete(false) }
                    return@launch
                }
                val host = if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                    try {
                        java.net.URI(trimmed).host ?: trimmed
                    } catch (_: Exception) {
                        trimmed
                    }
                } else if (trimmed.startsWith("[") && trimmed.contains("]")) {
                    trimmed.substringAfter("[").substringBefore("]")
                } else if (trimmed.count { it == ':' } > 1) {
                    trimmed
                } else {
                    trimmed.substringBefore(":")
                }
                val reachable = try {
                    val addr = java.net.InetAddress.getByName(host)
                    var success = false
                    try {
                        java.net.Socket().use { sock ->
                            sock.connect(java.net.InetSocketAddress(addr, 53), 2000)
                            success = true
                        }
                    } catch (_: Exception) {
                        try {
                            java.net.Socket().use { sock ->
                                sock.connect(java.net.InetSocketAddress(addr, 443), 2000)
                                success = true
                            }
                        } catch (_: Exception) {
                            addr.isReachable(2000)
                        }
                    }
                    success
                } catch (_: Exception) {
                    false
                }

                if (reachable) {
                    preferencesHelper.dnsAddress = trimmed
                    _customDnsAddress.emit(trimmed)
                    proxyDNSManager.invalidConfig = true
                    if (!vpnManagerStateManager.isVPNConnected()) {
                        proxyDNSManager.stopControlD()
                    }
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            }
        }

        override fun onPacketSizeModeSelected(auto: Boolean) {
            viewModelScope.launch {
                _packetSizeAuto.emit(auto)
                preferencesHelper.isPackageSizeModeAuto = auto
            }
        }

        override fun onPacketSizedChanged(size: Int) {
            viewModelScope.launch {
                _packetSize.emit(size)
            }
        }

        override fun onPacketSizeSaved() {
            preferencesHelper.packetSize = _packetSize.value
        }

        override fun onAutoDetectClicked() {
            viewModelScope.launch {
                if (vpnManagerStateManager.isVPNConnected()) {
                    _toastMessage.emit(ToastMessage.Localized(com.windscribe.vpn.R.string.disconnect_from_vpn))
                    return@launch
                }
                val manager =
                    appContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (!manager.hasConnectedNetwork()) {
                    _toastMessage.emit(ToastMessage.Localized(com.windscribe.vpn.R.string.no_network_detected))
                    return@launch
                }
                _autoDetecting.emit(true)
                val prop: LinkProperties? = manager.getLinkProperties(manager.activeNetwork)
                val iFace: NetworkInterface
                if (prop != null) {
                    iFace = NetworkInterface.getByName(prop.interfaceName)
                    currentPoint = iFace.mtu
                } else {
                    currentPoint = 1500
                }
                repeatPingFlow(currentPoint)
                    .flowOn(Dispatchers.IO)
                    .collectLatest { result ->
                        _autoDetecting.emit(false)
                        result.onSuccess {
                            _packetSize.emit(it)
                            preferencesHelper.packetSize = it
                            _toastMessage.emit(ToastMessage.Raw("Packet size detected successfully"))
                        }
                        result.onFailure {
                            _toastMessage.emit(
                                ToastMessage.Raw(
                                    it.message ?: "Error detecting packet size",
                                ),
                            )
                        }
                    }
            }
        }

        private fun ConnectivityManager.hasConnectedNetwork(): Boolean {
            val caps = getNetworkCapabilities(activeNetwork) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        private fun repeatPingFlow(startMtu: Int): Flow<Result<Int>> =
            flow {
                var mtu = startMtu
                while (mtu > 10) {
                    val result = ping(mtu)
                    if (result.isSuccess && isMtuSmallEnough(result.getOrNull().orEmpty())) {
                        emit(Result.success(mtu))
                        return@flow
                    } else {
                        mtu -= 10
                    }
                }
                emit(Result.failure(WindScribeException("MTU detection failed.")))
            }

        private suspend fun ping(value: Int): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    val process =
                        Runtime
                            .getRuntime()
                            .exec("/system/bin/ping -c 2 -s $value -i 0.5 -W 3 -M do checkip.windscribe.com")

                    val input = process.inputStream.bufferedReader().use { it.readText() }

                    if (input.isNotEmpty()) {
                        Result.success(input)
                    } else {
                        Result.failure(WindScribeException("Empty ping response."))
                    }
                } catch (e: IOException) {
                    Result.failure(WindScribeException("MTU detection failed."))
                }
            }

        private fun isMtuSmallEnough(response: String): Boolean = !response.contains("100% packet loss")

        override fun onCleared() {
            autoConnectionManager.reset()
            super.onCleared()
        }
    }
