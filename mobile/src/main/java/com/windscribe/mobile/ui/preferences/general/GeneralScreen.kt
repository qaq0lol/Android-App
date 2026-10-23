package com.windscribe.mobile.ui.preferences.general

import android.content.Context
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.windscribe.mobile.ui.AppStartActivity
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberSegmented
import com.windscribe.mobile.ui.common.CyberSwitch
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.LocalGlassColors
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.mobile.ui.nav.LocalNavController

class GeneralActions(
    val onOrderBySelected: (DropDownStringItem) -> Unit = {},
    val onLocationLoadClick: () -> Unit = {},
    val onNotificationStatClick: () -> Unit = {},
    val onHapticToggleClick: () -> Unit = {},
    val onAutoReconnectClick: () -> Unit = {},
    val onKeepAliveClick: (Context) -> Unit = {},
)

@Composable
fun GeneralScreen(viewModel: GeneralViewModel = hiltViewModel<GeneralViewModelImpl>()) {
    val activity = LocalActivity.current as? AppStartActivity
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val reloadAppState = viewModel.reloadApp.collectAsState(initial = false)
    val selectedOrderBy by viewModel.selectedOrderBy.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val isNotificationStatEnabled by viewModel.isNotificationStatEnabled.collectAsState()
    val isLocationLoadEnabled by viewModel.isLocationLoadEnabled.collectAsState()
    val isAutoReconnectEnabled by viewModel.isAutoReconnectEnabled.collectAsState()
    val isKeepAliveEnabled by viewModel.isKeepAliveEnabled.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshKeepAliveStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(reloadAppState.value) {
        if (reloadAppState.value) {
            activity?.recreate()
        }
    }

    GeneralContent(
        orderByItems = viewModel.orderByItems,
        selectedOrderBy = selectedOrderBy,
        isLocationLoadEnabled = isLocationLoadEnabled,
        isNotificationStatEnabled = isNotificationStatEnabled,
        isHapticEnabled = isHapticEnabled,
        isAutoReconnectEnabled = isAutoReconnectEnabled,
        isKeepAliveEnabled = isKeepAliveEnabled,
        versionName = viewModel.versionName,
        buildChannel = viewModel.buildChannel,
        compileTarget = viewModel.compileTarget,
        coreArch = viewModel.coreArch,
        actions = GeneralActions(
            onOrderBySelected = viewModel::onOrderByItemSelected,
            onLocationLoadClick = viewModel::onLocationLoadItemClicked,
            onNotificationStatClick = viewModel::onNotificationStatEnabledClick,
            onHapticToggleClick = viewModel::onHapticToggleButtonClicked,
            onAutoReconnectClick = viewModel::onAutoReconnectClicked,
            onKeepAliveClick = viewModel::onKeepAliveClicked,
        ),
    )
}

@Composable
fun GeneralContent(
    orderByItems: List<DropDownStringItem>,
    selectedOrderBy: String,
    isLocationLoadEnabled: Boolean,
    isNotificationStatEnabled: Boolean,
    isHapticEnabled: Boolean,
    isAutoReconnectEnabled: Boolean,
    isKeepAliveEnabled: Boolean,
    versionName: String,
    buildChannel: String,
    compileTarget: String,
    coreArch: String,
    actions: GeneralActions,
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sortOptions = listOf("地理", "延迟", "字母")
    val selectedIndex = when (selectedOrderBy) {
        "Geography" -> 0
        "Latency" -> 1
        "Alphabet" -> 2
        else -> 1
    }

    val description = when (selectedOrderBy) {
        "Geography" -> "按与当前物理位置的测距升序排列。"
        "Alphabet" -> "按国家与城市英文字母 A-Z 顺序排列。"
        else -> "按实测延迟升序排列，无数据的节点排在末尾。"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "常规",
                subtitle = "SORTING, TELEMETRY & SYSTEM DIAGNOSTICS",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // ================= Section 1: 节点位置排序 (1:1 原型 Screen 12) =================
                CyberSectionHeader(title = "节点位置排序")
                
                CyberSegmented(
                    options = sortOptions,
                    selectedIndex = selectedIndex,
                    onSelect = { index ->
                        val targetKey = when (index) {
                            0 -> "Geography"
                            1 -> "Latency"
                            2 -> "Alphabet"
                            else -> "Latency"
                        }
                        orderByItems.firstOrNull { it.key == targetKey }?.let {
                            actions.onOrderBySelected(it)
                        } ?: actions.onOrderBySelected(DropDownStringItem(targetKey))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = description,
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ================= Card 1: 体验与通知 (1:1 原型 Screen 12) =================
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. 显示负载与延迟
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "显示负载与延迟",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "在节点列表中显示实时弧线与健康度",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isLocationLoadEnabled,
                                onCheckedChange = { actions.onLocationLoadClick() }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LocalGlassColors.current.borderSubtle))

                        // 2. 通知栏实时流量
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "通知栏实时流量",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "驻留通知持续显示上下行速率",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isNotificationStatEnabled,
                                onCheckedChange = { actions.onNotificationStatClick() }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LocalGlassColors.current.borderSubtle))

                        // 3. 触觉反馈
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "触觉反馈",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "切换与握手成功时的微震动",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isHapticEnabled,
                                onCheckedChange = { actions.onHapticToggleClick() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ================= Card 2: 连接与保活 (1:1 原型 Screen 12) =================
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. 自动重连
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "自动重连",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "网络切换后自动恢复隧道",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isAutoReconnectEnabled,
                                onCheckedChange = { actions.onAutoReconnectClick() }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LocalGlassColors.current.borderSubtle))

                        // 2. 后台保活
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "后台保活",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "锁定进程，避免被系统回收",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isKeepAliveEnabled,
                                onCheckedChange = { actions.onKeepAliveClick(context) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ================= Card 3: 诊断与系统构建信息表 (1:1 原型 Screen 12) =================
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoRow(label = "应用版本", value = versionName)
                        InfoRow(label = "构建渠道", value = buildChannel)
                        InfoRow(label = "编译目标", value = compileTarget)
                        InfoRow(label = "核心架构", value = coreArch)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.5.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
