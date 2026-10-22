package com.windscribe.mobile.ui.preferences.general

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
import com.windscribe.mobile.ui.AppStartActivity
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberDropdown
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberSegmented
import com.windscribe.mobile.ui.common.CyberSwitch
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.NeonYellow
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.mobile.ui.nav.LocalNavController

class GeneralActions(
    val onOrderBySelected: (DropDownStringItem) -> Unit = {},
    val onLanguageSelected: (DropDownStringItem) -> Unit = {},
    val onLocationLoadClick: () -> Unit = {},
    val onNotificationStatClick: () -> Unit = {},
    val onHapticToggleClick: () -> Unit = {},
)

@Composable
fun GeneralScreen(viewModel: GeneralViewModel = hiltViewModel<GeneralViewModelImpl>()) {
    val activity = LocalActivity.current as? AppStartActivity
    val reloadAppState = viewModel.reloadApp.collectAsState(initial = false)
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val isNotificationStatEnabled by viewModel.isNotificationStatEnabled.collectAsState()
    val isLocationLoadEnabled by viewModel.isLocationLoadEnabled.collectAsState()

    LaunchedEffect(reloadAppState.value) {
        if (reloadAppState.value) {
            activity?.recreate()
        }
    }

    GeneralContent(
        orderByItems = viewModel.orderByItems,
        selectedOrderBy = viewModel.selectedOrderBy,
        languageItems = viewModel.languageItems,
        selectedLanguage = viewModel.selectedLanguage,
        isLocationLoadEnabled = isLocationLoadEnabled,
        isNotificationStatEnabled = isNotificationStatEnabled,
        isHapticEnabled = isHapticEnabled,
        versionName = viewModel.versionName,
        actions = GeneralActions(
            onOrderBySelected = viewModel::onOrderByItemSelected,
            onLanguageSelected = viewModel::onLanguageItemSelected,
            onLocationLoadClick = viewModel::onLocationLoadItemClicked,
            onNotificationStatClick = viewModel::onNotificationStatEnabledClick,
            onHapticToggleClick = viewModel::onHapticToggleButtonClicked,
        ),
    )
}

@Composable
fun GeneralContent(
    orderByItems: List<DropDownStringItem>,
    selectedOrderBy: String,
    languageItems: List<DropDownStringItem>,
    selectedLanguage: String,
    isLocationLoadEnabled: Boolean,
    isNotificationStatEnabled: Boolean,
    isHapticEnabled: Boolean,
    versionName: String,
    actions: GeneralActions,
) {
    val navController = LocalNavController.current
    val scrollState = rememberScrollState()

    val currentOrderItem = orderByItems.firstOrNull { it.key == selectedOrderBy }
        ?: orderByItems.firstOrNull() ?: DropDownStringItem(selectedOrderBy)

    val currentLangItem = languageItems.firstOrNull { it.key == selectedLanguage }
        ?: languageItems.firstOrNull() ?: DropDownStringItem(selectedLanguage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "常规",
                subtitle = "SORTING, TELEMETRY & FEEDBACK",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Section 1: Order By
                CyberSectionHeader(title = "节点位置排序")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x1FFFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sort_location),
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "位置顺序",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "按地理位置、字母或实测延迟排序节点列表",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (orderByItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            CyberSegmented(
                                options = orderByItems.map { it.label ?: it.key },
                                selectedIndex = orderByItems
                                    .indexOfFirst { it.key == selectedOrderBy }
                                    .coerceAtLeast(0),
                                onSelect = { index -> actions.onOrderBySelected(orderByItems[index]) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "按实测延迟升序排列，无数据的节点排在末尾。",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Telemetry & Feedback Toggles
                CyberSectionHeader(title = "体验与反馈特性")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Location Health Load
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "显示节点健康与负载状态",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "在节点列表中展示服务器实时承载负荷与延迟弧线",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isLocationLoadEnabled,
                                onCheckedChange = { actions.onLocationLoadClick() }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

                        // Notification Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "通知栏实时流量统计",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "在系统通知栏持续更新显示实时上下行传输数据",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberSwitch(
                                checked = isNotificationStatEnabled,
                                onCheckedChange = { actions.onNotificationStatClick() }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

                        // Haptic Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "触觉震动物理反馈 (Haptics)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "开关切换、节点点击与握手成功时的微震动反馈",
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

                // Section 4: Version Card
                CyberSectionHeader(title = "构建与核心版本")
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
                        Column {
                            Text(
                                text = "Windscribe Native Client",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target: Android 16 (API 36+) · aarch64",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        CyberBadge(
                            text = versionName,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
