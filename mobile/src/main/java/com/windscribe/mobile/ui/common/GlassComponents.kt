package com.windscribe.mobile.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.connection.ConnectionUIState

// Cyber Glass Color Palette
val CyberBackground = Color(0xFF06080D)
val GlassSurfaceColor = Color(0xB30D1218)
val GlassNestedColor = Color(0x8A0A0F14)
val GlassBorderColor = Color(0x1FFFFFFF)
val NeonCyan = Color(0xFF6FE3DC)
val NeonGreen = Color(0xFF4FF0C0)
val NeonPurple = Color(0xFFB299FF)
val NeonYellow = Color(0xFFFACC15)
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF8A98A6)
val TextMuted = Color(0xFF5E6C7A)

/**
 * Reusable Glassmorphism Card Container
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    borderColor: Color = Color(0x1FFFFFFF),
    backgroundColor: Color = GlassSurfaceColor,
    flush: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (flush) {
        Box(modifier = modifier.padding(vertical = 12.dp)) { content() }
        return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.05f),
                        borderColor.copy(alpha = 0.15f),
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

// —— 连接球：紫罗兰涟漪 + 青绿核心 ——
val RippleViolet = Color(0xFFB299FF)
val OrbCyanHigh = Color(0xFFC9FFF6)
val OrbCyanMid = Color(0xFF27CFD6)
val OrbCyanDeep = Color(0xFF0A8CA6)
val OrbIdleHigh = Color(0xFF3C444D)
val OrbIdleDeep = Color(0xFF10161D)

/**
 * 三态连接球。
 *
 * - 未连接：灰色哑球，紫环静止且整体压暗，不产生任何涟漪动画。
 * - 连接中：青绿球体轻微呼吸，三圈紫罗兰涟漪快速外扩（约 2.1s 一轮）。
 * - 已连接：青绿球体常亮，涟漪放慢（约 4.6s 一轮），形成稳定呼吸感。
 *
 * 紫环为静态同心结构（带断口），涟漪是叠加在其上的动态层，二者互不干扰。
 */
@Composable
fun GlowingConnectionOrb(
    state: ConnectionUIState,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 232.dp,
) {
    val isConnected = state is ConnectionUIState.Connected
    val isConnecting = state is ConnectionUIState.Connecting
    val active = isConnected || isConnecting

    val transition = rememberInfiniteTransition(label = "OrbRipple")
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnecting) 2100 else 4600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ripple",
    )
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val orbDiameter = diameter * 0.42f

    Box(
        modifier = modifier
            .size(diameter)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onConnectClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 静态紫环 + 动态涟漪
        Canvas(modifier = Modifier.size(diameter)) {
            val c = center
            val maxR = size.minDimension / 2f
            val orbR = maxR * 0.42f

            // 右上角紫罗兰氛围光
            if (active) {
                val glowCenter = Offset(c.x + maxR * 0.26f, c.y - maxR * 0.24f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RippleViolet.copy(alpha = 0.30f), Color.Transparent),
                        center = glowCenter,
                        radius = maxR * 0.66f,
                    ),
                    radius = maxR * 0.66f,
                    center = glowCenter,
                )
            }

            // 7 圈同心紫环，逐层变淡并带断口
            val ratios = floatArrayOf(0.50f, 0.58f, 0.66f, 0.74f, 0.82f, 0.90f, 0.97f)
            val alphas = floatArrayOf(0.46f, 0.40f, 0.34f, 0.28f, 0.22f, 0.16f, 0.11f)
            ratios.forEachIndexed { i, ratio ->
                val r = maxR * ratio
                val pattern = floatArrayOf(
                    r * 1.55f, r * 0.085f,
                    r * 0.62f, r * 0.065f,
                )
                drawCircle(
                    color = RippleViolet.copy(alpha = if (active) alphas[i] else alphas[i] * 0.55f),
                    radius = r,
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(pattern, r * (0.23f * i)),
                    ),
                )
            }

            // 三圈错相位涟漪
            if (active) {
                repeat(3) { i ->
                    val p = (ripple + i / 3f) % 1f
                    drawCircle(
                        color = RippleViolet.copy(alpha = (1f - p) * 0.55f),
                        radius = orbR + (maxR - orbR) * p,
                        style = Stroke(width = 1.3f),
                    )
                }
            }
        }

        // 球体外层辉光
        Box(
            modifier = Modifier
                .size(diameter * 0.82f)
                .drawBehind {
                    if (active) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    OrbCyanMid.copy(alpha = if (isConnected) 0.34f else 0.22f),
                                    Color.Transparent,
                                ),
                                center = center,
                                radius = size.minDimension / 2f,
                            ),
                        )
                    }
                },
        )

        // 青绿核心球
        Box(
            modifier = Modifier
                .size(orbDiameter)
                .graphicsLayer {
                    if (isConnecting) {
                        scaleX = pulse
                        scaleY = pulse
                    }
                }
                .clip(CircleShape)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (active) {
                                listOf(OrbCyanHigh, OrbCyanMid, OrbCyanDeep)
                            } else {
                                listOf(OrbIdleHigh, Color(0xFF232B34), OrbIdleDeep)
                            },
                            center = Offset(size.width * 0.33f, size.height * 0.24f),
                            radius = size.width * 0.95f,
                        ),
                    )
                    if (active) {
                        // 左上高光
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                                center = Offset(size.width * 0.32f, size.height * 0.2f),
                                radius = size.width * 0.3f,
                            ),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    id = if (isConnected) R.drawable.ic_on_button else R.drawable.ic_connection_icon,
                ),
                contentDescription = null,
                modifier = Modifier.size((orbDiameter.value * 0.30f).dp),
                tint = if (active) Color(0xFF04323B) else TextMuted,
            )
        }
    }
}


private val countryZhMap = mapOf(
    "hong kong" to "香港", "singapore" to "新加坡", "japan" to "日本",
    "united states" to "美国", "usa" to "美国", "us" to "美国", "u.s." to "美国", "canada" to "加拿大",
    "united kingdom" to "英国", "uk" to "英国", "great britain" to "英国", "germany" to "德国", "france" to "法国",
    "netherlands" to "荷兰", "australia" to "澳大利亚", "india" to "印度",
    "south korea" to "韩国", "taiwan" to "台湾", "vietnam" to "越南",
    "thailand" to "泰国", "malaysia" to "马来西亚", "indonesia" to "印度尼西亚",
    "philippines" to "菲律宾", "russia" to "俄罗斯", "brazil" to "巴西",
    "turkey" to "土耳其", "switzerland" to "瑞士", "sweden" to "瑞典",
    "norway" to "挪威", "spain" to "西班牙", "italy" to "意大利",
    "poland" to "波兰", "romania" to "罗马尼亚", "ukraine" to "乌克兰",
    "mexico" to "墨西哥", "argentina" to "阿根廷", "south africa" to "南非",
    "egypt" to "埃及", "israel" to "以色列", "united arab emirates" to "阿联酋",
    "saudi arabia" to "沙特阿拉伯", "new zealand" to "新西兰", "ireland" to "爱尔兰",
    "finland" to "芬兰", "denmark" to "丹麦", "belgium" to "比利时",
    "austria" to "奥地利", "czech republic" to "捷克", "czechia" to "捷克",
    "hungary" to "匈牙利", "greece" to "希腊", "portugal" to "葡萄牙",
    "chile" to "智利", "colombia" to "哥伦比亚", "peru" to "秘鲁",
    "china" to "中国", "macau" to "澳门", "macao" to "澳门",
    "iceland" to "冰岛", "luxembourg" to "卢森堡", "bulgaria" to "保加利亚",
    "croatia" to "克罗地亚", "serbia" to "塞尔维亚", "slovakia" to "斯洛伐克",
    "slovenia" to "斯洛文尼亚", "estonia" to "爱沙尼亚", "latvia" to "拉脱维亚",
    "lithuania" to "立陶宛", "moldova" to "摩尔多瓦", "albania" to "阿尔巴尼亚",
    "armenia" to "亚美尼亚", "georgia" to "格鲁吉亚", "kazakhstan" to "哈萨克斯坦",
    "pakistan" to "巴基斯坦", "bangladesh" to "孟加拉国", "sri lanka" to "斯里兰卡",
    "nepal" to "尼泊尔", "mongolia" to "蒙古", "cambodia" to "柬埔寨",
    "laos" to "老挝", "myanmar" to "缅甸", "brunei" to "文莱",
    "qatar" to "卡塔尔", "kuwait" to "科威特", "bahrain" to "巴林",
    "oman" to "阿曼", "jordan" to "约旦", "lebanon" to "黎巴嫩",
    "iraq" to "伊拉克", "iran" to "伊朗", "nigeria" to "尼日利亚",
    "kenya" to "肯尼亚", "morocco" to "摩洛哥", "tunisia" to "突尼斯",
    "algeria" to "阿尔及利亚", "ghana" to "加纳", "tanzania" to "坦桑尼亚",
    "uganda" to "乌干达", "ethiopia" to "埃塞俄比亚", "panama" to "巴拿马",
    "costa rica" to "哥斯达黎加", "guatemala" to "危地马拉", "uruguay" to "乌拉圭",
    "paraguay" to "巴拉圭", "bolivia" to "玻利维亚", "ecuador" to "厄瓜多尔",
    "venezuela" to "委内瑞拉", "puerto rico" to "波多黎各", "bermuda" to "百慕大",
    "bahamas" to "巴哈马", "jamaica" to "牙买加", "cyprus" to "塞浦路斯",
    "malta" to "马耳他", "monaco" to "摩纳哥", "liechtenstein" to "列支敦士登",
    "andorra" to "安道尔", "north macedonia" to "北马其顿", "bosnia and herzegovina" to "波黑",
    "montenegro" to "黑山", "belarus" to "白俄罗斯", "uzbekistan" to "乌兹别克斯坦",
    "kyrgyzstan" to "吉尔吉斯斯坦", "tajikistan" to "塔吉克斯坦", "turkmenistan" to "土库曼斯坦",
    "azerbaijan" to "阿塞拜疆", "afghanistan" to "阿富汗", "syria to" to "叙利亚",
    "yemen" to "也门", "libya" to "利比亚", "sudan" to "苏丹",
    "senegal" to "塞内加尔", "cameroon" to "喀麦隆", "zimbabwe" to "津巴布韦",
    "zambia" to "赞比亚", "mozambique" to "莫桑比克", "angola" to "安哥拉",
    "madagascar" to "马达加斯加", "mauritius" to "毛里求斯", "seychelles" to "塞舌尔",
    "maldives" to "马尔代夫",
)

/**
 * 国家/地区英文名 -> 简体中文。仅用于国家层级，机房名称保持服务端原名。
 */
private val regionSuffixZh = mapOf(
    "west" to "西部", "east" to "东部", "central" to "中部", "north" to "北部",
    "south" to "南部", "midwest" to "中西部", "northeast" to "东北部",
    "northwest" to "西北部", "southeast" to "东南部", "southwest" to "西南部",
)

fun countryNameToZh(name: String?): String {
    if (name.isNullOrBlank()) return "未知"
    val trimmed = name.trim()
    val key = trimmed.lowercase()
    countryZhMap[key]?.let { return it }

    // "US West" / "Canada East" 这类：拆成国家 + 方位
    val parts = trimmed.split(" ", limit = 2)
    if (parts.size == 2) {
        val base = countryZhMap[parts[0].lowercase()]
        val suffix = regionSuffixZh[parts[1].lowercase()]
        if (base != null && suffix != null) return base + suffix
    }
    return trimmed
}

/**
 * Convert 2-letter ISO country code (e.g. "HK", "US", "JP") to real Unicode Flag Emoji
 */
fun countryCodeToEmoji(countryCode: String?): String {
    if (countryCode == null || countryCode.length != 2) return "🌐"
    val code = countryCode.uppercase()
    val firstChar = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
    val secondChar = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
    return try {
        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    } catch (e: Exception) {
        "🌐"
    }
}

/**
 * Format real-time network traffic speed
 */
fun formatTrafficSpeed(bytesPerSec: Long): String {
    return when {
        bytesPerSec >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
        bytesPerSec >= 1024 -> String.format(java.util.Locale.US, "%.1f KB/s", bytesPerSec / 1024.0)
        else -> String.format(java.util.Locale.US, "%.1f KB/s", (bytesPerSec / 1024.0).coerceAtLeast(0.0))
    }
}

/**
 * Real-time Hardware Accelerated Spline Waveform Canvas
 */
@Composable
fun LiveWaveformCanvas(
    isConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(65.dp)) {
            val width = size.width
            val height = size.height

            if (isConnected) {
                // Downlink Cyan Wave
                val pathDown = Path()
                pathDown.moveTo(0f, height * 0.6f)
                val points = 24
                for (i in 0..points) {
                    val x = (width / points) * i
                    val sinFactor = Math.sin((i * 0.45 + phase).toDouble()).toFloat()
                    val cosFactor = Math.cos((i * 0.25 - phase).toDouble()).toFloat()
                    val y = (height * 0.5f) + (sinFactor * 18f) + (cosFactor * 8f)
                    pathDown.lineTo(x, y.coerceIn(4f, height - 4f))
                }
                drawPath(
                    path = pathDown,
                    color = NeonCyan,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Uplink Purple Wave
                val pathUp = Path()
                pathUp.moveTo(0f, height * 0.75f)
                for (i in 0..points) {
                    val x = (width / points) * i
                    val sinFactor = Math.cos((i * 0.35 + phase * 1.2).toDouble()).toFloat()
                    val y = (height * 0.7f) + (sinFactor * 12f)
                    pathUp.lineTo(x, y.coerceIn(6f, height - 4f))
                }
                drawPath(
                    path = pathUp,
                    color = NeonPurple,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                // Idle gentle wave
                val pathIdle = Path()
                pathIdle.moveTo(0f, height * 0.8f)
                val points = 20
                for (i in 0..points) {
                    val x = (width / points) * i
                    val sinFactor = Math.sin((i * 0.3 + phase * 0.5).toDouble()).toFloat()
                    val y = (height * 0.8f) + (sinFactor * 5f)
                    pathIdle.lineTo(x, y)
                }
                drawPath(
                    path = pathIdle,
                    color = TextMuted.copy(alpha = 0.4f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Suspended Floating Glass Dock Navigation Bar
 */
@Composable
fun FloatingGlassDock(
    currentTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xE60F1420))
            .border(
                width = 1.dp,
                color = Color(0x1FFFFFFF),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                title = "控制台",
                iconRes = R.drawable.ic_preferences_icon,
                isSelected = currentTabIndex == 0,
                onClick = { onTabSelected(0) }
            )
            DockItem(
                title = "节点矩阵",
                iconRes = R.drawable.ic_location_load,
                isSelected = currentTabIndex == 1,
                onClick = { onTabSelected(1) }
            )
            DockItem(
                title = "偏好设置",
                iconRes = R.drawable.ic_feel,
                isSelected = currentTabIndex == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun DockItem(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier.size(22.dp),
            tint = if (isSelected) NeonCyan else TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (isSelected) NeonCyan else TextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Cyber Glass Top Navigation Bar for Redesigned Subpages
 */
@Composable
fun CyberTopAppBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33121826))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow_white),
                    contentDescription = "Back",
                    modifier = Modifier.size(18.dp),
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

/**
 * Modern Cyber Switch with Glowing States
 */
@Composable
fun CyberSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val thumbOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (checked) 26.dp else 3.dp,
        animationSpec = androidx.compose.animation.core.tween(180),
        label = "thumbOffset"
    )
    val trackColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) Color(0x334FF0C0) else Color(0x14FFFFFF),
        label = "track"
    )
    val borderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) Color(0x884FF0C0) else Color(0x24FFFFFF),
        label = "border"
    )
    val thumbColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) Color(0xFF4FF0C0) else Color(0xFF9AA8B4),
        label = "thumb"
    )

    Box(
        modifier = modifier
            .size(width = 54.dp, height = 31.dp)
            .drawBehind {
                if (!checked) return@drawBehind
                val r = size.maxDimension
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4FF0C0).copy(alpha = 0.20f), Color.Transparent),
                        center = center,
                        radius = r
                    ),
                    radius = r,
                    center = center
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(25.dp)
                .drawBehind {
                    if (checked) {
                        val r = size.maxDimension * 0.9f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4FF0C0).copy(alpha = 0.55f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = r
                            ),
                            radius = r,
                            center = center
                        )
                    }
                }
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun CyberSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(13.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) NeonGreen.copy(alpha = 0.14f) else Color.Transparent)
                    .then(
                        if (selected) {
                            Modifier.border(
                                width = 1.dp,
                                color = NeonGreen.copy(alpha = 0.28f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(index) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) NeonGreen else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Categorized Section Header with Neon Accent Mark
 */
@Composable
fun CyberSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp
        )
    }
}

/**
 * Cyber Badge with Neon Border
 */
@Composable
fun CyberBadge(
    text: String,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Cyber Dropdown Selector
 */
@Composable
fun <T> CyberDropdown(
    label: String,
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemToString: (T) -> String = { it.toString() },
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xB30D1218))
                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = itemToString(selectedItem),
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_change_protocol_arrow),
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(12.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xF00A0E17))
                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp))
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemToString(item),
                            color = if (isSelected) NeonCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Cyber Slider with Cyan Neon Glow
 */
@Composable
fun CyberSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = NeonCyan,
            activeTrackColor = NeonCyan,
            inactiveTrackColor = Color(0x331E293B),
        )
    )
}

/**
 * Cyber Styled Text Field
 */
@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = TextMuted,
                fontSize = 12.sp
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = androidx.compose.material3.TextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = Color(0xB30D1218),
            unfocusedContainerColor = Color(0x800E1420),
            focusedIndicatorColor = NeonCyan,
            unfocusedIndicatorColor = Color(0x33FFFFFF),
            cursorColor = NeonCyan,
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    )
}

/**
 * Cyber Glass Button
 */
@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .softGlow(color, enabled, 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) {
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.14f), color.copy(alpha = 0.05f))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0x11FFFFFF), Color(0x08FFFFFF))
                    )
                }
            )
            .border(
                1.dp,
                if (enabled) color.copy(alpha = 0.45f) else Color(0x22FFFFFF),
                RoundedCornerShape(14.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) color else TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CyberBandwidthGauge(
    isUnlimited: Boolean,
    dataLeft: String = "",
    totalData: String = "",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isUnlimited) "高速专用隧道数据" else "可用剩余流量",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isUnlimited) "UNLIMITED ∞" else "$dataLeft / $totalData",
                color = if (isUnlimited) NeonGreen else NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x331E293B))
        ) {
            val progressFraction = if (isUnlimited) {
                1.0f
            } else {
                val leftNum = dataLeft.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
                val totalNum = totalData.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 10f
                if (totalNum > 0f) (leftNum / totalNum).coerceIn(0.05f, 1.0f) else 0.5f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progressFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isUnlimited) {
                                listOf(NeonCyan, NeonGreen)
                            } else {
                                listOf(Color(0xFF818CF8), NeonCyan)
                            }
                        )
                    )
            )
        }
    }
}


/**
 * 形状外柔和光晕：多层同形状圆角矩形向外扩散、透明度递减。
 */
fun Modifier.softGlow(
    color: Color,
    enabled: Boolean = true,
    corner: Dp = 14.dp,
): Modifier =
    this.drawBehind {
        if (!enabled) return@drawBehind
        val steps = 12
        val baseRadius = corner.toPx()
        val w = size.width
        val h = size.height
        for (i in steps downTo 1) {
            val grow = i * 1.1.dp.toPx()
            val alpha = (1f - i / (steps + 1f)) * 0.09f
            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = androidx.compose.ui.geometry.Offset(-grow, -grow),
                size = androidx.compose.ui.geometry.Size(w + grow * 2, h + grow * 2),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    baseRadius + grow,
                    baseRadius + grow
                )
            )
        }
    }
