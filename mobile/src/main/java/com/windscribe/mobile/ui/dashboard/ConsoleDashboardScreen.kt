package com.windscribe.mobile.ui.dashboard

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers


import com.windscribe.mobile.ui.common.GlassBorderColor
import com.windscribe.mobile.ui.common.PurpleAccent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas

import com.windscribe.vpn.Windscribe

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.TrafficStats
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.FloatingGlassDock
import com.windscribe.mobile.ui.common.GlassNestedColor
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.GlassSurfaceColor
import com.windscribe.mobile.ui.common.LocalGlassColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import com.windscribe.mobile.ui.common.getLatencyColor

import com.windscribe.mobile.ui.common.CyberProBadge
import com.windscribe.mobile.ui.common.CyberSegmented
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.NeonYellow
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.common.countryCodeToEmoji
import com.windscribe.mobile.ui.common.countryNameToZh
import com.windscribe.mobile.ui.common.formatTrafficSpeed
import com.windscribe.vpn.commonutils.FlagIconResource
import androidx.compose.ui.draw.alpha
import com.windscribe.mobile.ui.connection.BridgeApiViewModel
import com.windscribe.mobile.ui.connection.BridgeApiViewModelImpl
import com.windscribe.mobile.ui.connection.ConnectionUIState
import com.windscribe.mobile.ui.connection.ConnectionViewmodel
import com.windscribe.mobile.ui.connection.ConnectionViewmodelImpl
import com.windscribe.mobile.ui.connection.LocationInfoState
import com.windscribe.mobile.ui.helper.ForceStatusBarIcons
import com.windscribe.mobile.ui.home.HandleGoto
import com.windscribe.mobile.ui.home.HandleToast
import com.windscribe.mobile.ui.home.HomeViewmodel
import com.windscribe.mobile.ui.home.HomeViewmodelImpl
import com.windscribe.mobile.ui.home.UserState
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.mobile.ui.preferences.main.MainMenuViewModel
import com.windscribe.mobile.ui.preferences.main.MainMenuViewModelImpl
import com.windscribe.mobile.ui.serverlist.ListState
import com.windscribe.mobile.ui.serverlist.ServerListItem
import com.windscribe.mobile.ui.serverlist.ServerListType
import com.windscribe.mobile.ui.serverlist.ServerViewModel
import com.windscribe.mobile.ui.serverlist.ServerViewModelImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private object DashboardSessionState {
    var connectedStartTime: Long? = null
    var sessionTxBytes: Long = 0L
    var sessionRxBytes: Long = 0L
    var txSpeedText: String = "0B/s"
    var rxSpeedText: String = "0B/s"
    val speedHistory = mutableListOf<Float>(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
}


/**
 * 100% Cyber-Glassmorphism Dashboard Screen
 * High-performance, categorized navigation, real ViewModel bindings.
 * Strictly aligned 1:1 with user-approved prototype screenshots.
 */
@Composable
fun ConsoleDashboardScreen(
    serverViewModel: ServerViewModel = hiltViewModel<ServerViewModelImpl>(),
    connectionViewmodel: ConnectionViewmodel = hiltViewModel<ConnectionViewmodelImpl>(),
    homeViewmodel: HomeViewmodel = hiltViewModel<HomeViewmodelImpl>(),
    bridgeApiViewModel: BridgeApiViewModel = hiltViewModel<BridgeApiViewModelImpl>(),
    mainMenuViewModel: MainMenuViewModel = hiltViewModel<MainMenuViewModelImpl>(),
) {
    ForceStatusBarIcons(true)
    HandleToast(connectionViewmodel)
    HandleGoto(connectionViewmodel, homeViewmodel, bridgeApiViewModel)

    var currentTabIndex by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    // Intercept Back Press:
    // - In Tab 1 (Nodes) or Tab 2 (Settings): navigate back to Tab 0 (Console)
    // - In Tab 0 (Console): double-tap within 2000ms with Toast to exit the app
    BackHandler(enabled = true) {
        if (currentTabIndex != 0) {
            currentTabIndex = 0
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "再次点击返回键退出程序", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Tab contents with smooth cross-fade animation
        AnimatedContent(
            targetState = currentTabIndex,
            transitionSpec = {
                (fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing)) +
                 scaleIn(initialScale = 0.99f, animationSpec = tween(200)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) +
                        scaleOut(targetScale = 0.99f, animationSpec = tween(160))
                    )
            },
            label = "MainTabsCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            when (tabIndex) {
                0 -> ConsoleTab(
                    connectionViewmodel = connectionViewmodel,
                    serverViewModel = serverViewModel,
                    homeViewmodel = homeViewmodel,
                    bridgeApiViewModel = bridgeApiViewModel,
                    onSwitchToNodes = { currentTabIndex = 1 }
                )
                1 -> CyberNodesMatrix(
                    serverViewModel = serverViewModel,
                    connectionViewmodel = connectionViewmodel,
                    homeViewmodel = homeViewmodel
                )
                2 -> SettingsTab(
                    homeViewmodel = homeViewmodel,
                    connectionViewmodel = connectionViewmodel,
                    mainMenuViewModel = mainMenuViewModel
                )
            }
        }

        // Floating Action Button (FAB) only on Dashboard Tab (0)
        val connStateForFab by connectionViewmodel.connectionUIState.collectAsState()
        val isConnectedForFab = connStateForFab is ConnectionUIState.Connected
        val isConnectingForFab = connStateForFab is ConnectionUIState.Connecting

        if (currentTabIndex == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 78.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PurpleAccent)
                        .clickable { connectionViewmodel.onConnectButtonClick() },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isConnectingForFab -> CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        isConnectedForFab -> Canvas(modifier = Modifier.size(18.dp)) {
                            drawRoundRect(
                                color = Color.White,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )
                        }
                        else -> Canvas(modifier = Modifier.size(22.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.28f, size.height * 0.18f)
                                lineTo(size.width * 0.85f, size.height * 0.50f)
                                lineTo(size.width * 0.28f, size.height * 0.82f)
                                close()
                            }
                            drawPath(path, color = Color.White)
                        }
                    }
                }
            }
        }

        // Fixed Solid Glass Dock at bottom (Zero click penetration / 100% interception)
        FloatingGlassDock(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentTabIndex = currentTabIndex,
            onTabSelected = { currentTabIndex = it }
        )
    }
}

/**
 * Tab 0: Core Cyber Dashboard (Strict 1:1 Match with Prototype Screenshot)
 */
@Composable
private fun ConsoleTab(
    connectionViewmodel: ConnectionViewmodel,
    serverViewModel: ServerViewModel,
    homeViewmodel: HomeViewmodel,
    bridgeApiViewModel: BridgeApiViewModel,
    onSwitchToNodes: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val connectionState by connectionViewmodel.connectionUIState.collectAsState()
    val isConnected = connectionState is ConnectionUIState.Connected
    val isConnecting = connectionState is ConnectionUIState.Connecting

    val selectedCityId by connectionViewmodel.selectedCityId.collectAsState()
    val ipState by connectionViewmodel.ipState.collectAsState()
    val serverState by serverViewModel.serverListState.collectAsState()

    var isDetectingNetwork by remember { mutableStateOf(false) }

    // Location / Node information
    val locationInfo = (connectionState.locationInfo as? LocationInfoState.Success)?.locationInfo
    val fallbackItems = (serverState as? ListState.Success<ServerListItem>)?.data
    val fallbackCity = fallbackItems
        ?.flatMap { it.datacenters }
        ?.firstOrNull { it.id == selectedCityId }
    val fallbackRegion = fallbackItems
        ?.firstOrNull { it.datacenters.any { dc -> dc.id == selectedCityId } }?.region

    val nodeName = if (isConnected) {
        locationInfo?.nodeName
            ?: fallbackCity?.nodeName
            ?: countryNameToZh(fallbackRegion?.name)
            ?: "直连节点"
    } else {
        "本地直连网络"
    }
    val nickname = if (isConnected) {
        locationInfo?.nickName
            ?: fallbackCity?.nickName
            ?: "已连接"
    } else {
        "未开启代理"
    }
    val flagEmoji = if (isConnected) {
        countryCodeToEmoji(
            locationInfo?.country
                ?: fallbackRegion?.countryCode
                ?: "US"
        )
    } else {
        "🌐"
    }

    // Real-time Traffic Speeds & duration backed by DashboardSessionState across Tab switches
    var txSpeedText by remember { mutableStateOf(DashboardSessionState.txSpeedText) }
    var rxSpeedText by remember { mutableStateOf(DashboardSessionState.rxSpeedText) }
    var sessionTxBytes by remember { androidx.compose.runtime.mutableLongStateOf(DashboardSessionState.sessionTxBytes) }
    var sessionRxBytes by remember { androidx.compose.runtime.mutableLongStateOf(DashboardSessionState.sessionRxBytes) }
    val speedHistory = remember {
        mutableStateListOf<Float>().apply {
            addAll(DashboardSessionState.speedHistory)
        }
    }

    var durationSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            if (DashboardSessionState.connectedStartTime == null) {
                DashboardSessionState.connectedStartTime = System.currentTimeMillis()
            }
            while (isActive) {
                val start = DashboardSessionState.connectedStartTime ?: System.currentTimeMillis()
                durationSeconds = (System.currentTimeMillis() - start) / 1000
                delay(1000)
            }
        } else {
            DashboardSessionState.connectedStartTime = null
            DashboardSessionState.sessionTxBytes = 0L
            DashboardSessionState.sessionRxBytes = 0L
            DashboardSessionState.txSpeedText = "0B/s"
            DashboardSessionState.rxSpeedText = "0B/s"
            DashboardSessionState.speedHistory.clear()
            repeat(16) { DashboardSessionState.speedHistory.add(0f) }
            durationSeconds = 0L
        }
    }

    val durationText = if (isConnected) {
        val h = durationSeconds / 3600
        val m = (durationSeconds % 3600) / 60
        val s = durationSeconds % 60
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        "--:--:--"
    }

    // Traffic speed polling and session traffic accumulation
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            txSpeedText = "0B/s"
            rxSpeedText = "0B/s"
            DashboardSessionState.txSpeedText = "0B/s"
            DashboardSessionState.rxSpeedText = "0B/s"
            sessionTxBytes = 0L
            sessionRxBytes = 0L
            speedHistory.clear()
            repeat(16) { speedHistory.add(0f) }
            return@LaunchedEffect
        }
        // 保持现存累计值，防止切页被清零
        sessionTxBytes = DashboardSessionState.sessionTxBytes
        sessionRxBytes = DashboardSessionState.sessionRxBytes
        var lastRx = TrafficStats.getTotalRxBytes()
        var lastTx = TrafficStats.getTotalTxBytes()
        while (isActive) {
            delay(1000)
            val curRx = TrafficStats.getTotalRxBytes()
            val curTx = TrafficStats.getTotalTxBytes()
            val rxDelta = if (lastRx > 0 && curRx >= lastRx) (curRx - lastRx) else 0L
            val txDelta = if (lastTx > 0 && curTx >= lastTx) (curTx - lastTx) else 0L
            lastRx = curRx
            lastTx = curTx

            sessionTxBytes += txDelta
            sessionRxBytes += rxDelta
            DashboardSessionState.sessionTxBytes = sessionTxBytes
            DashboardSessionState.sessionRxBytes = sessionRxBytes

            rxSpeedText = formatTrafficSpeed(rxDelta)
            txSpeedText = formatTrafficSpeed(txDelta)
            DashboardSessionState.rxSpeedText = rxSpeedText
            DashboardSessionState.txSpeedText = txSpeedText

            val totalSpeedKb = ((rxDelta + txDelta) / 1024f).coerceAtLeast(0f)
            if (speedHistory.size >= 24) {
                speedHistory.removeAt(0)
            }
            speedHistory.add(totalSpeedKb)
            DashboardSessionState.speedHistory.clear()
            DashboardSessionState.speedHistory.addAll(speedHistory)
        }
    }

    // Local IP
    var lanIp by remember { mutableStateOf("--.--.--.--") }
    LaunchedEffect(isConnected, ipState) {
        val (ip, _) = withContext(Dispatchers.IO) {
            getLocalIpAddress()
        }
        lanIp = ip
    }
    var isLanHighlighted by remember { mutableStateOf(false) }

    // Exit IP & Public IP (Authentic real IP flow, eliminating hardcoded fallbacks)
    val exitIp = if (isConnected && ipState.isNotBlank()) ipState else (ipState.takeIf { it.isNotBlank() && !it.contains("--") } ?: Windscribe.appContext.preference.userIP ?: "--.--.--.--")

    // Total session traffic bytes
    val totalTrafficBytes = (sessionTxBytes + sessionRxBytes).coerceAtLeast(1)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top App Bar: "仪表盘" + Right Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "仪表盘",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            // Right edit / switch icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSwitchToNodes),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_icon),
                    contentDescription = "Switch Nodes",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card 1: 网络速度 (Top Full-width Card with Waveform Curve)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GlassSurfaceColor)
                .border(1.dp, GlassBorderColor, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "网络速度",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "↑ $txSpeedText   ↓ $rxSpeedText",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Waveform / Area Curve Chart
                val linePath = remember { Path() }
                val areaPath = remember { Path() }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    if (speedHistory.isEmpty()) return@Canvas

                    val maxVal = (speedHistory.maxOrNull() ?: 10f).coerceAtLeast(10f)
                    val stepX = w / (speedHistory.size - 1).coerceAtLeast(1)

                    linePath.reset()
                    areaPath.reset()

                    speedHistory.forEachIndexed { i, speed ->
                        val x = i * stepX
                        val normalized = (speed / maxVal).coerceIn(0f, 1f)
                        val y = h - (normalized * (h - 22f)) - 8f

                        if (i == 0) {
                            linePath.moveTo(x, y)
                            areaPath.moveTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevSpeed = speedHistory[i - 1]
                            val prevNorm = (prevSpeed / maxVal).coerceIn(0f, 1f)
                            val prevY = h - (prevNorm * (h - 22f)) - 8f
                            val midX = (prevX + x) / 2f
                            linePath.cubicTo(midX, prevY, midX, y, x, y)
                            areaPath.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }

                    areaPath.lineTo(w, h)
                    areaPath.lineTo(0f, h)
                    areaPath.close()

                    // Draw area gradient
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(PurpleAccent.copy(alpha = 0.38f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw glowing curve
                    drawPath(
                        path = linePath,
                        color = NeonPurple,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Subtle bottom baseline
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, PurpleAccent.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        start = Offset(0f, h - 1f),
                        end = Offset(w, h - 1f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card: 当前连接的国家与地区 (全屏宽长面板，高度通高 108dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassSurfaceColor)
                .border(1.dp, GlassBorderColor, RoundedCornerShape(18.dp))
                .clickable {
                    if (isConnected) {
                        onSwitchToNodes()
                    } else {
                        connectionViewmodel.onConnectButtonClick()
                    }
                }
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🌐",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "当前连接地区",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    when {
                        isConnected -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "加密隧道已建立",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        isConnecting -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NeonYellow)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "正在建立加密隧道...",
                                    color = NeonYellow,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "待机未连接",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val currentCountryCode = if (isConnected) {
                            locationInfo?.country ?: fallbackRegion?.countryCode ?: "US"
                        } else {
                            fallbackRegion?.countryCode ?: "US"
                        }
                        val flagResId = FlagIconResource.getSmallFlag(currentCountryCode)
                        Image(
                            painter = painterResource(id = flagResId),
                            contentDescription = fallbackRegion?.name ?: "Country",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isConnected) "$nodeName · $nickname" else (fallbackCity?.let { "${countryNameToZh(fallbackRegion?.name)} · ${it.nickName ?: it.nodeName ?: "地区"}" } ?: "未选择地区"),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    isConnected -> fallbackCity?.nodeName ?: "专用节点通信中"
                                    isConnecting -> "正在协商安全连接，点击可取消"
                                    else -> "点击右下角按钮或区域卡片发起连接"
                                },
                                color = if (isConnecting) NeonYellow.copy(alpha = 0.85f) else TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (fallbackCity?.linkSpeed != null && fallbackCity.linkSpeed >= 10000) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x2638BDF8))
                                .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "10G 专线",
                                color = NeonCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2-Column Grid: 方案 B (左侧双 IP 融合卡片 + 连接时间，右侧会话流量通高齐平)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column 1 (Left): 双 IP 融合卡片 (64dp) + 连接时间 (46dp), 间隔 8dp, 合计 118dp
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: 双 IP 融合卡片 (高 64dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLanHighlighted) LocalGlassColors.current.accentMuted else GlassSurfaceColor)
                        .border(
                            1.dp,
                            if (isLanHighlighted) LocalGlassColors.current.accent else GlassBorderColor,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 内网 IP 行 (点击切换高亮)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isLanHighlighted = !isLanHighlighted
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💻",
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "内网",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = lanIp,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 出口 IP 行 (点击刷新)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isDetectingNetwork) {
                                        isDetectingNetwork = true
                                        connectionViewmodel.refreshNetworkDetection()
                                        coroutineScope.launch {
                                            delay(1000)
                                            isDetectingNetwork = false
                                        }
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🌐",
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "出口",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (isDetectingNetwork) "刷新中..." else exitIp,
                                color = if (isDetectingNetwork) Color.White.copy(alpha = 0.5f) else NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Card 2: 连接时间 (高 46dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GlassSurfaceColor)
                        .border(1.dp, GlassBorderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "连接时间",
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = durationText,
                            color = NeonPurple,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Column 2 (Right): 会话流量 (高 118dp，与左列两张卡片通高精准齐平)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(118.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurfaceColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔄",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "会话流量",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Mini Donut Chart
                        Canvas(modifier = Modifier.size(38.dp)) {
                            val strokeW = 4.5.dp.toPx()
                            drawCircle(
                                color = Color(0xFF2B2D3E),
                                style = Stroke(width = strokeW)
                            )
                            val sweep = ((sessionTxBytes.toFloat() / totalTrafficBytes) * 360f).coerceIn(25f, 335f)
                            drawArc(
                                color = NeonPurple,
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Legend & formatted bytes
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "↑ 上传", color = NeonPurple, fontSize = 10.sp)
                                Text(
                                    text = formatTrafficBytes(sessionTxBytes),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "↓ 下载", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = formatTrafficBytes(sessionRxBytes),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom clearance for dock and FAB
        Spacer(modifier = Modifier.height(130.dp))
    }
}
/**
 * Tab 1: 100% Cyber-Glassmorphism Nodes Matrix (Strict 1:1 Match with Screenshot 2)
 */
@Composable
private fun CyberNodesMatrix(
    serverViewModel: ServerViewModel,
    connectionViewmodel: ConnectionViewmodel,
    homeViewmodel: HomeViewmodel,
) {
    val context = LocalContext.current
    val searchKeyword by serverViewModel.searchKeyword.collectAsState()
    val selectedType by serverViewModel.selectedServerListType.collectAsState()
    val isRefreshing by serverViewModel.refreshState.collectAsState()

    // Observe real selected city ID to highlight [已选中]
    val selectedCityId by connectionViewmodel.selectedCityId.collectAsState()

    // Observe user preference for location health / load visibility
    val showLocationLoad by homeViewmodel.showLocationLoad.collectAsState()

    // Observe connection state to give feedback on latency test
    val connectionUIState by connectionViewmodel.connectionUIState.collectAsState()
    val isVpnActive = connectionUIState is ConnectionUIState.Connected || connectionUIState is ConnectionUIState.Connecting

    // Collect server lists & real health/latency states
    val serverState by serverViewModel.serverListState.collectAsState()
    val favState by serverViewModel.favouriteListState.collectAsState()
    val latencyState by serverViewModel.latencyListState.collectAsState()
    val serversState by serverViewModel.serversState.collectAsState()

    val latencyMap = remember(latencyState) { (latencyState as? ListState.Success)?.data?.associateBy { it.id } }
    val serverMap = (serversState as? com.windscribe.vpn.serverlist.entity.ServerMapState.Success)?.data

    val expandedMap = remember { mutableStateMapOf<Int, Boolean>() }
    val favExpandedMap = remember { mutableStateMapOf<Int, Boolean>() }

    // Dynamic counts for total regions and total datacenters
    val totalRegions = remember(serverState) { (serverState as? ListState.Success)?.data?.size ?: 0 }
    val totalDatacenters = remember(serverState) { (serverState as? ListState.Success)?.data?.sumOf { it.datacenters.size } ?: 0 }
    val statsText = if (totalRegions > 0) "$totalRegions 个国家 · $totalDatacenters 个地区" else "正在载入节点数据..."

    val favCount = (favState as? ListState.Success)?.data?.size ?: 0
    val favTabTitle = if (favCount > 0) "收藏 $favCount" else "收藏"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top Header: Title & Total Datacenters Summary + Batch Ping Button (ui_preview_v3.html)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "节点矩阵",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statsText,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Batch Ping Button (Purple themed, unblocked)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1F7C68EE))
                    .border(1.dp, Color(0x4D7C68EE), RoundedCornerShape(16.dp))
                    .clickable {
                        if (!isRefreshing) {
                            serverViewModel.refresh(selectedType)
                        }
                    }
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = NeonPurple,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Text(
                            text = "◈ ",
                            color = NeonPurple,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "一键测速",
                        color = NeonPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full-width Search Input Field (ui_preview_v3.html)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LocalGlassColors.current.surface)
                .border(1.dp, LocalGlassColors.current.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_icon),
                    contentDescription = "Search",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchKeyword,
                    onValueChange = { serverViewModel.onQueryTextChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(NeonCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchKeyword.isEmpty()) {
                            Text(
                                text = "搜索国家、城市或代码...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchKeyword.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                serverViewModel.clearSearch()
                                expandedMap.clear()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 全部 / 收藏 分段控件
        CyberSegmented(
            modifier = Modifier.fillMaxWidth(),
            options = listOf("全部", favTabTitle),
            selectedIndex = if (selectedType == ServerListType.Fav) 1 else 0,
            onSelect = { index ->
                serverViewModel.setSelectedType(
                    if (index == 1) ServerListType.Fav else ServerListType.All
                )
            }
        )

        val filteredServers = remember(serverState, searchKeyword) {
            val list = (serverState as? ListState.Success)?.data ?: return@remember emptyList()
            if (searchKeyword.isBlank()) list
            else {
                list.filter { item ->
                    val regionEnMatch = item.region.name?.contains(searchKeyword, ignoreCase = true) == true
                    val regionZhMatch = countryNameToZh(item.region.name).contains(searchKeyword, ignoreCase = true)
                    val ccMatch = item.region.countryCode?.contains(searchKeyword, ignoreCase = true) == true
                    val cityMatch = item.datacenters.any {
                        (it.nickName?.contains(searchKeyword, ignoreCase = true) == true) ||
                        (it.nodeName?.contains(searchKeyword, ignoreCase = true) == true)
                    }
                    regionEnMatch || regionZhMatch || ccMatch || cityMatch
                }
            }
        }

        val filteredFavs = remember(favState, searchKeyword) {
            val list = (favState as? ListState.Success)?.data ?: return@remember emptyList()
            if (searchKeyword.isBlank()) list
            else {
                list.filter { item ->
                    val nickMatch = item.city.nickName?.contains(searchKeyword, ignoreCase = true) == true
                    val nodeMatch = item.city.nodeName?.contains(searchKeyword, ignoreCase = true) == true
                    val zhMatch = countryNameToZh(item.region.name).contains(searchKeyword, ignoreCase = true)
                    val enMatch = item.region.name?.contains(searchKeyword, ignoreCase = true) == true
                    val ccMatch = (item.countryCode ?: item.region.countryCode)?.contains(searchKeyword, ignoreCase = true) == true
                    nickMatch || nodeMatch || zhMatch || enMatch || ccMatch
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area by Tab
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedType) {
                ServerListType.All -> {
                    when (val state = serverState) {
                        is ListState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp)
                                }
                            }
                        }
                        is ListState.Error -> {
                            item {
                                Text(text = "加载节点失败: ${state.message}", color = Color(0xFFFF5252), fontSize = 12.sp)
                            }
                        }
                        is ListState.Success -> {
                            val itemsList = filteredServers

                            if (itemsList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (searchKeyword.isNotBlank()) "未找到与「$searchKeyword」匹配的节点" else "暂无可用节点",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(itemsList, key = { it.id }) { item ->
                                    val region = item.region
                                    val datacenters = item.datacenters

                                    val regionEnMatch = searchKeyword.isNotBlank() && item.region.name?.contains(searchKeyword, ignoreCase = true) == true
                                    val regionZhMatch = searchKeyword.isNotBlank() && countryNameToZh(item.region.name).contains(searchKeyword, ignoreCase = true)
                                    val ccMatch = searchKeyword.isNotBlank() && item.region.countryCode?.contains(searchKeyword, ignoreCase = true) == true
                                    val countryDirectlyMatched = regionEnMatch || regionZhMatch || ccMatch

                                    // Auto expand if search matches; otherwise user manual toggle
                                    val isExpanded = if (searchKeyword.isNotBlank()) {
                                        expandedMap[item.id] ?: true
                                    } else {
                                        expandedMap[item.id] ?: false
                                    }

                                    // Sub-datacenters to display: If country matched or no search, show all; if only specific cities matched, show matching cities
                                    val displayedDatacenters = if (searchKeyword.isBlank() || countryDirectlyMatched) {
                                        datacenters
                                    } else {
                                        datacenters.filter {
                                            (it.nickName?.contains(searchKeyword, ignoreCase = true) == true) ||
                                            (it.nodeName?.contains(searchKeyword, ignoreCase = true) == true)
                                        }
                                    }

                                    val emoji = countryCodeToEmoji(region.countryCode)

                                    // Latency calculations: Real average and lowest
                                    val validPings = datacenters.mapNotNull { latencyMap?.get(it.id)?.time }.filter { it > 0 }
                                    val minPing = validPings.minOrNull()
                                    val avgPing = if (validPings.isNotEmpty()) validPings.average().toInt() else null
                                    val minPingText = if (minPing != null) "$minPing ms" else "-- ms"
                                    val avgPingText = if (avgPing != null) "$avgPing ms" else "-- ms"
                                    val pingColor = getLatencyColor(minPing)

                                    GlassSurface(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 16.dp,
                                        borderColor = if (isExpanded) NeonCyan.copy(alpha = 0.35f) else LocalGlassColors.current.border,
                                        backgroundColor = LocalGlassColors.current.surface
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Country Card Header: Unified Accordion (Every country expands on tap, NEVER auto-connects)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        // 所有国家统一手风琴：只展开/收起，绝不拨号
                                                        expandedMap[item.id] = !isExpanded
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val flagResId = FlagIconResource.getSmallFlag(region.countryCode)
                                                Image(
                                                    painter = painterResource(id = flagResId),
                                                    contentDescription = region.name,
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(11.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = countryNameToZh(region.name),
                                                        color = TextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${datacenters.size} 个地区 · 平均 $avgPingText",
                                                        color = TextMuted,
                                                        fontSize = 9.5.sp
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(20.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = minPingText,
                                                        color = pingColor,
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isExpanded) "⌃" else "⌄",
                                                    color = TextMuted,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            // Expandable Sub-Datacenters (City Rows)
                                            AnimatedVisibility(
                                                visible = isExpanded,
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 10.dp)
                                                ) {
                                                    displayedDatacenters.forEach { dc ->
                                                        val dcServers = serverMap?.get(dc.id)
                                                        val isMaintenance = (dc.status != 1) || (dcServers.isNullOrEmpty())
                                                        val isSelectedCity = if (isMaintenance) false else (dc.id == selectedCityId)
                                                        val isProNode = dc.pro == 1
                                                        val isTenGbps = dc.linkSpeed >= 10000

                                                        val dcHealth = if (!isMaintenance && !dcServers.isNullOrEmpty()) {
                                                            dcServers.map { it.health }.average().toInt().coerceIn(5, 99)
                                                        } else {
                                                            null
                                                        }

                                                        val dcPing = latencyMap?.get(dc.id)?.time
                                                        val dcPingText = if (isMaintenance) "-- ms" else if (dcPing != null && dcPing > 0) "$dcPing ms" else "-- ms"
                                                        val isFav = (favState as? ListState.Success)?.data
                                                            ?.any { it.city.id == dc.id } == true
                                                        val dcPingColor = if (isMaintenance) TextMuted else getLatencyColor(dcPing)

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    if (isSelectedCity) LocalGlassColors.current.accentMuted else LocalGlassColors.current.nestedSurface
                                                                )
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = if (isSelectedCity) LocalGlassColors.current.accent else LocalGlassColors.current.borderSubtle,
                                                                    shape = RoundedCornerShape(10.dp)
                                                                )
                                                                .then(
                                                                    if (isMaintenance) {
                                                                        Modifier.alpha(0.5f)
                                                                    } else {
                                                                        Modifier.clickable {
                                                                            // 只选中节点，绝不自动发起连接
                                                                            connectionViewmodel.selectCity(dc)
                                                                            Toast.makeText(
                                                                                context,
                                                                                "已选择地区：${dc.nickName ?: dc.nodeName ?: "Node"}，请返回控制台手动连接",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    }
                                                                )
                                                                .padding(horizontal = 11.dp, vertical = 9.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(
                                                                        text = dc.nickName ?: dc.nodeName ?: "Node",
                                                                        color = if (isMaintenance) TextMuted else TextPrimary,
                                                                        fontSize = 12.5.sp,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                    if (isMaintenance) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "🚧 维护中",
                                                                            color = Color(0xFFFBBF24),
                                                                            fontSize = 8.5.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(4.dp))
                                                                                .background(Color(0x26FBBF24))
                                                                                .border(1.dp, Color(0x66FBBF24), RoundedCornerShape(4.dp))
                                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                        )
                                                                    } else {
                                                                        if (isProNode) {
                                                                            Spacer(modifier = Modifier.width(6.dp))
                                                                            CyberProBadge()
                                                                        }
                                                                        if (isTenGbps) {
                                                                            Spacer(modifier = Modifier.width(6.dp))
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(18.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(Color(0x2600F0FF))
                                                                                    .border(1.dp, Color(0x6600F0FF), CircleShape),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Image(
                                                                                    painter = painterResource(id = R.drawable.city_ten_gbps),
                                                                                    contentDescription = "10Gbps",
                                                                                    modifier = Modifier.size(11.dp),
                                                                                    colorFilter = ColorFilter.tint(NeonCyan)
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .width(104.dp)
                                                                            .height(5.dp)
                                                                            .clip(RoundedCornerShape(3.dp))
                                                                            .background(LocalGlassColors.current.borderSubtle)
                                                                    ) {
                                                                        if (dcHealth != null && !isMaintenance) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .height(5.dp)
                                                                                    .fillMaxWidth((dcHealth.coerceIn(5, 99) / 100f))
                                                                                    .background(if (dcHealth >= 70) NeonYellow else NeonGreen)
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text(
                                                                        text = if (isMaintenance || dcHealth == null) "负载 获取失败" else "负载 $dcHealth%",
                                                                        color = TextMuted,
                                                                        fontSize = 9.5.sp
                                                                    )
                                                                }
                                                            }

                                                            Text(
                                                                text = dcPingText,
                                                                color = dcPingColor,
                                                                fontSize = 11.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                    ) { serverViewModel.toggleFavorite(dc) },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    painter = painterResource(
                                                                        id = if (isFav) R.drawable.ic_fav_selected else R.drawable.ic_favourite
                                                                    ),
                                                                    contentDescription = "Favorite",
                                                                    tint = if (isFav) NeonYellow else Color(0x66FFFFFF),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ServerListType.Fav -> {
                    when (val state = favState) {
                        is ListState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp)
                                }
                            }
                        }
                        is ListState.Error -> {
                            item { Text("暂无收藏节点", color = TextMuted, fontSize = 12.sp) }
                        }
                        is ListState.Success -> {

                            if (state.data.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                        Text("暂无收藏的节点，在节点列表中点击星标即可添加", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            } else if (filteredFavs.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                        Text("未找到与「$searchKeyword」匹配的收藏节点", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                val favsByRegion = filteredFavs.groupBy { it.region.id }

                                items(favsByRegion.keys.toList(), key = { it }) { regionId ->
                                    val favGroup = favsByRegion[regionId] ?: return@items
                                    val firstItem = favGroup.first()
                                    val region = firstItem.region
                                    val countryCode = firstItem.countryCode ?: region.countryCode

                                    // Auto expand if search matches; otherwise user manual toggle
                                    val isExpanded = if (searchKeyword.isNotBlank()) {
                                        favExpandedMap[regionId] ?: true
                                    } else {
                                        favExpandedMap[regionId] ?: false
                                    }

                                    val validPings = favGroup.mapNotNull { latencyMap?.get(it.city.id)?.time }.filter { it > 0 }
                                    val minPing = validPings.minOrNull()
                                    val avgPing = if (validPings.isNotEmpty()) validPings.average().toInt() else null
                                    val minPingText = if (minPing != null) "$minPing ms" else "-- ms"
                                    val avgPingText = if (avgPing != null) "$avgPing ms" else "-- ms"
                                    val pingColor = getLatencyColor(minPing)

                                    val flagResId = FlagIconResource.getSmallFlag(countryCode)

                                    GlassSurface(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 16.dp,
                                        borderColor = if (isExpanded) NeonCyan.copy(alpha = 0.35f) else LocalGlassColors.current.border,
                                        backgroundColor = LocalGlassColors.current.surface
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Country Card Header: Unified Accordion
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        favExpandedMap[regionId] = !isExpanded
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = painterResource(id = flagResId),
                                                    contentDescription = region.name,
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(11.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = countryNameToZh(region.name),
                                                        color = TextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${favGroup.size} 个已收藏地区 · 平均 $avgPingText",
                                                        color = TextMuted,
                                                        fontSize = 9.5.sp
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(20.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = minPingText,
                                                        color = pingColor,
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isExpanded) "⌃" else "⌄",
                                                    color = TextMuted,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            // Expandable Sub-Datacenters (Favorite City Rows)
                                            AnimatedVisibility(
                                                visible = isExpanded,
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 10.dp)
                                                ) {
                                                    favGroup.forEach { favItem ->
                                                        val dc = favItem.city
                                                        val dcServers = serverMap?.get(dc.id)
                                                        val isMaintenance = (dc.status != 1) || (dcServers.isNullOrEmpty())
                                                        val isSelectedCity = if (isMaintenance) false else (dc.id == selectedCityId)
                                                        val isProNode = dc.pro == 1
                                                        val isTenGbps = dc.linkSpeed >= 10000

                                                        val dcHealth = if (!isMaintenance && !dcServers.isNullOrEmpty()) {
                                                            dcServers.map { it.health }.average().toInt().coerceIn(5, 99)
                                                        } else {
                                                            null
                                                        }

                                                        val dcPing = latencyMap?.get(dc.id)?.time
                                                        val dcPingText = if (isMaintenance) "-- ms" else if (dcPing != null && dcPing > 0) "$dcPing ms" else "-- ms"
                                                        val dcPingColor = if (isMaintenance) TextMuted else getLatencyColor(dcPing)

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    if (isSelectedCity) LocalGlassColors.current.accentMuted else LocalGlassColors.current.nestedSurface
                                                                )
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = if (isSelectedCity) LocalGlassColors.current.accent else LocalGlassColors.current.borderSubtle,
                                                                    shape = RoundedCornerShape(10.dp)
                                                                )
                                                                .then(
                                                                    if (isMaintenance) {
                                                                        Modifier.alpha(0.5f)
                                                                    } else {
                                                                        Modifier.clickable {
                                                                            // 只选中节点，绝不自动发起连接
                                                                            connectionViewmodel.selectCity(dc)
                                                                            Toast.makeText(
                                                                                context,
                                                                                "已选择地区：${dc.nickName ?: dc.nodeName ?: "Node"}，请返回控制台手动连接",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    }
                                                                )
                                                                .padding(horizontal = 11.dp, vertical = 9.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(
                                                                        text = dc.nickName ?: dc.nodeName ?: "Node",
                                                                        color = if (isMaintenance) TextMuted else TextPrimary,
                                                                        fontSize = 12.5.sp,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                    if (isMaintenance) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Text(
                                                                            text = "🚧 维护中",
                                                                            color = Color(0xFFFBBF24),
                                                                            fontSize = 8.5.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(4.dp))
                                                                                .background(Color(0x26FBBF24))
                                                                                .border(1.dp, Color(0x66FBBF24), RoundedCornerShape(4.dp))
                                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                        )
                                                                    } else {
                                                                        if (isProNode) {
                                                                            Spacer(modifier = Modifier.width(6.dp))
                                                                            CyberProBadge()
                                                                        }
                                                                        if (isTenGbps) {
                                                                            Spacer(modifier = Modifier.width(6.dp))
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(18.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(Color(0x2600F0FF))
                                                                                    .border(1.dp, Color(0x6600F0FF), CircleShape),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Image(
                                                                                    painter = painterResource(id = R.drawable.city_ten_gbps),
                                                                                    contentDescription = "10Gbps",
                                                                                    modifier = Modifier.size(11.dp),
                                                                                    colorFilter = ColorFilter.tint(NeonCyan)
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .width(104.dp)
                                                                            .height(5.dp)
                                                                            .clip(RoundedCornerShape(3.dp))
                                                                            .background(LocalGlassColors.current.borderSubtle)
                                                                    ) {
                                                                        if (dcHealth != null && !isMaintenance) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .height(5.dp)
                                                                                    .fillMaxWidth((dcHealth.coerceIn(5, 99) / 100f))
                                                                                    .background(if (dcHealth >= 70) NeonYellow else NeonGreen)
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text(
                                                                        text = if (isMaintenance || dcHealth == null) "负载 获取失败" else "负载 $dcHealth%",
                                                                        color = TextMuted,
                                                                        fontSize = 9.5.sp
                                                                    )
                                                                }
                                                            }

                                                            Text(
                                                                text = dcPingText,
                                                                color = dcPingColor,
                                                                fontSize = 11.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                    ) { serverViewModel.deleteFavourite(favItem.id) },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    painter = painterResource(id = R.drawable.ic_fav_selected),
                                                                    contentDescription = "Remove Favorite",
                                                                    tint = NeonYellow,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Static & Config tabs removed
                }
            }

            // Extra clearance for FloatingGlassDock
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

/**
 * Tab 2: Categorized Settings Hub (Pure Simplified Chinese, Strict 1:1 Match with Screenshot 3)
 */
@Composable
private fun SettingsTab(
    homeViewmodel: HomeViewmodel,
    connectionViewmodel: ConnectionViewmodel,
    mainMenuViewModel: MainMenuViewModel,
) {
    val navController = LocalNavController.current
    val userState by homeViewmodel.userState.collectAsState()
    val showProgress by mainMenuViewModel.showProgress.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    text = "注销登录",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "确定要退出当前账户登录吗？退出后需要重新输入账户凭据。",
                    color = TextSecondary,
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        mainMenuViewModel.logout()
                    }
                ) {
                    Text(
                        text = "确认退出",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showLogoutConfirm = false }
                ) {
                    Text(
                        text = "取消",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            },
            containerColor = Color(0xFF1A1B26),
            shape = RoundedCornerShape(18.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Header (设置)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "设置",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 账户概览卡 (尊贵黑金镶金 S 标)
        val isProUser = userState is UserState.Pro || userState is UserState.UnlimitedData
        val planTitle = when (val st = userState) {
            is UserState.Pro -> "PRO 专业版"
            is UserState.UnlimitedData -> "无限流量专线"
            is UserState.Free -> if (st.isGhost) "临时账户" else "免费账户"
            else -> "账户"
        }
        val planSubtitle = when (val st = userState) {
            is UserState.Free -> "剩余流量 " + st.dataLeft
            is UserState.Pro -> "尊贵黑金订阅有效 · 专属高速通道"
            is UserState.UnlimitedData -> "尊贵黑金订阅有效 · 专属高速通道"
            else -> "点击查看账户详情"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x33594511),
                            Color(0x221E190B),
                            LocalGlassColors.current.surface
                        )
                    )
                )
                .border(1.dp, Color(0x66FFD700), RoundedCornerShape(18.dp))
                .clickable { navController.navigate(Screen.Account.route) }
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFE259), Color(0xFFFFA751))
                            )
                        )
                        .border(1.dp, Color(0xFFFFF1A8), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.Black,
                        fontSize = 21.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = planTitle,
                            color = Color(0xFFFFDF7A),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CyberProBadge()
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = planSubtitle,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right_small),
                    contentDescription = null,
                    tint = Color(0xFFFFDF7A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8 大分类：双列卡片网格 (无下方小字，极简图标+主标题)
        val settingEntries = listOf(
            SettingEntry("常规", R.drawable.ic_preferences_icon, NeonCyan, Screen.General.route),
            SettingEntry("账户", R.drawable.ic_myaccount_icon, NeonGreen, Screen.Account.route),
            SettingEntry("连接", R.drawable.ic_connection_icon, Color(0xFF38BDF8), Screen.Connection.route),
            SettingEntry("R.O.B.E.R.T.", R.drawable.ic_robert, NeonYellow, Screen.Robert.route),
            SettingEntry("拆分隧道", R.drawable.ic_split_routing, NeonPurple, Screen.SplitTunnel.route),
            SettingEntry("反审查", R.drawable.ic_anti_censorship_enabled, Color(0xFFFB7185), Screen.AntiCensorship.route),
            SettingEntry("界面外观", R.drawable.ic_feel, Color(0xFFFBBF24), Screen.LookAndFeel.route),
            SettingEntry("运行日志", R.drawable.ic_view_log, Color(0xFF2DD4BF), Screen.Debug.route),
        )
        settingEntries.chunked(2).forEachIndexed { rowIndex, rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowEntries.forEach { entry ->
                    SettingsGridCard(
                        modifier = Modifier.weight(1f),
                        title = entry.title,
                        iconRes = entry.icon,
                        accentColor = entry.accent,
                        onClick = { navController.navigate(entry.route) }
                    )
                }
                if (rowEntries.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (rowIndex < 3) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Logout Button
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLogoutConfirm = true },
            cornerRadius = 16.dp,
            borderColor = Color(0x44F59E0B),
            backgroundColor = Color(0x33B45309)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sign_out),
                    contentDescription = "Logout",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "注销登录",
                    color = Color(0xFFFBBF24),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (showProgress) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFFBBF24),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Ample bottom padding to ensure complete clearance of FloatingGlassDock
        Spacer(modifier = Modifier.height(130.dp))
    }
}

private data class SettingEntry(
    val title: String,
    val icon: Int,
    val accent: Color,
    val route: String,
)

/**
 * 设置双列网格卡片：图标徽章 + 标题 (去下方辅助小字)。
 */
@Composable
private fun SettingsGridCard(
    modifier: Modifier = Modifier,
    title: String,
    iconRes: Int,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LocalGlassColors.current.surface)
            .border(1.dp, LocalGlassColors.current.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.13f))
                    .border(1.dp, accentColor.copy(alpha = 0.26f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp
        )
    }
}

private suspend fun getLocalIpAddress(): Pair<String, String> = withContext(Dispatchers.IO) {
    try {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()?.toList() ?: return@withContext Pair("未连接", "无局域网连接")
        val physicalFirst = interfaces.sortedByDescending { intf ->
            val n = intf.name.lowercase()
            when {
                n.contains("wlan") -> 4
                n.contains("eth") -> 3
                n.contains("rmnet") || n.contains("ccmni") -> 2
                else -> 0
            }
        }
        for (intf in physicalFirst) {
            if (intf.isLoopback || !intf.isUp) continue
            val nameLower = intf.name.lowercase()
            if (nameLower.startsWith("tun") || nameLower.startsWith("tap") || nameLower.startsWith("wgtun") ||
                nameLower.startsWith("ppp") || nameLower.startsWith("p2p") || nameLower.startsWith("dummy")) continue
            val isKnownPhysical = nameLower.contains("wlan") || nameLower.contains("eth") || nameLower.contains("rmnet") || nameLower.contains("ccmni")
            if (!isKnownPhysical && intf.isPointToPoint) continue
            val addrs = intf.inetAddresses ?: continue
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && !addr.isLinkLocalAddress && addr is java.net.Inet4Address) {
                    val hostAddress = addr.hostAddress ?: continue
                    val name = when {
                        nameLower.contains("wlan") -> "Wi-Fi (${intf.name})"
                        nameLower.contains("rmnet") || nameLower.contains("ccmni") -> "移动蜂窝 (${intf.name})"
                        nameLower.contains("eth") -> "以太网 (${intf.name})"
                        else -> intf.displayName ?: intf.name
                    }
                    return@withContext Pair(hostAddress, name)
                }
            }
        }
    } catch (_: Exception) {}
    Pair("未连接", "无局域网连接")
}

private fun formatTrafficBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.1f GB".format(gb)
        mb >= 1.0 -> "%.1f MB".format(mb)
        kb >= 1.0 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}
