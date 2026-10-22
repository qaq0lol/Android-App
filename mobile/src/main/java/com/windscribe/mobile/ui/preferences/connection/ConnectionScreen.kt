package com.windscribe.mobile.ui.preferences.connection

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.AppStartActivity
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberDropdown
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberSlider
import com.windscribe.mobile.ui.common.CyberSwitch
import com.windscribe.mobile.ui.common.CyberTextField
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.NeonYellow
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.CONNECTION_MODE_AUTO
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.CONNECTION_MODE_MANUAL
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.DNS_MODE_CUSTOM
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.DNS_MODE_ROBERT
import com.windscribe.vpn.mocklocation.MockLocationManager.Companion.isAppSelectedInMockLocationList
import com.windscribe.vpn.mocklocation.MockLocationManager.Companion.isDevModeOn
import kotlinx.coroutines.flow.Flow

/**
 * Callbacks the connection-preferences UI can raise.
 */
class ConnectionActions(
    val onAllowLanToggleClicked: () -> Unit = {},
    val onGPSSpoofingToggleClicked: () -> Unit = {},
    val onDecoyTrafficToggleClicked: () -> Unit = {},
    val onFakeTrafficVolumeSelected: (DropDownStringItem) -> Unit = {},
    val onIpStackEgressModeSelected: (DropDownStringItem) -> Unit = {},
    val onModeSelected: (String) -> Unit = {},
    val onProtocolSelected: (DropDownStringItem) -> Unit = {},
    val onPortSelected: (DropDownStringItem) -> Unit = {},
    val onDNSModeSelected: (String) -> Unit = {},
    val onCustomDNSAddressChanged: (String) -> Unit = {},
    val onSaveCustomDNSAddress: () -> Unit = {},
    val onPacketSizeModeSelected: (Boolean) -> Unit = {},
    val onPacketSizedChanged: (Int) -> Unit = {},
    val onPacketSizeSaved: () -> Unit = {},
    val onAutoDetectClicked: () -> Unit = {},
)

/**
 * Immutable snapshot of Connection state.
 */
data class ConnectionUiState(
    val allowLan: Boolean = false,
    val gpsSpoofing: Boolean = false,
    val decoyTraffic: Boolean = false,
    val trafficMultiplier: String = "",
    val trafficMultipliers: List<DropDownStringItem> = emptyList(),
    val potentialDataUse: String = "",
    val ipStackEgressMode: String = "auto",
    val ipStackEgressModes: List<DropDownStringItem> = emptyList(),
    val mode: String = CONNECTION_MODE_AUTO,
    val protocols: List<DropDownStringItem> = listOf(DropDownStringItem("WireGuard")),
    val ports: List<DropDownStringItem> = listOf(DropDownStringItem("1194")),
    val selectedProtocol: String = "WireGuard",
    val selectedPort: String = "1194",
    val dnsMode: String = DNS_MODE_ROBERT,
    val customDnsAddress: String = "",
    val packetSizeAuto: Boolean = true,
    val packetSize: Int = 1420,
    val autoDetecting: Boolean = false,
)

@Composable
fun ConnectionScreen(viewModel: ConnectionViewModel = hiltViewModel<ConnectionViewModelImpl>()) {
    val allowLan by viewModel.allowLan.collectAsState()
    val gpsSpoofing by viewModel.gpsSpoofing.collectAsState()
    val decoyTraffic by viewModel.decoyTraffic.collectAsState()
    val trafficMultiplier by viewModel.trafficMultiplier.collectAsState()
    val trafficMultipliers by viewModel.trafficMultipliers.collectAsState()
    val potentialDataUse by viewModel.potentialDataUse.collectAsState()
    val ipStackEgressMode by viewModel.ipStackEgressMode.collectAsState()
    val ipStackEgressModes by viewModel.ipStackEgressModes.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val protocols by viewModel.protocols.collectAsState()
    val ports by viewModel.ports.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val selectedPort by viewModel.selectedPort.collectAsState()
    val dnsMode by viewModel.dnsMode.collectAsState()
    val customDnsAddress by viewModel.customDnsAddress.collectAsState()
    val packetSizeAuto by viewModel.packetSizeAuto.collectAsState()
    val packetSize by viewModel.packetSize.collectAsState()
    val autoDetecting by viewModel.autoDetecting.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshPreferences()
    }

    val state = ConnectionUiState(
        allowLan = allowLan,
        gpsSpoofing = gpsSpoofing,
        decoyTraffic = decoyTraffic,
        trafficMultiplier = trafficMultiplier,
        trafficMultipliers = trafficMultipliers,
        potentialDataUse = potentialDataUse,
        ipStackEgressMode = ipStackEgressMode,
        ipStackEgressModes = ipStackEgressModes,
        mode = mode,
        protocols = protocols,
        ports = ports,
        selectedProtocol = selectedProtocol,
        selectedPort = selectedPort,
        dnsMode = dnsMode,
        customDnsAddress = customDnsAddress,
        packetSizeAuto = packetSizeAuto,
        packetSize = packetSize,
        autoDetecting = autoDetecting,
    )

    ConnectionContent(
        state = state,
        toastMessage = viewModel.toastMessage,
        actions = ConnectionActions(
            onAllowLanToggleClicked = viewModel::onAllowLanToggleClicked,
            onGPSSpoofingToggleClicked = viewModel::onGPSSpoofingToggleClicked,
            onDecoyTrafficToggleClicked = viewModel::onDecoyTrafficToggleClicked,
            onFakeTrafficVolumeSelected = viewModel::onFakeTrafficVolumeSelected,
            onIpStackEgressModeSelected = viewModel::onIpStackEgressModeSelected,
            onModeSelected = viewModel::onModeSelected,
            onProtocolSelected = viewModel::onProtocolSelected,
            onPortSelected = viewModel::onPortSelected,
            onDNSModeSelected = viewModel::onDNSModeSelected,
            onCustomDNSAddressChanged = viewModel::onCustomDNSAddressChanged,
            onSaveCustomDNSAddress = viewModel::saveCustomDNSAddress,
            onPacketSizeModeSelected = viewModel::onPacketSizeModeSelected,
            onPacketSizedChanged = viewModel::onPacketSizedChanged,
            onPacketSizeSaved = viewModel::onPacketSizeSaved,
            onAutoDetectClicked = viewModel::onAutoDetectClicked,
        ),
    )
}

@Composable
fun ConnectionContent(
    state: ConnectionUiState,
    toastMessage: Flow<ToastMessage>,
    actions: ConnectionActions,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(toastMessage) {
        toastMessage.collect {
            when (it) {
                is ToastMessage.Raw -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                is ToastMessage.Localized -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "连接",
                subtitle = "VPN PROTOCOL, PORTS & ROUTING CONFIG",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 连接模式
                CyberConnectionModeCard(state = state, actions = actions)

                Spacer(modifier = Modifier.height(10.dp))

                // 协议 / 端口 / MTU：一张卡三段
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CyberProtocolPortCard(state = state, actions = actions, embedded = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        CyberPacketSizeCard(state = state, actions = actions, embedded = true)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // DNS / IP 堆叠：一张卡两段
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CyberDnsCard(state = state, actions = actions, embedded = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        CyberIpStackCard(state = state, actions = actions, embedded = true)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 路由与高级开关
                CyberRoutingTogglesCard(state = state, actions = actions)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CyberConnectionModeCard(
    state: ConnectionUiState,
    actions: ConnectionActions
,
    embedded: Boolean = false
) {
    val isAuto = state.mode == CONNECTION_MODE_AUTO

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "连接模式",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isAuto) "智能根据网络环境自动选择最佳通讯协议" else "已启用手动指定协定与端口",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                CyberDropdown(
                    label = "Mode",
                    selectedItem = if (isAuto) "自动 (Auto)" else "手动 (Manual)",
                    items = listOf("自动 (Auto)", "手动 (Manual)"),
                    onItemSelected = { selected ->
                        if (selected.startsWith("自动")) {
                            actions.onModeSelected(CONNECTION_MODE_AUTO)
                        } else {
                            actions.onModeSelected(CONNECTION_MODE_MANUAL)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CyberProtocolPortCard(
    state: ConnectionUiState,
    actions: ConnectionActions
,
    embedded: Boolean = false
) {
    val isAuto = state.mode == CONNECTION_MODE_AUTO
    val currentProtoItem = state.protocols.firstOrNull { it.key == state.selectedProtocol }
        ?: DropDownStringItem(state.selectedProtocol)
    val currentPortItem = state.ports.firstOrNull { it.key == state.selectedPort }
        ?: DropDownStringItem(state.selectedPort)

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = if (isAuto) Color(0x14FFFFFF) else Color(0x264FF0C0)
    ) {
        if (isAuto) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1FFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_connection_icon),
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "智能自适应协议通道",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "根据网络延迟与连通性自动协商最快隧道（WireGuard 首选）",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberBadge(text = "自动协商协议与端口", color = NeonGreen)
                    CyberButton(
                        text = "切换为手动指定",
                        onClick = { actions.onModeSelected(CONNECTION_MODE_MANUAL) },
                        color = NeonCyan
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 通讯协议：横向可选项
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "通讯协议",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(9.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.protocols
                            .sortedBy { proto ->
                                val name = (proto.label ?: "") + proto.key
                                if (name.contains("wireguard", ignoreCase = true)) 0 else 1
                            }
                            .forEach { proto ->
                            val selected = proto.key == state.selectedProtocol
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) Color(0x264FF0C0) else Color(0x14FFFFFF)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Color(0x664FF0C0) else Color(0x1AFFFFFF),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { actions.onProtocolSelected(proto) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = proto.label ?: proto.key,
                                    color = if (selected) NeonGreen else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

                // Port Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "传输端口",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "指定底层隧道连接端口",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    CyberDropdown(
                        label = "Port",
                        selectedItem = currentPortItem,
                        items = state.ports,
                        onItemSelected = { actions.onPortSelected(it) },
                        itemToString = { it.label ?: it.key }
                    )
                }
            }
        }
    }
}

@Composable
private fun CyberPacketSizeCard(
    state: ConnectionUiState,
    actions: ConnectionActions
,
    embedded: Boolean = false
) {
    var sliderValue by remember(state.packetSize) { mutableFloatStateOf(state.packetSize.toFloat()) }

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "数据包 MTU: ${state.packetSize}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (state.packetSizeAuto) "自动侦测网络最优传输单元" else "手动指定固定 MTU 大小",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                CyberDropdown(
                    label = "MTU Mode",
                    selectedItem = if (state.packetSizeAuto) "自动 (Auto)" else "手动 (Manual)",
                    items = listOf("自动 (Auto)", "手动 (Manual)"),
                    onItemSelected = { selected ->
                        actions.onPacketSizeModeSelected(selected.startsWith("自动"))
                    }
                )
            }

            if (!state.packetSizeAuto) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1280", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        text = "${sliderValue.toInt()} 字节",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "1500", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                CyberSlider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        actions.onPacketSizedChanged(it.toInt())
                    },
                    valueRange = 1280f..1500f,
                    onValueChangeFinished = { actions.onPacketSizeSaved() }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    CyberButton(
                        text = if (state.autoDetecting) "检测中..." else "一键自动探测最佳 MTU",
                        onClick = actions.onAutoDetectClicked,
                        enabled = !state.autoDetecting
                    )
                }
            }
        }
    }
}

@Composable
private fun CyberIpStackCard(
    state: ConnectionUiState,
    actions: ConnectionActions
,
    embedded: Boolean = false
) {
    val currentEgressItem = state.ipStackEgressModes.firstOrNull { it.key == state.ipStackEgressMode }
        ?: DropDownStringItem(state.ipStackEgressMode)

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x14FFFFFF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "IP 堆叠协议出口",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "为 VPN 连接配置 IP 堆栈出口 (仅 WireGuard)",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            CyberDropdown(
                label = "IP Stack",
                selectedItem = currentEgressItem,
                items = state.ipStackEgressModes,
                onItemSelected = { actions.onIpStackEgressModeSelected(it) },
                itemToString = { it.label ?: it.key }
            )
        }
    }
}

@Composable
private fun CyberDnsCard(
    state: ConnectionUiState,
    actions: ConnectionActions
,
    embedded: Boolean = false
) {
    val isCustom = state.dnsMode == DNS_MODE_CUSTOM
    var dnsInput by remember(state.customDnsAddress) { mutableStateOf(state.customDnsAddress) }
    val context = androidx.compose.ui.platform.LocalContext.current

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "连线 DNS 服务器",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isCustom) "手动指定自定义 DNS / DoH" else "默认 R.O.B.E.R.T. 云端过滤 DNS",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                CyberDropdown(
                    label = "DNS",
                    selectedItem = if (isCustom) "自定义 (Custom)" else "默认 (R.O.B.E.R.T.)",
                    items = listOf("默认 (R.O.B.E.R.T.)", "自定义 (Custom)"),
                    onItemSelected = { selected ->
                        if (selected.startsWith("默认")) {
                            actions.onDNSModeSelected(DNS_MODE_ROBERT)
                        } else {
                            actions.onDNSModeSelected(DNS_MODE_CUSTOM)
                        }
                    }
                )
            }

            if (isCustom) {
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(
                    value = dnsInput,
                    onValueChange = {
                        dnsInput = it
                        actions.onCustomDNSAddressChanged(it)
                    },
                    placeholder = "https://dns.google/dns-query 或 IP: 1.1.1.1"
                )
                Spacer(modifier = Modifier.height(8.dp))
                val dnsDirty = dnsInput.trim() != state.customDnsAddress.trim()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dnsDirty) {
                        CyberButton(
                            text = "保存 DNS 配置",
                            onClick = {
                                actions.onSaveCustomDNSAddress()
                                android.widget.Toast.makeText(
                                    context,
                                    "DNS 配置已保存",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            color = NeonGreen
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "已保存",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✓",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberRoutingTogglesCard(
    state: ConnectionUiState,
    actions: ConnectionActions
,
    embedded: Boolean = false
) {
    val activity = LocalActivity.current as? AppStartActivity
    val context = LocalContext.current
    val navController = LocalNavController.current

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x14FFFFFF)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 允许局域网流量
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "允许局域网流量 (LAN Bypass)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "允许在连接 VPN 时访问本地网络设备（打印机、NAS 等）",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                CyberSwitch(
                    checked = state.allowLan,
                    onCheckedChange = { actions.onAllowLanToggleClicked() }
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

            // GPS 欺骗
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "全球定位系统欺骗 (GPS Spoofing)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "将手机物理 GPS 坐标与连接的 VPN 服务器地理位置匹配",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                CyberSwitch(
                    checked = state.gpsSpoofing,
                    onCheckedChange = {
                        if (!state.gpsSpoofing) {
                            if (!isDevModeOn(context) || !isAppSelectedInMockLocationList(context)) {
                                navController.navigate(Screen.GpsSpoofing.route)
                            } else {
                                actions.onGPSSpoofingToggleClicked()
                            }
                        } else {
                            actions.onGPSSpoofingToggleClicked()
                        }
                    }
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

            // 诱饵流量
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "诱饵交通 (Decoy Traffic)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "产生拟态网络握手与流量混淆，掩盖真实协议特征",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                CyberSwitch(
                    checked = state.decoyTraffic,
                    onCheckedChange = { actions.onDecoyTrafficToggleClicked() }
                )
            }

            if (state.decoyTraffic && state.trafficMultipliers.isNotEmpty()) {
                val currentMultiplier = state.trafficMultipliers.firstOrNull { it.key == state.trafficMultiplier }
                    ?: state.trafficMultipliers.first()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "流量倍增量 (${state.potentialDataUse})",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    CyberDropdown(
                        label = "Volume",
                        selectedItem = currentMultiplier,
                        items = state.trafficMultipliers,
                        onItemSelected = { actions.onFakeTrafficVolumeSelected(it) },
                        itemToString = { it.label ?: it.key }
                    )
                }
            }
        }
    }
}
