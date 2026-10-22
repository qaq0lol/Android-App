package com.windscribe.mobile.ui.preferences.anticensorship

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberDropdown
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberSwitch
import com.windscribe.mobile.ui.common.CyberTextField
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.vpn.apppreference.PreferencesKeyConstants

data class AntiCensorshipState(
    val protocolTweaksModes: List<DropDownStringItem> = emptyList(),
    val selectedProtocolTweaksMode: String = "auto",
    val serverRoutingModes: List<DropDownStringItem> = emptyList(),
    val selectedServerRouting: String = "Auto",
    val amneziaPresets: List<DropDownStringItem> = emptyList(),
    val selectedPreset: String = "",
    val extraTlsPaddingEnabled: Boolean = true,
    val tlsServerName: String = "",
    val decoyTraffic: Boolean = false,
    val trafficMultiplier: String = "",
    val trafficMultipliers: List<DropDownStringItem> = emptyList(),
)

class AntiCensorshipActions(
    val onProtocolTweaksModeSelected: (String) -> Unit = {},
    val onAmneziaPresetSelected: (String) -> Unit = {},
    val onServerRoutingSelected: (String) -> Unit = {},
    val onExtraTlsPaddingToggled: () -> Unit = {},
    val onTlsServerNameChanged: (String) -> Unit = {},
    val onDecoyTrafficToggled: () -> Unit = {},
    val onFakeTrafficVolumeSelected: (DropDownStringItem) -> Unit = {},
)

@Composable
fun AntiCensorshipScreen(viewModel: AntiCensorshipViewModel = hiltViewModel<AntiCensorshipViewModelImpl>()) {
    val protocolTweaksModes by viewModel.protocolTweaksModes.collectAsState()
    val selectedProtocolTweaksMode by viewModel.selectedProtocolTweaksMode.collectAsState()
    val serverRoutingModes by viewModel.serverRoutingModes.collectAsState()
    val selectedServerRouting by viewModel.selectedServerRouting.collectAsState()
    val amneziaPresets by viewModel.amneziaPresets.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val extraTlsPaddingEnabled by viewModel.extraTlsPaddingEnabled.collectAsState()
    val tlsServerName by viewModel.tlsServerName.collectAsState()
    val decoyTraffic by viewModel.decoyTraffic.collectAsState()
    val trafficMultiplier by viewModel.trafficMultiplier.collectAsState()
    val trafficMultipliers by viewModel.trafficMultipliers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshPreferences()
    }

    AntiCensorshipContent(
        state = AntiCensorshipState(
            protocolTweaksModes = protocolTweaksModes,
            selectedProtocolTweaksMode = selectedProtocolTweaksMode,
            serverRoutingModes = serverRoutingModes,
            selectedServerRouting = selectedServerRouting,
            amneziaPresets = amneziaPresets,
            selectedPreset = selectedPreset,
            extraTlsPaddingEnabled = extraTlsPaddingEnabled,
            tlsServerName = tlsServerName,
            decoyTraffic = decoyTraffic,
            trafficMultiplier = trafficMultiplier,
            trafficMultipliers = trafficMultipliers,
        ),
        actions = AntiCensorshipActions(
            onProtocolTweaksModeSelected = viewModel::onProtocolTweaksModeSelected,
            onAmneziaPresetSelected = viewModel::onAmneziaPresetSelected,
            onServerRoutingSelected = viewModel::onServerRoutingSelected,
            onExtraTlsPaddingToggled = viewModel::onExtraTlsPaddingToggled,
            onTlsServerNameChanged = viewModel::onTlsServerNameChanged,
            onDecoyTrafficToggled = viewModel::onDecoyTrafficToggled,
            onFakeTrafficVolumeSelected = viewModel::onFakeTrafficVolumeSelected,
        ),
    )
}

@Composable
fun AntiCensorshipContent(
    state: AntiCensorshipState,
    actions: AntiCensorshipActions,
) {
    val navController = LocalNavController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "反审查",
                subtitle = "CIRCUMVENT DEEP PACKET INSPECTION & FIREWALLS",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Warning / Notice Banner
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x44FF3366)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33FF3366)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_anti_censorship_enabled),
                                contentDescription = null,
                                tint = Color(0xFFFF5376),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "深度伪装与混淆特性",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "这些功能会微幅增加延迟与开销，仅在常规连线被严格阻断时推荐开启。",
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 混淆特性：一张卡四段
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CyberProtocolTweaksCard(state = state, actions = actions, embedded = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        CyberServerRoutingCard(state = state, actions = actions, embedded = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        CyberExtraTlsPaddingCard(state = state, actions = actions, embedded = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        CyberCustomSniCard(state = state, actions = actions, embedded = true)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 诱饵流量单独一张（含倍率滑块）
                CyberDecoyTrafficCard(state = state, actions = actions)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CyberProtocolTweaksCard(
    state: AntiCensorshipState,
    actions: AntiCensorshipActions,
    embedded: Boolean = false
) {
    val currentModeItem = state.protocolTweaksModes.firstOrNull { it.key == state.selectedProtocolTweaksMode }
        ?: state.protocolTweaksModes.firstOrNull()
        ?: DropDownStringItem(state.selectedProtocolTweaksMode)

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
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "底层协议特征修改",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "对 WireGuard、OpenVPN 和 Stealth 的底层报文头部进行变形打乱",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                CyberDropdown(
                    label = "Tweaks",
                    selectedItem = currentModeItem,
                    items = state.protocolTweaksModes,
                    onItemSelected = { actions.onProtocolTweaksModeSelected(it.key) },
                    itemToString = { it.label ?: it.key }
                )
            }

            // Amnezia Presets (if active)
            if (state.selectedProtocolTweaksMode != PreferencesKeyConstants.PROTOCOL_TWEAKS_DISABLED &&
                state.amneziaPresets.isNotEmpty()
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))
                Spacer(modifier = Modifier.height(12.dp))

                val currentPreset = state.amneziaPresets.firstOrNull { it.key == state.selectedPreset }
                    ?: state.amneziaPresets.first()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Amnezia 预设参数",
                            color = NeonPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "加载针对特定网络审查环境调优的混淆参数",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    CyberDropdown(
                        label = "Preset",
                        selectedItem = currentPreset,
                        items = state.amneziaPresets,
                        onItemSelected = { actions.onAmneziaPresetSelected(it.key) },
                        itemToString = { it.label ?: it.key }
                    )
                }
            }
        }
    }
}

@Composable
private fun CyberServerRoutingCard(
    state: AntiCensorshipState,
    actions: AntiCensorshipActions,
    embedded: Boolean = false
) {
    val currentRoutingItem = state.serverRoutingModes.firstOrNull { it.key == state.selectedServerRouting }
        ?: state.serverRoutingModes.firstOrNull()
        ?: DropDownStringItem(state.selectedServerRouting)

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
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "服务器路由通道",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "增加多路径中继握手，虽有轻微延迟但提高连线成功率",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            CyberDropdown(
                label = "Routing",
                selectedItem = currentRoutingItem,
                items = state.serverRoutingModes,
                onItemSelected = { actions.onServerRoutingSelected(it.key) },
                itemToString = { it.label ?: it.key }
            )
        }
    }
}

@Composable
private fun CyberExtraTlsPaddingCard(
    state: AntiCensorshipState,
    actions: AntiCensorshipActions,
    embedded: Boolean = false
) {
    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = if (state.extraTlsPaddingEnabled) Color(0x1FFFFFFF) else Color(0x14FFFFFF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "大型 TLS 封包保护 (Extra TLS Padding)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "人为填充增大 TLS ClientHello 封包，规避特定深度特征规则",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            CyberSwitch(
                checked = state.extraTlsPaddingEnabled,
                onCheckedChange = { actions.onExtraTlsPaddingToggled() }
            )
        }
    }
}

@Composable
private fun CyberCustomSniCard(
    state: AntiCensorshipState,
    actions: AntiCensorshipActions,
    embedded: Boolean = false
) {
    var sniInput by remember(state.tlsServerName) { mutableStateOf(state.tlsServerName) }

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "自订 SNI 网域 (Server Name Indication)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "伪装 TLS 握手的主机名（仅适用于 Stealth 和 Wstunnel 协定）",
                color = TextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CyberTextField(
                value = sniInput,
                onValueChange = {
                    sniInput = it
                    actions.onTlsServerNameChanged(it)
                },
                placeholder = "例如: www.microsoft.com 或 www.apple.com"
            )
        }
    }
}

@Composable
private fun CyberDecoyTrafficCard(
    state: AntiCensorshipState,
    actions: AntiCensorshipActions,
    embedded: Boolean = false
) {
    val currentVolumeItem = state.trafficMultipliers.firstOrNull { it.key == state.trafficMultiplier }
        ?: state.trafficMultipliers.firstOrNull()
        ?: DropDownStringItem(state.trafficMultiplier)

    GlassSurface(
        flush = embedded,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = if (state.decoyTraffic) Color(0x1FFFFFFF) else Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "拟态诱饵流量混淆 (Decoy Traffic)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "持续生成高随机性后台拟态伪装数据包，混淆 DPI 对真实通信流量的行为特征分析",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                CyberSwitch(
                    checked = state.decoyTraffic,
                    onCheckedChange = { actions.onDecoyTrafficToggled() }
                )
            }

            if (state.decoyTraffic) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "伪装流量规模 / 倍率",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "调整后台诱饵数据包发送频率与数据规模",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    CyberDropdown(
                        label = "Volume",
                        selectedItem = currentVolumeItem,
                        items = state.trafficMultipliers,
                        onItemSelected = { actions.onFakeTrafficVolumeSelected(it) },
                        itemToString = { it.label ?: it.key }
                    )
                }
            }
        }
    }
}

