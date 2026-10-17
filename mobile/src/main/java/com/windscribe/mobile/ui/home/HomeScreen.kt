package com.windscribe.mobile.ui.home

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.Icons
import NetworkNameSheet
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import com.windscribe.mobile.ui.common.AppWallpaperBackground
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.AnimatedIPAddress
import com.windscribe.mobile.ui.common.AppConnectButton
import com.windscribe.mobile.ui.common.LocationImage
import com.windscribe.mobile.ui.common.fitsInOneLine
import com.windscribe.mobile.ui.connection.BridgeApiViewModel
import com.windscribe.mobile.ui.connection.BridgeApiViewModelImpl
import com.windscribe.mobile.ui.connection.ConnectionUIState
import com.windscribe.mobile.ui.connection.ConnectionViewmodel
import com.windscribe.mobile.ui.connection.ConnectionViewmodelImpl
import com.windscribe.mobile.ui.connection.LocationBackground
import com.windscribe.mobile.ui.connection.LocationInfo
import com.windscribe.mobile.ui.connection.LocationInfoState
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.helper.ForceStatusBarIcons
import com.windscribe.mobile.ui.helper.MultiDevicePreview
import com.windscribe.mobile.ui.helper.PreviewWithNav
import com.windscribe.mobile.ui.helper.getStatusBarHeight
import com.windscribe.mobile.ui.helper.hapticClickable
import com.windscribe.mobile.ui.model.AccountStatusDialogData
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.mobile.ui.serverlist.ConfigViewmodel
import com.windscribe.mobile.ui.serverlist.ConfigViewmodelImpl
import com.windscribe.mobile.ui.serverlist.SearchServerList
import com.windscribe.mobile.ui.serverlist.ServerListScreen
import com.windscribe.mobile.ui.serverlist.ServerViewModel
import com.windscribe.mobile.ui.serverlist.ServerViewModelImpl
import com.windscribe.mobile.ui.theme.font12
import com.windscribe.mobile.ui.theme.font16
import com.windscribe.mobile.ui.theme.font26
import com.windscribe.mobile.ui.theme.font9
import com.windscribe.vpn.autoconnection.ProtocolConnectionStatus
import com.windscribe.vpn.autoconnection.ProtocolInformation
import com.windscribe.vpn.backend.Util
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    serverViewModel: ServerViewModel = hiltViewModel<ServerViewModelImpl>(),
    connectionViewmodel: ConnectionViewmodel = hiltViewModel<ConnectionViewmodelImpl>(),
    configViewmodel: ConfigViewmodel = hiltViewModel<ConfigViewmodelImpl>(),
    homeViewmodel: HomeViewmodel = hiltViewModel<HomeViewmodelImpl>(),
    bridgeApiViewModel: BridgeApiViewModel = hiltViewModel<BridgeApiViewModelImpl>(),
) {
    HandleToast(connectionViewmodel)
    HandleGoto(connectionViewmodel, homeViewmodel, bridgeApiViewModel)
    CompactUI(serverViewModel, connectionViewmodel, configViewmodel, homeViewmodel, bridgeApiViewModel)
}

@Composable
fun HandleGoto(
    connectionViewmodel: ConnectionViewmodel,
    homeViewmodel: HomeViewmodel,
    bridgeApiViewModel: BridgeApiViewModel,
) {
    val connectionGoto by connectionViewmodel.goto.collectAsState(initial = null)
    val homeGoto by homeViewmodel.goto.collectAsState(initial = null)
    val bridgeApiGoto by bridgeApiViewModel.goto.collectAsState(initial = null)
    HandleGotoAction(goto = connectionGoto, homeViewmodel, connectionViewmodel, bridgeApiViewModel)
    HandleGotoAction(goto = homeGoto, homeViewmodel, connectionViewmodel, bridgeApiViewModel)
    HandleGotoAction(goto = bridgeApiGoto, homeViewmodel, connectionViewmodel, bridgeApiViewModel)
}

@Composable
private fun HandleGotoAction(
    goto: HomeGoto?,
    homeViewmodel: HomeViewmodel,
    connectionViewmodel: ConnectionViewmodel,
    bridgeApiViewModel: BridgeApiViewModel,
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    var didNavigate = false
    when (goto) {
        HomeGoto.Banned -> {
            val bannedData =
                AccountStatusDialogData(
                    title = stringResource(com.windscribe.vpn.R.string.you_ve_been_banned),
                    icon = R.drawable.garry_account_ban,
                    description = stringResource(com.windscribe.vpn.R.string.you_ve_violated_our_terms),
                    showSecondaryButton = true,
                    secondaryText = stringResource(com.windscribe.vpn.R.string.close),
                    showPrimaryButton = false,
                    primaryText = "",
                )
            navigateWithData(navController, Screen.AccountStatus.route, bannedData)
            didNavigate = true
        }

        HomeGoto.Downgraded -> {
            val downgradedData =
                AccountStatusDialogData(
                    title = stringResource(com.windscribe.vpn.R.string.you_r_pro_plan_expired),
                    icon = R.drawable.garry_downgraded,
                    description = stringResource(com.windscribe.vpn.R.string.you_ve_been_downgraded_to_free_for_now),
                    showSecondaryButton = true,
                    secondaryText = stringResource(com.windscribe.vpn.R.string.back),
                    showPrimaryButton = true,
                    primaryText = stringResource(com.windscribe.vpn.R.string.renew_plan),
                )
            navigateWithData(navController, Screen.AccountStatus.route, downgradedData)
            didNavigate = true
        }

        is HomeGoto.Expired -> {
            val expireData =
                AccountStatusDialogData(
                    title = stringResource(com.windscribe.vpn.R.string.you_re_out_of_data),
                    icon = R.drawable.garry_account_no_data,
                    description =
                        stringResource(
                            com.windscribe.vpn.R.string.upgrade_to_stay_protected,
                            goto.date,
                        ),
                    showSecondaryButton = true,
                    secondaryText = stringResource(com.windscribe.vpn.R.string.back),
                    showPrimaryButton = true,
                    primaryText = stringResource(com.windscribe.vpn.R.string.upgrade_case_normal),
                )
            navigateWithData(navController, Screen.AccountStatus.route, expireData)
            didNavigate = true
        }

        is HomeGoto.EditCustomConfig -> {
            navController.currentBackStackEntry?.savedStateHandle?.set("config_id", goto.id)
            navController.currentBackStackEntry?.savedStateHandle?.set("connect", goto.connect)
            navController.navigate(Screen.EditCustomConfig.route)
            didNavigate = true
        }

        is HomeGoto.Upgrade -> {
            navController.navigate(Screen.Upgrade.route)
            didNavigate = true
        }

        HomeGoto.PowerWhitelist -> {
            navController.navigate(Screen.PowerWhitelist.route)
            didNavigate = true
        }

        HomeGoto.ShareAppLink -> {
            navController.navigate(Screen.ShareLink.route)
            didNavigate = true
        }

        HomeGoto.LocationMaintenance -> {
            navController.navigate(Screen.LocationUnderMaintenance.route)
            didNavigate = true
        }

        HomeGoto.MainMenu -> {
            navController.navigate(Screen.MainMenu.route)
            didNavigate = true
        }

        is HomeGoto.UpdateAvailable -> {
            navController.currentBackStackEntry?.savedStateHandle?.apply {
                set("latest_version", goto.latestVersion)
                set("force_upgrade", goto.force)
            }
            // Foreground re-trigger can fire repeatedly — keep at most one instance.
            if (goto.force) {
                navController.navigate(Screen.UpdateAvailable.route) {
                    popUpTo(Screen.UpdateAvailable.route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(Screen.UpdateAvailable.route)
            }
            didNavigate = true
        }

        is HomeGoto.IpActionError -> {
            navController.currentBackStackEntry?.savedStateHandle?.apply {
                set("message", goto.message)
                set("description", goto.description)
            }
            navController.navigate(Screen.IpActionResult.route)
            didNavigate = true
        }

        HomeGoto.None -> {}

        null -> {}
    }

    if (didNavigate) {
        connectionViewmodel.onGoToHandled()
        homeViewmodel.onGoToHandled()
        bridgeApiViewModel.onGoToHandled()
    }
}

private fun navigateWithData(
    navController: NavController,
    route: String,
    data: AccountStatusDialogData,
) {
    navController.currentBackStackEntry?.savedStateHandle?.set("accountStatusDialogData", data)
    navController.navigate(route)
}

@Composable
fun HandleToast(connectionViewmodel: ConnectionViewmodel) {
    val context = LocalContext.current
    val toastMessage by connectionViewmodel.toastMessage.collectAsState()
    LaunchedEffect(toastMessage) {
        when (toastMessage) {
            is ToastMessage.Localized -> {
                Toast
                    .makeText(
                        context,
                        (toastMessage as ToastMessage.Localized).message,
                        Toast.LENGTH_SHORT,
                    ).show()
                connectionViewmodel.clearToast()
            }

            is ToastMessage.Raw -> {
                Toast
                    .makeText(
                        context,
                        (toastMessage as ToastMessage.Raw).message,
                        Toast.LENGTH_SHORT,
                    ).show()
                connectionViewmodel.clearToast()
            }

            else -> {}
        }
    }
}

@Composable
private fun Background(content: @Composable () -> Unit) {
    AppWallpaperBackground(modifier = Modifier.fillMaxSize()) {
        // Draw the wallpaper edge-to-edge; only the interactive content observes the nav inset.
        Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            content()
        }
    }
}

@Composable
private fun CompactUI(
    serverViewModel: ServerViewModel,
    connectionViewmodel: ConnectionViewmodel,
    configViewmodel: ConfigViewmodel,
    homeViewmodel: HomeViewmodel,
    bridgeApiViewModel: BridgeApiViewModel,
) {
    ForceStatusBarIcons()
    val searchState by serverViewModel.showSearchView.collectAsState()
    Background {
        Box(modifier = Modifier.fillMaxSize()) {
            ConnectedBackground(connectionViewmodel)
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Box(modifier = Modifier.fillMaxWidth().height(276.dp).clipToBounds()) {
                    LocationImage(connectionViewmodel)
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.10f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.38f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
                                ),
                            ),
                        ),
                    )
                    Column(modifier = Modifier.fillMaxSize()) {
                        Header(connectionViewmodel, homeViewmodel)
                        ConnectionStatusSheet(connectionViewmodel)
                        Spacer(modifier = Modifier.weight(1f))
                        NetworkInfoSheet(connectionViewmodel, homeViewmodel, bridgeApiViewModel)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 18.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f).height(95.dp).padding(end = 12.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                                tonalElevation = 1.dp,
                            ) {
                                Box(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        LocationName(connectionViewmodel)
                                    }
                            }
                            AppConnectButton(connectionViewmodel)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Color.Transparent,
                ) {
                    ServerListScreen(
                        viewModel = serverViewModel,
                        connectionViewModel = connectionViewmodel,
                        bridgeApiViewModel = bridgeApiViewModel,
                        configViewmodel = configViewmodel,
                        homeViewmodel = homeViewmodel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            IPContextMenu(bridgeApiViewModel)
            if (searchState) SearchServerList(serverViewModel, connectionViewmodel, homeViewmodel)
        }
    }
}

@Composable
private fun ConnectionStatusSheet(connectionViewmodel: ConnectionViewmodel) {
    val state by connectionViewmodel.connectionUIState.collectAsState()
    val isDecoyTrafficEnabled by connectionViewmodel.isDecoyTrafficEnabled.collectAsState()
    val protocolTweaksEnabled by connectionViewmodel.isProtocolTweaksEnabled.collectAsState()
    val preferredProtocolEnabled by connectionViewmodel.isPreferredProtocolEnabled.collectAsState()
    ConnectionStatusSheetContent(
        state = state,
        isDecoyTrafficEnabled = isDecoyTrafficEnabled,
        isProtocolTweaksEnabled = protocolTweaksEnabled,
        isPreferredProtocolEnabled = preferredProtocolEnabled,
        onProtocolChangeClick = { connectionViewmodel.onProtocolChangeClick() },
    )
}

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
private fun ConnectionStatusSheetContent(
    state: ConnectionUIState,
    isDecoyTrafficEnabled: Boolean,
    isProtocolTweaksEnabled: Boolean,
    isPreferredProtocolEnabled: Boolean,
    onProtocolChangeClick: () -> Unit,
) {
    val containerColor = if (state is ConnectionUIState.Connected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
    val showDecoyTraffic = state is ConnectionUIState.Connected && isDecoyTrafficEnabled
    Surface(
        modifier = Modifier.padding(start = 18.dp, top = 4.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConnectionStatus(state)
            Spacer(modifier = Modifier.width(if (isProtocolTweaksEnabled || showDecoyTraffic) 4.dp else 12.dp))
            if (isProtocolTweaksEnabled) {
                Image(
                    painter = painterResource(if (state is ConnectionUIState.Connected) R.drawable.ic_anti_censorship_enabled else R.drawable.ic_anti_censorship_disabled),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(containerColor),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (showDecoyTraffic) {
                Text(stringResource(com.windscribe.vpn.R.string.decoy), style = font12, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "${Util.getProtocolLabel(state.protocolInfo?.protocol ?: "")}  ${state.protocolInfo?.port}",
                style = font12.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = containerColor,
            )
            Spacer(modifier = Modifier.width(4.dp))
            if (isPreferredProtocolEnabled) {
                Image(
                    painter = painterResource(if (state is ConnectionUIState.Connected) R.drawable.ic_preferred_protocol_status_enabled else R.drawable.ic_preferred_protocol_status_disabled),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(containerColor),
                )
            }
            Image(
                painter = painterResource(R.drawable.arrow_right_small),
                contentDescription = stringResource(com.windscribe.vpn.R.string.protocol_change),
                modifier = Modifier.size(24.dp).semantics { role = Role.Button }.hapticClickable { onProtocolChangeClick() }.testTag("protocol_switcher"),
                contentScale = ContentScale.None,
                colorFilter = ColorFilter.tint(containerColor.copy(alpha = 0.4f)),
            )
        }
    }
}

@Composable
private fun LocationName(connectionViewmodel: ConnectionViewmodel) {
    val state by connectionViewmodel.connectionUIState.collectAsState()
    LocationNameContent(
        locationInfo = state.locationInfo,
        onSingleLineComputed = { isSingleLine ->
            connectionViewmodel.setIsSingleLineLocationName(isSingleLine)
        },
    )
}

@Composable
private fun LocationNameContent(
    locationInfo: LocationInfoState?,
    onSingleLineComputed: (Boolean) -> Unit,
) {
    val info = (locationInfo as? LocationInfoState.Success)?.locationInfo
    val nodeName = info?.nodeName ?: ""
    val nickname = info?.nickName ?: ""
    val density = LocalDensity.current
    // LocalWindowInfo.containerSize is measured in pixels; do not reinterpret it as dp.
    val screenWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()
    val maxWidthPx = (screenWidthPx - with(density) { 150.dp.toPx() }).coerceAtLeast(0f)
    val isSingleLine = remember(nodeName, nickname, maxWidthPx, density) {
        fitsInOneLine("$nodeName $nickname", 26.0f, maxWidthPx, density)
    }
    LaunchedEffect(isSingleLine) { onSingleLineComputed(isSingleLine) }
    if (isSingleLine) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = nodeName,
                style = font26.copy(textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (nickname.isNotBlank()) {
                Text(" $nickname", maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = font26.copy(fontWeight = FontWeight.Normal, textAlign = TextAlign.Start),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        Column {
            Text(nodeName, style = font26.copy(textAlign = TextAlign.Start, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (nickname.isNotBlank()) {
                Text(nickname, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = font26.copy(fontWeight = FontWeight.Normal, textAlign = TextAlign.Start),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
internal fun NetworkInfoSheet(
    connectionViewmodel: ConnectionViewmodel,
    homeViewmodel: HomeViewmodel,
    bridgeApiViewModel: BridgeApiViewModel,
) {
    val showContextMenu by bridgeApiViewModel.ipContextMenuState.collectAsState()
    val isBridgeApiReady by bridgeApiViewModel.bridgeApiReady.collectAsState()
    val hideIp by homeViewmodel.hideIp.collectAsState()
    // Keep the displayed address and copied address sourced from the same state.
    val ipAddress by connectionViewmodel.ipState.collectAsState()
    val context = LocalContext.current
    var showIpAndIcon by remember { mutableStateOf(true) }

    LaunchedEffect(showContextMenu.first) {
        if (showContextMenu.first) {
            // Menu opened, hide IP and icon immediately
            showIpAndIcon = false
        } else {
            // Menu closed, wait for animation to finish before showing IP and icon
            delay(600)
            showIpAndIcon = true
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
    ) {
        NetworkNameSheet(connectionViewmodel, homeViewmodel)
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!showContextMenu.first && showIpAndIcon) {
                Box(
                    modifier =
                        Modifier
                            .pointerInput(hideIp, ipAddress) {
                                detectTapGestures(
                                    onTap = {
                                        if (!hideIp) {
                                            val clipboard =
                                                context.getSystemService(
                                                    android.content.Context.CLIPBOARD_SERVICE,
                                                ) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("IP Address", ipAddress)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "IP address copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onDoubleTap = {
                                        homeViewmodel.onHideIpClick()
                                    },
                                )
                            },
                ) {
                    AnimatedIPAddress(
                        connectionViewmodel = connectionViewmodel,
                        bridgeApiViewModel = bridgeApiViewModel,
                        style = font16.copy(
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontFeatureSettings = "tnum",
                            textAlign = TextAlign.End,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier.width(132.dp).graphicsLayer {
                                renderEffect =
                                    if (hideIp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        BlurEffect(15f, 15f)
                                    } else {
                                        null
                                    }
                            },
                    )

                    // Overlay box for Android 10 and below - only covers the text
                    if (hideIp && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 1.0f),
                                        shape = MaterialTheme.shapes.extraSmall,
                                    ),
                        )
                    }
                }
            }
            if (!showContextMenu.first && showIpAndIcon) {
                Image(
                    painter = painterResource(R.drawable.ic_context),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .alpha(if (isBridgeApiReady) 1.0f else 0.3f)
                            .size(24.dp)
                            .onGloballyPositioned { layoutCoordinates ->
                                bridgeApiViewModel.onIpContextMenuPosition(layoutCoordinates.positionInRoot())
                            }.hapticClickable {
                                if (isBridgeApiReady) {
                                    bridgeApiViewModel.setContextMenuState(true)
                                }
                            },
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatus(connectionUIState: ConnectionUIState) {
    val containerColor =
        if (connectionUIState is ConnectionUIState.Connected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
    val stateTag =
        when (connectionUIState) {
            is ConnectionUIState.Connected -> "connection_status_connected"
            is ConnectionUIState.Connecting -> "connection_status_connecting"
            else -> "connection_status_disconnected"
        }
    Box(
        modifier =
            Modifier
                .testTag(stateTag)
                .border(
                    width = 1.dp,
                    color = containerColor.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(size = 34.dp),
                ).width(39.dp)
                .height(21.dp)
                .background(
                    color = containerColor.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(size = 34.dp),
                ).padding(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 3.dp),
    ) {
        when (connectionUIState) {
            is ConnectionUIState.Connected -> {
                Text(
                    text = "ON",
                    style = font12.copy(fontWeight = FontWeight.SemiBold),
                    color = containerColor,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is ConnectionUIState.Connecting -> {
                Dots(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                Text(
                    text = "OFF",
                    style = font12.copy(fontWeight = FontWeight.SemiBold),
                    color = containerColor,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun ConnectedBackground(connectionViewmodel: ConnectionViewmodel) {
    val state by connectionViewmodel.connectionUIState.collectAsState()
    ConnectedBackgroundContent(connectionUIState = state)
}

@Composable
private fun ConnectedBackgroundContent(connectionUIState: ConnectionUIState) {
    if (connectionUIState is ConnectionUIState.Connected) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(getStatusBarHeight() + 276.dp)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.background,
                                    ),
                            ),
                    ),
        )
    }
}

@Composable
private fun Header(
    connectionViewmodel: ConnectionViewmodel,
    homeViewmodel: HomeViewmodel,
) {
    val navController = LocalNavController.current
    val state by connectionViewmodel.connectionUIState.collectAsState()
    val userState by homeViewmodel.userState.collectAsState()
    val newsfeedCount by connectionViewmodel.newFeedCount.collectAsState()
    HeaderContent(
        connectionUIState = state,
        userState = userState,
        newsfeedCount = newsfeedCount,
        onMainMenuClick = { homeViewmodel.onMainMenuClick() },
        onNewsfeedClick = { navController.navigate(Screen.Newsfeed.route) },
    )
}

@Composable
private fun HeaderContent(
    connectionUIState: ConnectionUIState,
    userState: UserState,
    newsfeedCount: Int,
    onMainMenuClick: () -> Unit,
    onNewsfeedClick: () -> Unit,
) {
    val isConnected = connectionUIState is ConnectionUIState.Connected
    val headerContentColor = MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(58.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        contentColor = headerContentColor,
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).testTag("main_menu_button").clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                    .hapticClickable { onMainMenuClick() },
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(com.windscribe.vpn.R.string.preferences),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(23.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text("Windscribe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = headerContentColor, maxLines = 1)
                Text(
                    text = stringResource(if (isConnected) R.string.home_secure_connection else R.string.home_private_by_design),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (userState is UserState.Pro) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { onNewsfeedClick() }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Text("PRO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            androidx.compose.material3.BadgedBox(
                badge = {
                    if (newsfeedCount > 0) {
                        androidx.compose.material3.Badge { Text(newsfeedCount.coerceAtMost(99).toString(), style = MaterialTheme.typography.labelSmall) }
                    }
                },
            ) {
                androidx.compose.material3.IconButton(onClick = onNewsfeedClick, modifier = Modifier.testTag("newsfeed_open")) {
                    androidx.compose.material3.Icon(imageVector = Icons.Filled.Notifications, contentDescription = stringResource(com.windscribe.vpn.R.string.news_feed), tint = headerContentColor)
                }
            }
        }
    }
}

@Composable
private fun IPContextMenu(bridgeApiViewModel: BridgeApiViewModel) {
    val context = LocalContext.current
    val ipContextMenuState by bridgeApiViewModel.ipContextMenuState.collectAsState()
    val hasPinnedIp by bridgeApiViewModel.hasPinnedIp.collectAsState()

    IPContextMenuContent(
        ipContextMenuState = ipContextMenuState,
        hasPinnedIp = hasPinnedIp,
        onPinIPClick = {
            bridgeApiViewModel.onPinIPClick(
                onSuccess = { _, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
            )
        },
        onRotateIpClick = {
            bridgeApiViewModel.onRotateIpClick(
                onSuccess = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
            )
        },
        onCloseClick = { bridgeApiViewModel.setContextMenuState(false) },
    )
}

@Composable
private fun IPContextMenuContent(
    ipContextMenuState: Pair<Boolean, Offset>,
    hasPinnedIp: Boolean,
    onPinIPClick: () -> Unit,
    onRotateIpClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val density = LocalDensity.current
    val menuWidth = 112.dp
    val menuHeight = 40.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val menuWidthPx = with(density) { menuWidth.toPx() }
        val menuHeightPx = with(density) { menuHeight.toPx() }
        val contentWidthPx = with(density) { maxWidth.toPx() }
        val contentHeightPx = with(density) { maxHeight.toPx() }

        Box(
            modifier = Modifier.offset {
                val x = ipContextMenuState.second.x
                val y = ipContextMenuState.second.y
                if (!x.isFinite() || !y.isFinite()) {
                    IntOffset.Zero
                } else {
                    val maxX = (contentWidthPx - menuWidthPx).coerceAtLeast(0f)
                    val maxY = (contentHeightPx - menuHeightPx).coerceAtLeast(0f)
                    IntOffset(
                        (x - menuWidthPx).coerceIn(0f, maxX).roundToInt(),
                        y.coerceIn(0f, maxY).roundToInt(),
                    )
                }
            },
        ) {
            AnimatedVisibility(
                visible = ipContextMenuState.first,
                enter =
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit =
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    ) + fadeOut(animationSpec = tween(durationMillis = 400)),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IPMenuItem(
                        icon = if (hasPinnedIp) R.drawable.ic_fav_selected else R.drawable.ic_fav,
                        contentDescription = if (hasPinnedIp) "Marked as Favourite" else "Mark as Favourite",
                        onClick = onPinIPClick,
                    )
                    IPMenuItem(
                        icon = R.drawable.refresh,
                        contentDescription = "Refresh IP",
                        onClick = onRotateIpClick,
                    )
                    IPMenuItem(
                        icon = R.drawable.ic_search_location_close,
                        contentDescription = "Close Menu",
                        onClick = onCloseClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun IPMenuItem(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, color = MaterialTheme.colorScheme.onSurface),
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun Dots(modifier: Modifier) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(600)
            currentIndex = (currentIndex + 1) % 3
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        repeat(3) { index ->
            val alpha =
                animateFloatAsState(
                    targetValue = if (index == currentIndex) 1f else 0.4f,
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                    label = "dotAlpha",
                )

            Box(
                modifier =
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = alpha.value)),
            )
        }
    }
}

private val previewProtocol =
    ProtocolInformation(
        protocol = "WireGuard",
        port = "443",
        description = "",
        type = ProtocolConnectionStatus.Connected,
    )

private val previewLocation =
    LocationInfoState.Success(
        LocationInfo(
            country = "United States",
            nodeName = "New York",
            nickName = "Liberty",
            locationBackground = LocationBackground.Flag(R.drawable.dummy_flag),
        ),
    )

/**
 * Connected vs disconnected status sheet, so the preview shows the green "ON" pill with the
 * protocol/port label and the grey "OFF" variant side by side.
 */
private class ConnectionStateProvider : PreviewParameterProvider<ConnectionUIState> {
    override val values =
        sequenceOf(
            ConnectionUIState.Connected(previewProtocol, previewLocation),
            ConnectionUIState.Disconnected(previewProtocol, previewLocation),
        )
}

@Composable
@MultiDevicePreview
private fun ConnectionStatusSheetContentPreview(
    @PreviewParameter(ConnectionStateProvider::class) state: ConnectionUIState,
) {
    PreviewWithNav {
        ConnectionStatusSheetContent(
            state = state,
            isDecoyTrafficEnabled = true,
            isProtocolTweaksEnabled = true,
            isPreferredProtocolEnabled = true,
            onProtocolChangeClick = {},
        )
    }
}

@Composable
@MultiDevicePreview
private fun LocationNameContentPreview() {
    PreviewWithNav {
        LocationNameContent(
            locationInfo = previewLocation,
            onSingleLineComputed = {},
        )
    }
}
