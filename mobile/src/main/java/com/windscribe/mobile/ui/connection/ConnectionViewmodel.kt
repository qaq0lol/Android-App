package com.windscribe.mobile.ui.connection

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.home.HomeGoto
import com.windscribe.mobile.ui.preferences.lipstick.LookAndFeelHelper
import com.windscribe.mobile.ui.preferences.lipstick.LookAndFeelHelper.bundledBackgrounds
import com.windscribe.mobile.ui.preferences.lipstick.LookAndFeelHelper.getSoundFile
import com.windscribe.mobile.ui.serverlist.ServerListItem
import com.windscribe.vpn.Windscribe.Companion.appContext
import com.windscribe.vpn.apppreference.PreferencesHelper
import com.windscribe.vpn.apppreference.isProtocolTweaksEnabled
import com.windscribe.vpn.autoconnection.AutoConnectionManager
import com.windscribe.vpn.autoconnection.ProtocolInformation
import com.windscribe.vpn.backend.Util
import com.windscribe.vpn.backend.VPNState
import com.windscribe.vpn.backend.utils.LastSelectedLocation
import com.windscribe.vpn.backend.utils.SelectedLocationType
import com.windscribe.vpn.backend.utils.WindVpnController
import com.windscribe.vpn.commonutils.FlagIconResource
import com.windscribe.vpn.commonutils.ResourceHelper
import com.windscribe.vpn.commonutils.WindUtilities
import com.windscribe.vpn.constants.NetworkKeyConstants
import com.windscribe.vpn.localdatabase.LocalDbInterface
import com.windscribe.vpn.model.User
import com.windscribe.vpn.repository.IpRepository
import com.windscribe.vpn.repository.LocationRepository
import com.windscribe.vpn.repository.NotificationRepository
import com.windscribe.vpn.repository.RepositoryState
import com.windscribe.vpn.repository.ServerListRepository
import com.windscribe.vpn.repository.UserRepository
import com.windscribe.vpn.serverlist.entity.ConfigFile
import com.windscribe.vpn.serverlist.entity.Datacenter
import com.windscribe.vpn.serverlist.entity.DatacenterAndLocation
import com.windscribe.vpn.serverlist.entity.DatacenterStatus
import com.windscribe.vpn.serverlist.entity.DatacenterStatusHelper
import com.windscribe.vpn.serverlist.entity.StaticRegion
import com.windscribe.vpn.state.NetworkInfoManager
import com.windscribe.vpn.state.VPNConnectionStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

sealed class LocationInfoState {
    data class Success(
        val locationInfo: LocationInfo,
    ) : LocationInfoState()

    object Unavailable : LocationInfoState()
}

sealed class ToastMessage {
    data class Localized(
        @param:StringRes val message: Int,
    ) : ToastMessage()

    data class Raw(
        val message: String,
    ) : ToastMessage()

    object None : ToastMessage()
}

sealed class LocationBackground {
    open val resource: Int = R.drawable.dummy_flag

    data class Flag(
        @param:DrawableRes override val resource: Int,
    ) : LocationBackground()

    data class Wallpaper(
        @param:DrawableRes override val resource: Int,
    ) : LocationBackground()

    data class Custom(
        val file: File,
    ) : LocationBackground()
}

data class LocationInfo(
    val country: String,
    val nodeName: String,
    val nickName: String,
    val locationBackground: LocationBackground,
)

sealed class NetworkInfoState {
    open val name: String? = null

    data class Secured(
        override val name: String,
    ) : NetworkInfoState()

    data class Unsecured(
        override val name: String,
    ) : NetworkInfoState()

    object Unknown : NetworkInfoState()
}

sealed class ConnectionUIState {
    abstract val locationInfo: LocationInfoState?
    abstract val protocolInfo: ProtocolInformation?

    data class Connecting(
        override val protocolInfo: ProtocolInformation,
        override val locationInfo: LocationInfoState,
    ) : ConnectionUIState()

    data class Connected(
        override val protocolInfo: ProtocolInformation,
        override val locationInfo: LocationInfoState,
        val connectedUsingSplitRouting: Boolean = false,
    ) : ConnectionUIState()

    data class Disconnected(
        override val protocolInfo: ProtocolInformation,
        override val locationInfo: LocationInfoState,
    ) : ConnectionUIState()

    object Idle : ConnectionUIState() {
        override val protocolInfo: ProtocolInformation?
            get() = null
        override val locationInfo: LocationInfoState?
            get() = null
    }
}

abstract class ConnectionViewmodel : ViewModel() {
    abstract val connectionUIState: StateFlow<ConnectionUIState>
    abstract val ipState: StateFlow<String>
    abstract val shouldAnimateIp: StateFlow<Boolean>
    abstract val networkInfoState: StateFlow<NetworkInfoState>
    abstract val bestLocation: StateFlow<ServerListItem?>
    abstract val isProtocolTweaksEnabled: StateFlow<Boolean>
    abstract val isPreferredProtocolEnabled: StateFlow<Boolean>
    abstract val isDecoyTrafficEnabled: StateFlow<Boolean>
    abstract val aspectRatio: StateFlow<Int>
    abstract val goto: SharedFlow<HomeGoto>
    abstract val newFeedCount: StateFlow<Int>
    abstract val selectedCityId: StateFlow<Int>

    abstract fun onConnectButtonClick()

    abstract fun selectCity(city: Datacenter)

    abstract fun onCityClick(
        city: Datacenter,
        isFav: Boolean = false,
    )

    abstract fun onStaticIpClick(staticRegion: StaticRegion)

    abstract fun onConfigClick(config: ConfigFile)

    abstract val toastMessage: StateFlow<ToastMessage>
    abstract val isSingleLineLocationName: StateFlow<Boolean>
    abstract val shouldPlayHapticFeedback: StateFlow<Boolean>

    abstract fun clearToast()

    abstract fun onProtocolChangeClick()

    abstract fun onGoToHandled()

    abstract fun setIsSingleLineLocationName(singleLine: Boolean)

    abstract fun onHapticFeedbackHandled()

    abstract fun onIpAnimationComplete()

    abstract fun refreshNetworkDetection()
}

@HiltViewModel
class ConnectionViewmodelImpl
    @Inject
    constructor(
        private val appScope: CoroutineScope,
        private val vpnConnectionStateManager: VPNConnectionStateManager,
        private val vpnController: WindVpnController,
        private val ipRepository: IpRepository,
        private val networkInfoManager: NetworkInfoManager,
        private val locationRepository: LocationRepository,
        private val localdb: LocalDbInterface,
        private val preferences: PreferencesHelper,
        private val autoConnectionManager: AutoConnectionManager,
        private val userRepository: UserRepository,
        private val serverListRepository: ServerListRepository,
        private val resourceHelper: ResourceHelper,
        private val notificationRepository: NotificationRepository,
    ) : ConnectionViewmodel() {
        private val _connectionUIState = MutableStateFlow<ConnectionUIState>(ConnectionUIState.Idle)
        override val connectionUIState: StateFlow<ConnectionUIState> = _connectionUIState

        private val _ipState: MutableStateFlow<String> = MutableStateFlow("")
        override val ipState: StateFlow<String> = _ipState
        private val _shouldAnimateIp: MutableStateFlow<Boolean> = MutableStateFlow(false)
        override val shouldAnimateIp: StateFlow<Boolean> = _shouldAnimateIp
        private var lastValidIp: String? = null
        private val _networkInfoState: MutableStateFlow<NetworkInfoState> =
            MutableStateFlow(NetworkInfoState.Unknown)
        override val networkInfoState: StateFlow<NetworkInfoState> = _networkInfoState
        private val _toastMessage = MutableStateFlow<ToastMessage>(ToastMessage.None)
        override val toastMessage: StateFlow<ToastMessage> = _toastMessage
        private val _bestLocation = MutableStateFlow<ServerListItem?>(null)
        override val bestLocation: StateFlow<ServerListItem?> = _bestLocation
        private val _isProtocolTweaksEnabled = MutableStateFlow(preferences.isProtocolTweaksEnabled)
        override val isProtocolTweaksEnabled: StateFlow<Boolean> = _isProtocolTweaksEnabled
        private val _isPreferredProtocolEnabled = MutableStateFlow(false)
        override val isPreferredProtocolEnabled: StateFlow<Boolean> = _isPreferredProtocolEnabled
        private val _isDecoyTrafficEnabled = MutableStateFlow(false)
        override val isDecoyTrafficEnabled: StateFlow<Boolean> = _isDecoyTrafficEnabled
        private val _goto = MutableSharedFlow<HomeGoto>(replay = 0)
        override val goto: SharedFlow<HomeGoto> = _goto
        private val _newFeedCount = MutableStateFlow(0)
        override val newFeedCount: StateFlow<Int> = _newFeedCount
        override val selectedCityId: StateFlow<Int> = locationRepository.selectedCity

        private val lastLocationState: MutableStateFlow<LastSelectedLocation?> = MutableStateFlow(null)
        private val _aspectRatio = MutableStateFlow(1)
        override val aspectRatio: StateFlow<Int> = _aspectRatio
        private val _isSingleLineLocationName = MutableStateFlow(true)
        override val isSingleLineLocationName: StateFlow<Boolean> = _isSingleLineLocationName
        private val _shouldPlayHapticFeedback = MutableStateFlow(false)
        override val shouldPlayHapticFeedback: StateFlow<Boolean> = _shouldPlayHapticFeedback
        private val logger = LoggerFactory.getLogger("ConnectionViewmodel")

        private var lastCityClickTime = 0L
        private var lastStaticIpClickTime = 0L
        private var lastConfigClickTime = 0L
        private val debounceMillis = 1500L

        init {
            fetchLastLocation()
            fetchIPState()
            fetchNetworkState()
            fetchBestLocation()
            fetchUserPreferences()
            observeNotificationCount()
            handleConnectionHapticFeedback()
        }

        private fun observeNotificationCount() {
            viewModelScope.launch(Dispatchers.IO) {
                notificationRepository.unreadCount.collect { count ->
                    _newFeedCount.emit(count)
                }
            }
        }

        private fun fetchUserPreferences() {
            viewModelScope.launch(Dispatchers.IO) {
                preferences.protocolTweaksModeFlow.collectLatest { mode ->
                    // Update the boolean state based on the mode using the extension property
                    _isProtocolTweaksEnabled.value = preferences.isProtocolTweaksEnabled
                }
            }
            // Also observe session changes for Auto mode API recommendations
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.user.filterNotNull().collectLatest { user ->
                    _isProtocolTweaksEnabled.value = preferences.isProtocolTweaksEnabled
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                preferences.backgroundAspectRatioOptionFlow.collectLatest { ratio ->
                    _aspectRatio.value = ratio
                }
            }
        }

        private fun fetchLastLocation() {
            viewModelScope.launch(Dispatchers.IO) {
                val location = Util.getLastSelectedLocation(appContext)
                if (location != null) {
                    lastLocationState.emit(location)
                    fetchConnectionState()
                } else {
                    try {
                        val bestDatacenterAndLocation = locationRepository.getBestLocationAsync()
                        saveLastLocation(bestDatacenterAndLocation)
                        _bestLocation.emit(
                            ServerListItem(
                                bestDatacenterAndLocation.location!!.id,
                                bestDatacenterAndLocation.location!!,
                                listOf(bestDatacenterAndLocation.datacenter),
                            ),
                        )
                        fetchConnectionState()
                    } catch (_: Exception) {
                        lastLocationState.value = null
                        fetchConnectionState()
                    }
                }
            }
        }


        private fun saveLastLocation(location: DatacenterAndLocation) {
            val coords = (location.datacenter.coordinates ?: "").split(",")
            val lat = coords.getOrNull(0)?.trim() ?: ""
            val lon = coords.getOrNull(1)?.trim() ?: ""
            val lastLocation =
                LastSelectedLocation(
                    location.datacenter.id,
                    location.datacenter.nodeName ?: "",
                    location.datacenter.nickName ?: "",
                    location.location?.countryCode,
                    lat,
                    lon,
                )
            Util.saveSelectedLocation(lastLocation)
            locationRepository.setSelectedCity(lastLocation.cityId)
            lastLocationState.value = lastLocation
        }

        private fun saveLastLocation(staticRegion: StaticRegion) {
            val lastLocation =
                LastSelectedLocation(
                    staticRegion.id ?: 0,
                    staticRegion.cityName ?: "",
                    staticRegion.staticIp ?: "",
                    staticRegion.countryCode,
                    "",
                    "",
                )
            Util.saveSelectedLocation(lastLocation)
            locationRepository.setSelectedCity(lastLocation.cityId)
            lastLocationState.value = lastLocation
        }

        private fun saveLastLocation(config: ConfigFile) {
            val lastLocation =
                LastSelectedLocation(config.primaryKey, "Custom Config", config.name ?: "", "", "", "")
            Util.saveSelectedLocation(lastLocation)
            locationRepository.setSelectedCity(lastLocation.cityId)
            lastLocationState.value = lastLocation
        }

        private fun fetchNetworkState() {
            viewModelScope.launch(Dispatchers.IO) {
                networkInfoManager.networkInfo.collect { networkInfo ->
                    if (networkInfo == null) {
                        _isPreferredProtocolEnabled.value = false
                        _networkInfoState.value = NetworkInfoState.Unknown
                    } else {
                        connectionUIState.value.protocolInfo?.let {
                            setPreferredProtocolState(it)
                        }
                        if (networkInfo.isAutoSecureOn) {
                            _networkInfoState.value = NetworkInfoState.Secured(networkInfo.networkName)
                        } else {
                            _networkInfoState.value =
                                NetworkInfoState.Unsecured(networkInfo.networkName)
                        }
                    }
                }
            }
        }

        private fun fetchIPState() {
            viewModelScope.launch(Dispatchers.IO) {
                ipRepository.state.collect { state ->
                    when (state) {
                        is RepositoryState.Error -> {
                            _ipState.emit("--.--.--.--")
                            _shouldAnimateIp.emit(false)
                        }

                        is RepositoryState.Success -> {
                            val newIp = state.data
                            val isValid = !newIp.contains("--")
                            val shouldAnimate = isValid && lastValidIp != null && lastValidIp != newIp
                            _shouldAnimateIp.emit(shouldAnimate)
                            _ipState.emit(newIp)
                            if (isValid) {
                                lastValidIp = newIp
                            }
                        }

                        else -> {}
                    }
                }
            }
            ipRepository.update()
        }

        private var connectionStateJob: kotlinx.coroutines.Job? = null

        private fun fetchConnectionState() {
            connectionStateJob?.cancel()
            connectionStateJob = viewModelScope.launch(Dispatchers.IO) {
                combine(
                    vpnConnectionStateManager.state,
                    autoConnectionManager.connectedProtocol,
                    autoConnectionManager.nextInLineProtocol,
                    serverListRepository.customRegions,
                    serverListRepository.customCities,
                ) { state, connectedProtocol, nextInLineProtocol, _, _ ->
                    val protocolInfo =
                        when (state.status) {
                            VPNState.Status.Connected, VPNState.Status.Connecting -> connectedProtocol
                            else -> nextInLineProtocol
                        } ?: Util.getAppSupportedProtocolList().first()
                    val locationInfo = buildLocationInfo()
                    setPreferredProtocolState(protocolInfo)
                    when (state.status) {
                        VPNState.Status.Connected -> {
                            ConnectionUIState.Connected(
                                protocolInfo,
                                locationInfo,
                                preferences.lastConnectedUsingSplit,
                            )
                        }

                        VPNState.Status.Connecting -> {
                            ConnectionUIState.Connecting(
                                protocolInfo,
                                locationInfo,
                            )
                        }

                        VPNState.Status.Disconnected -> {
                            handleError(state)
                            ConnectionUIState.Disconnected(protocolInfo, locationInfo)
                        }

                        else -> {
                            ConnectionUIState.Disconnected(protocolInfo, locationInfo)
                        }
                    }
                }.collectLatest { uiState ->
                    _connectionUIState.value = uiState
                }
            }
        }

        private fun handleError(state: VPNState) {
            viewModelScope.launch(Dispatchers.IO) {
                if (state.error?.showError == true && state.error?.message != null) {
                    _toastMessage.emit(ToastMessage.Raw(state.error!!.message))
                }
            }
        }

        private fun setPreferredProtocolState(protocolInfo: ProtocolInformation) {
            viewModelScope.launch(Dispatchers.IO) {
                val networkInfo = networkInfoManager.networkInfo.value
                _isPreferredProtocolEnabled.value =
                    networkInfo?.isPreferredOn == true &&
                    networkInfo.protocol == protocolInfo.protocol &&
                    networkInfo.port == protocolInfo.port
            }
        }

        private fun handleConnectionHapticFeedback() {
            viewModelScope.launch(Dispatchers.IO) {
                vpnConnectionStateManager.state.collectLatest { state ->
                    if (state.status == VPNState.Status.Connected || state.status == VPNState.Status.Disconnected) {
                        if (preferences.isHapticFeedbackEnabled) {
                            _shouldPlayHapticFeedback.emit(true)
                        }
                    }
                }
            }
        }

        private fun buildLocationInfo(): LocationInfoState {
            var location = lastLocationState.value
            if (location == null || location.cityId != locationRepository.selectedCity.value) {
                location = Util.getLastSelectedLocation(appContext)
                lastLocationState.value = location
            }
            if (location == null) return LocationInfoState.Unavailable
            val cityId = location.cityId
            val countryCode = location.countryCode.orEmpty()
            return LocationInfoState.Success(
                LocationInfo(
                    countryCode,
                    serverListRepository.getCustomCityName(cityId) ?: location.nodeName,
                    serverListRepository.getCustomCityNickName(cityId) ?: location.nickName,
                    getLocationBackground(countryCode),
                ),
            )
        }

        private fun getLocationBackground(countryCode: String): LocationBackground {
            val isConnected = vpnConnectionStateManager.isVPNConnected()
            val option =
                if (isConnected) {
                    preferences.whenConnectedBackgroundOption
                } else {
                    preferences.whenDisconnectedBackgroundOption
                }
            return when (option) {
                1 -> {
                    LocationBackground.Flag(FlagIconResource.getFlag(countryCode))
                }

                2 -> {
                    val index =
                        if (isConnected) preferences.connectedBundleBackgroundOption else preferences.disconnectedBundleBackgroundOption
                    LocationBackground.Wallpaper(
                        bundledBackgrounds[index] ?: com.windscribe.vpn.R.mipmap.square,
                    )
                }

                3 -> {
                    LocationBackground.Flag(R.drawable.dummy_flag)
                }

                else -> {
                    val path =
                        if (isConnected) {
                            preferences.customConnectedBackground
                        } else {
                            preferences.customDisconnectedBackground
                        }
                    val image =
                        if (isConnected) {
                            "connected_background.png"
                        } else {
                            "disconnected_background.png"
                        }
                    if (path.isNullOrEmpty()) {
                        LocationBackground.Flag(R.drawable.dummy_flag)
                    } else {
                        LocationBackground.Custom(File(appContext.filesDir, image))
                    }
                }
            }
        }

        override fun onConnectButtonClick() {
            val currentState = connectionUIState.value
            if (currentState is ConnectionUIState.Connected || currentState is ConnectionUIState.Connecting) {
                preferences.globalUserConnectionPreference = false
                vpnController.disconnectAsync()
                return
            }
            appScope.launch {
                // 如果当前选中的位置为 null，尝试自动寻找最佳可用节点或首个节点
                var selectedLocation = lastLocationState.value
                if (selectedLocation == null) {
                    try {
                        val best = locationRepository.getBestLocationAsync()
                        saveLastLocation(best)
                        selectedLocation = lastLocationState.value
                    } catch (e: Exception) {
                        logger.error("Failed to get best location fallback", e)
                    }
                }
                if (selectedLocation == null) {
                    try {
                        val locations = localdb.getAllLocationsAsync()
                        val firstCity = locations.firstOrNull()?.datacenters?.firstOrNull()
                        if (firstCity != null) {
                            val dcAndLoc = localdb.getDatacenterAndLocation(firstCity.id)
                            if (dcAndLoc != null) {
                                saveLastLocation(dcAndLoc)
                                selectedLocation = lastLocationState.value
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to get first city fallback", e)
                    }
                }

                if (selectedLocation == null) {
                    showToast("无法获取可用节点，请先刷新节点列表")
                    return@launch
                }

                val locationSource = WindUtilities.getSourceTypeBlocking()
                when (locationSource) {
                    SelectedLocationType.CustomConfiguredProfile -> {
                        try {
                            val location = localdb.getConfigFileAsync(selectedLocation.cityId)
                            onConfigClick(location)
                        } catch (e: Exception) {
                            showToast("Unable to find selected location in database. Update server list.")
                        }
                    }

                    SelectedLocationType.StaticIp -> {
                        val staticIp = localdb.getStaticRegionByIDAsync(selectedLocation.cityId)
                        if (staticIp != null) {
                            onStaticIpClick(staticIp)
                        }
                    }

                    SelectedLocationType.CityLocation, null -> {
                        try {
                            val location = localdb.getDatacenterAndLocation(selectedLocation.cityId)
                            if (location != null) {
                                onCityClick(location.datacenter)
                            } else {
                                showToast("Unable to find selected location in database. Update server list.")
                            }
                        } catch (e: Exception) {
                            showToast("Unable to find selected location in database. Update server list.")
                        }
                    }
                }
            }
        }

        private var selectCityJob: kotlinx.coroutines.Job? = null
        override fun selectCity(city: Datacenter) {
            selectCityJob?.cancel()
            selectCityJob = viewModelScope.launch(Dispatchers.IO) {
                val datacenterAndLocation = localdb.getDatacenterAndLocation(city.id) ?: return@launch
                preferences.isConnectingToStaticIp = false
                preferences.isConnectingToConfigured = false
                saveLastLocation(datacenterAndLocation)
                fetchConnectionState()
            }
        }

        override fun onCityClick(
            city: Datacenter,
            isFav: Boolean,
        ) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCityClickTime < debounceMillis) {
                logger.debug("Ignoring city click, debounce time not elapsed")
                return
            }
            lastCityClickTime = currentTime

            viewModelScope.launch(Dispatchers.IO) {
                val datacenterAndLocation = localdb.getDatacenterAndLocation(city.id)
                if (datacenterAndLocation == null) {
                    showToast("Unable to find selected location in database. Update server list.")
                    return@launch
                }
                try {
                    // Check datacenter status
                    val serverCount = localdb.getServersByDatacenter(city.id).size
                    val user = userRepository.user.value
                    val isPro = user?.isPro ?: false
                    val hasAlcAccess = user?.hasAlcAccess(datacenterAndLocation.location?.countryCode) ?: false
                    val status = DatacenterStatusHelper.getStatus(city, serverCount, isPro, hasAlcAccess)
                    when (status) {
                        DatacenterStatus.UnderMaintenance -> {
                            _goto.emit(HomeGoto.LocationMaintenance)
                            return@launch
                        }

                        DatacenterStatus.Pro -> {
                            _goto.emit(HomeGoto.Upgrade)
                            return@launch
                        }

                        else -> {}
                    }

                    // Check internet connectivity
                    if (!WindUtilities.isOnline()) {
                        logger.info("Error: no internet available to connect.")
                        showToast(com.windscribe.vpn.R.string.no_internet)
                        return@launch
                    }

                    // Check account status
                    if (user?.accountStatus == User.AccountStatus.Expired) {
                        logger.info("Error: account status is expired and can not connect to this datacenter")
                        val resetDate = user.nextResetDate() ?: ""
                        _goto.emit(HomeGoto.Expired(resetDate))
                        return@launch
                    }
                    if (user?.accountStatus == User.AccountStatus.Banned) {
                        logger.info("Error: account status is banned and can not connect to this datacenter.")
                        _goto.emit(HomeGoto.Banned)
                        return@launch
                    }

                    // Verify pinned node exists (if favourite)
                    if (isFav) {
                        val favourite = localdb.getFavouritesAsync().firstOrNull { it.id == city.id }
                        if (favourite?.pinnedIp != null && favourite.pinnedNodeIp != null) {
                            val nodes = localdb.getServersByDatacenter(city.id)
                            val nodeExists = nodes.any { WindUtilities.hostnamesMatch(it.hostname, favourite.pinnedNodeIp) }
                            if (!nodeExists) {
                                logger.warn("Pinned node IP ${favourite.pinnedNodeIp} not found in city ${city.id} nodes")
                                val errorMessage = resourceHelper.getString(com.windscribe.vpn.R.string.could_not_pin_ip)
                                val errorDescription = resourceHelper.getString(com.windscribe.vpn.R.string.check_status_description)
                                _goto.emit(HomeGoto.IpActionError(errorMessage, errorDescription))
                                return@launch
                            }
                        }
                    }

                    // All checks passed - connect
                    preferences.globalUserConnectionPreference = true
                    preferences.isConnectingToStaticIp = false
                    preferences.isConnectingToConfigured = false
                    saveLastLocation(datacenterAndLocation)
                    logger.debug("Attempting to connect")
                    appScope.launch {
                        autoConnectionManager.connectInForeground()
                    }
                } catch (_: Exception) {
                    showToast("Unable to find selected location in database. Update server list.")
                }
            }
        }

        override fun onStaticIpClick(staticRegion: StaticRegion) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastStaticIpClickTime < debounceMillis) {
                logger.debug("Ignoring static IP click, debounce time not elapsed")
                return
            }
            lastStaticIpClickTime = currentTime

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Check static IP availability
                    val status = staticRegion.status ?: 1
                    if (status == NetworkKeyConstants.SERVER_STATUS_TEMPORARILY_UNAVAILABLE || status == 0) {
                        logger.info("Error: Static IP is temporarily unavailable.")
                        showToast("Location temporary unavailable.")
                        _goto.emit(HomeGoto.LocationMaintenance)
                        return@launch
                    }

                    // Check internet connectivity
                    if (!WindUtilities.isOnline()) {
                        logger.info("Error: no internet available.")
                        showToast(com.windscribe.vpn.R.string.no_internet)
                        return@launch
                    }

                    // Check account status (banned only)
                    val user = userRepository.user.value
                    if (user?.accountStatus == User.AccountStatus.Banned) {
                        logger.info("Error: account status is banned.")
                        _goto.emit(HomeGoto.Banned)
                        return@launch
                    }

                    // All checks passed - connect
                    preferences.globalUserConnectionPreference = true
                    preferences.isConnectingToStaticIp = true
                    preferences.isConnectingToConfigured = false
                    saveLastLocation(staticRegion)
                    logger.debug("Attempting to connect..")
                    appScope.launch {
                        autoConnectionManager.connectInForeground()
                    }
                } catch (e: Exception) {
                    showToast("Unable to find selected location in database. Update server list.")
                }
            }
        }

        override fun onConfigClick(config: ConfigFile) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastConfigClickTime < debounceMillis) {
                logger.debug("Ignoring config click, debounce time not elapsed")
                return
            }
            lastConfigClickTime = currentTime

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Check internet connectivity
                    if (!WindUtilities.isOnline()) {
                        logger.info("Error: no internet available.")
                        showToast(com.windscribe.vpn.R.string.no_internet)
                        return@launch
                    }

                    // Setup config connection
                    locationRepository.setSelectedCity(config.primaryKey)
                    saveLastLocation(config)
                    preferences.globalUserConnectionPreference = true
                    preferences.isConnectingToStaticIp = false
                    preferences.isConnectingToConfigured = true
                    val type = WindUtilities.getConfigType(config.content ?: "")
                    if (type == WindUtilities.ConfigType.OpenVPN && (config.username.isNullOrEmpty() || config.password.isNullOrEmpty())) {
                        _goto.emit(HomeGoto.EditCustomConfig(config.primaryKey, true))
                        return@launch
                    } else {
                        vpnController.connectAsync()
                    }
                } catch (e: Exception) {
                    showToast("Unable to find selected location in database. Update server list.")
                }
            }
        }

        override fun onProtocolChangeClick() {
            if (WindUtilities.getSourceTypeBlocking() == SelectedLocationType.CustomConfiguredProfile) {
                showToast(com.windscribe.vpn.R.string.protocol_change_is_not_available_for_custom_config)
            } else {
                appScope.launch {
                    preferences.globalUserConnectionPreference = true
                    autoConnectionManager.changeProtocolInForeground()
                }
            }
        }

        private fun showToast(message: String) {
            viewModelScope.launch {
                _toastMessage.emit(ToastMessage.Raw(message))
            }
        }

        private fun showToast(
            @StringRes message: Int,
        ) {
            viewModelScope.launch {
                _toastMessage.emit(ToastMessage.Localized(message))
            }
        }

        override fun clearToast() {
            _toastMessage.value = ToastMessage.None
        }

        private fun fetchBestLocation() {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val bestDatacenterAndLocation = locationRepository.getBestLocationAsync()
                    try {
                        _bestLocation.emit(
                            ServerListItem(
                                bestDatacenterAndLocation.location!!.id,
                                bestDatacenterAndLocation.location!!,
                                listOf(bestDatacenterAndLocation.datacenter),
                            ),
                        )
                    } catch (ignored: Exception) {
                        _bestLocation.emit(null)
                    }
                } catch (_: Exception) {
                    _bestLocation.emit(null)
                }
            }
        }

        override fun onGoToHandled() {
            viewModelScope.launch {
                _goto.emit(HomeGoto.None)
                _toastMessage.emit(ToastMessage.None)
            }
        }

        override fun setIsSingleLineLocationName(singleLine: Boolean) {
            viewModelScope.launch {
                _isSingleLineLocationName.emit(singleLine)
            }
        }

        override fun onHapticFeedbackHandled() {
            viewModelScope.launch {
                _shouldPlayHapticFeedback.emit(false)
            }
        }

        override fun onIpAnimationComplete() {
            viewModelScope.launch {
                _shouldAnimateIp.emit(false)
            }
        }

        override fun refreshNetworkDetection() {
            ipRepository.update()
        }
    }
