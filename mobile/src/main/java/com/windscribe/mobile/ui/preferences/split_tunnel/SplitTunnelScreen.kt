package com.windscribe.mobile.ui.preferences.split_tunnel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberDropdown
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberSegmented
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
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.vpn.api.response.InstalledAppsData
import com.windscribe.vpn.cache.AppIconCache

data class SplitTunnelState(
    val showProgress: Boolean = false,
    val modes: List<DropDownStringItem> = emptyList(),
    val selectedModeKey: String = "",
    val isSplitTunnelEnabled: Boolean = false,
    val showSystemApps: Boolean = false,
    val searchKeyword: String = "",
    val filteredApps: List<InstalledAppsData> = emptyList(),
    val appIconCache: AppIconCache? = null,
    val excludedIpsCount: Int = 0,
)

class SplitTunnelActions(
    val onModeSelected: (DropDownStringItem) -> Unit = {},
    val onAppSelected: (InstalledAppsData) -> Unit = {},
    val onSplitTunnelSettingChanged: () -> Unit = {},
    val onQueryTextChange: (String) -> Unit = {},
    val onShowSystemAppsToggle: () -> Unit = {},
    val onManageDomainsIps: () -> Unit = {},
)

@Composable
fun SplitTunnelScreen(viewModel: SplitTunnelViewModel = hiltViewModel<SplitTunnelViewModelImpl>()) {
    val navController = LocalNavController.current
    val showProgress by viewModel.showProgress.collectAsState()
    val selectedModeKey by viewModel.selectedModeKey.collectAsState()
    val isSplitTunnelEnabled by viewModel.isSplitTunnelEnabled.collectAsState()
    val showSystemApps by viewModel.showSystemApps.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val excludedIpsCount by viewModel.excludedIpsCount.collectAsState()

    SplitTunnelContent(
        state = SplitTunnelState(
            showProgress = showProgress,
            modes = viewModel.modes,
            selectedModeKey = selectedModeKey,
            isSplitTunnelEnabled = isSplitTunnelEnabled,
            showSystemApps = showSystemApps,
            searchKeyword = searchKeyword,
            filteredApps = filteredApps,
            appIconCache = viewModel.appIconCache,
            excludedIpsCount = excludedIpsCount,
        ),
        actions = SplitTunnelActions(
            onModeSelected = viewModel::onModeSelected,
            onAppSelected = viewModel::onAppSelected,
            onSplitTunnelSettingChanged = viewModel::onSplitTunnelSettingChanged,
            onQueryTextChange = viewModel::onQueryTextChange,
            onShowSystemAppsToggle = viewModel::onShowSystemAppsToggle,
            onManageDomainsIps = {
                navController.navigate(Screen.ExcludedIpDomains.route)
            },
        ),
    )
}

@Composable
fun SplitTunnelContent(
    state: SplitTunnelState,
    actions: SplitTunnelActions,
) {
    val navController = LocalNavController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "拆分隧道",
                subtitle = "APP & IP TRAFFIC EXCLUSION/INCLUSION",
                onBack = { navController.popBackStack() },
                trailingContent = {
                    if (state.showProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Master Switch Card
                item {
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        borderColor = if (state.isSplitTunnelEnabled) Color(0x264FF0C0) else Color(0x14FFFFFF)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x1FFFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_split_routing),
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "拆分隧道总开关",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (state.isSplitTunnelEnabled) "已启用分流" else "已禁用（全局 VPN）",
                                            color = if (state.isSplitTunnelEnabled) NeonGreen else TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                CyberSwitch(
                                    checked = state.isSplitTunnelEnabled,
                                    onCheckedChange = { actions.onSplitTunnelSettingChanged() }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "选择连接时要从 Windscribe 中排除或包含的应用程序。支持已安装应用与特定 IP/域名分流。",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Mode Selector
                item {
                    val currentModeItem = state.modes.firstOrNull { it.key == state.selectedModeKey }
                        ?: state.modes.firstOrNull() ?: DropDownStringItem(state.selectedModeKey)
                    val isExclusive = state.selectedModeKey == "Exclusive"

                    GlassSurface(
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
                                    text = "分流运行模式",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isExclusive) "独家模式：勾选的应用将绕过 VPN 直连" else "包容模式：仅勾选的应用才走 VPN 隧道",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            if (state.modes.isNotEmpty()) {
                                CyberSegmented(
                                    modifier = Modifier.width(148.dp),
                                    options = state.modes.map { it.label ?: it.key },
                                    selectedIndex = state.modes
                                        .indexOfFirst { it.key == state.selectedModeKey }
                                        .coerceAtLeast(0),
                                    onSelect = { index -> actions.onModeSelected(state.modes[index]) }
                                )
                            }
                        }
                    }
                }

                // IP & Hostname Exclusions Shortcut
                item {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actions.onManageDomainsIps() },
                        cornerRadius = 20.dp,
                        borderColor = Color(0x14FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x26B299FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_connection_icon),
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "IP 与主机名排除列表",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "自定义不需要走 VPN 的域名或 IP 范围",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CyberBadge(
                                    text = "${state.excludedIpsCount} 项规则",
                                    color = NeonPurple
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_forward_arrow_white),
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Installed Applications Filter & List
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CyberSectionHeader(title = "本地应用程序分流列表")

                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            borderColor = Color(0x14FFFFFF)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Show System Apps Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "显示系统原生应用",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    CyberSwitch(
                                        checked = state.showSystemApps,
                                        onCheckedChange = { actions.onShowSystemAppsToggle() }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Search Apps Field
                                CyberTextField(
                                    value = state.searchKeyword,
                                    onValueChange = { actions.onQueryTextChange(it) },
                                    placeholder = "搜索应用名称或包名...",
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_preferences_icon),
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Apps List
                items(state.filteredApps, key = { it.packageName }) { app ->
                    CyberAppRow(
                        app = app,
                        appIconCache = state.appIconCache,
                        onAppSelected = { actions.onAppSelected(app) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CyberAppRow(
    app: InstalledAppsData,
    appIconCache: AppIconCache?,
    onAppSelected: () -> Unit
) {
    val isChecked = app.isChecked

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAppSelected),
        cornerRadius = 16.dp,
        borderColor = if (isChecked) Color(0x1FFFFFFF) else Color(0x14FFFFFF),
        backgroundColor = if (isChecked) Color(0xE60E182A) else Color(0x99101624)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconBitmap = appIconCache?.getIcon(app.packageName)
                if (iconBitmap != null) {
                    Image(
                        painter = BitmapPainter(iconBitmap.asImageBitmap()),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x331E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_split_routing),
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = app.appName,
                        color = if (isChecked) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            CyberSwitch(
                checked = isChecked,
                onCheckedChange = { onAppSelected() }
            )
        }
    }
}
