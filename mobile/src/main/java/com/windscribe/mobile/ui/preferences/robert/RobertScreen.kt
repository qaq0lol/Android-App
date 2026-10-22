package com.windscribe.mobile.ui.preferences.robert

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberSectionHeader
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
import com.windscribe.mobile.ui.common.openUrl
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.vpn.api.response.RobertFilter

class RobertActions(
    val onFilterSettingChanged: (RobertFilter, Int) -> Unit = { _, _ -> },
    val onManageRulesClick: () -> Unit = {},
)

@Composable
fun RobertScreen(viewModel: RobertViewModel = hiltViewModel<RobertViewModelImpl>()) {
    val context = LocalContext.current
    val state by viewModel.robertFilterState.collectAsState()
    val showProgress by viewModel.showProgress.collectAsState()
    val goToState by viewModel.goToState.collectAsState(initial = RobertGoToState.None)

    LaunchedEffect(goToState) {
        when (val current = goToState) {
            is RobertGoToState.ManageRules -> {
                context.openUrl(current.url)
                viewModel.clearGoToState()
            }
            is RobertGoToState.Toast -> {
                val toast = current.message
                if (toast is ToastMessage.Raw) {
                    Toast.makeText(context, toast.message, Toast.LENGTH_SHORT).show()
                } else if (toast is ToastMessage.Localized) {
                    Toast.makeText(context, toast.message, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearGoToState()
            }
            RobertGoToState.None -> {}
        }
    }

    RobertContent(
        state = state,
        showProgress = showProgress,
        actions = RobertActions(
            onFilterSettingChanged = viewModel::onFilterSettingChanged,
            onManageRulesClick = viewModel::onManageRulesClick,
        )
    )
}

@Composable
fun RobertContent(
    state: RobertFilterState,
    showProgress: Boolean,
    actions: RobertActions,
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
                title = "R.O.B.E.R.T.",
                subtitle = "SERVER-SIDE THREAT & AD BLOCKING",
                onBack = { navController.popBackStack() },
                trailingContent = {
                    if (showProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 概览：真实规则启用比例 + 说明
                when (state) {
                    is RobertFilterState.Success -> RobertSummaryCard(filters = state.filters)
                    else -> GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        borderColor = Color(0x26B299FF)
                    ) {
                        Text(
                            text = "正在读取云端拦截规则…",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 说明与网页控制台入口
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    borderColor = Color(0x1AFFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "拦截完全在服务端 DNS 层执行，不消耗设备电量与性能；切换节点后规则依然生效。",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x4D0D1218))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { actions.onManageRulesClick() }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "在网页控制台管理自定义白名单 / 黑名单",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.link_arrow_icon),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CyberSectionHeader(title = "云端安全规则拦截矩阵")

                when (state) {
                    is RobertFilterState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonCyan)
                        }
                    }
                    is RobertFilterState.Failure -> {
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            borderColor = Color(0x33FF5252)
                        ) {
                            Text(
                                text = state.error,
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp
                            )
                        }
                    }
                    is RobertFilterState.Success -> {
                        CyberRobertFilterList(
                            filters = state.filters,
                            onToggle = { filter, newStatus ->
                                actions.onFilterSettingChanged(filter, newStatus)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun robertTitleZh(id: String, title: String): String {
    val key = (id + " " + title).lowercase()
    return when {
        key.contains("malware") -> "恶意软件"
        key.contains("ad") || key.contains("tracker") -> "广告与追踪器"
        key.contains("social") -> "社交网络"
        key.contains("porn") -> "成人内容"
        key.contains("gambl") -> "赌博"
        key.contains("clickbait") || key.contains("fake") -> "标题党与假新闻"
        key.contains("crypto") || key.contains("mining") -> "加密货币挖矿"
        key.contains("phish") -> "钓鱼网站"
        key.contains("scam") || key.contains("fraud") -> "诈骗与欺诈"
        key.contains("vpn") || key.contains("proxy") -> "VPN 与代理"
        else -> title
    }
}

private fun robertDescZh(id: String, title: String): String {
    val key = (id + " " + title).lowercase()
    return when {
        key.contains("malware") ->
            "拦截已知的恶意软件分发站点、僵尸网络与命令控制服务器。除排障外不建议关闭。"
        key.contains("ad") || key.contains("tracker") ->
            "拦截常见的广告与跨站追踪器域名，减少页面加载与行为画像。"
        key.contains("social") ->
            "屏蔽社交平台的跨站追踪脚本，防止在其他网站上被画像。"
        key.contains("porn") ->
            "拦截成人内容站点，适用于需要环境干净的场景。"
        key.contains("gambl") ->
            "拦截赌博与博彩类站点。"
        key.contains("clickbait") || key.contains("fake") ->
            "拦截标题党与已知的假新闻聚合站点。"
        key.contains("crypto") || key.contains("mining") ->
            "拦截网页内嵌的挖矿脚本，避免占用设备算力。"
        key.contains("phish") ->
            "拦截伪装成银行、支付或登录页面的钓鱼域名。"
        key.contains("scam") || key.contains("fraud") ->
            "拦截已知的诈骗与欺诈站点。"
        key.contains("vpn") || key.contains("proxy") ->
            "拦截已知的恶意代理与挖矿劫持类代理。"
        else -> ""
    }
}

@Composable
private fun CyberRobertFilterList(
    filters: List<RobertFilter>,
    onToggle: (RobertFilter, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        filters.forEach { filter ->
            val isBlocked = filter.status == 1
            val emoji = getRobertEmoji(filter.id, filter.title)

            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                borderColor = if (isBlocked) Color(0x264FF0C0) else Color(0x1FFFFFFF)
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
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = robertTitleZh(filter.id, filter.title),
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                CyberBadge(
                                    text = if (isBlocked) "已阻塞" else "放行",
                                    color = if (isBlocked) NeonGreen else TextMuted
                                )
                            }
                            val zhDesc = robertDescZh(filter.id, filter.title)
                            if (zhDesc.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = zhDesc,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    CyberSwitch(
                        checked = isBlocked,
                        onCheckedChange = { checked ->
                            onToggle(filter, if (checked) 1 else 0)
                        }
                    )
                }
            }
        }
    }
}

private fun getRobertEmoji(id: String, title: String): String {
    val key = (id + title).lowercase()
    return when {
        key.contains("malware") -> "🪲"
        key.contains("ad") || key.contains("tracker") -> "👁️"
        key.contains("social") -> "🔗"
        key.contains("porn") -> "⬛"
        key.contains("gambling") -> "🪙"
        key.contains("clickbait") || key.contains("fake") -> "🎣"
        key.contains("vpn") -> "🛡️"
        key.contains("crypto") -> "⚡"
        else -> "🔒"
    }
}

/**
 * R.O.B.E.R.T. 概览卡：用真实规则数据计算启用比例，不写死任何统计数字。
 */
@Composable
private fun RobertSummaryCard(filters: List<RobertFilter>) {
    val total = filters.size
    val enabled = filters.count { it.status == 1 }
    val ratio = if (total == 0) 0f else enabled.toFloat() / total.toFloat()

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = Color(0x26B299FF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "已启用规则",
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$enabled",
                        color = NeonGreen,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " / $total",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "服务端 DNS 级拦截规则",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(76.dp)) {
                    val strokeWidth = 6.dp.toPx()
                    drawArc(
                        color = Color(0x1FFFFFFF),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    drawArc(
                        color = NeonGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * ratio,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Text(
                    text = "${(ratio * 100).toInt()}%",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
