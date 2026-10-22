package com.windscribe.mobile.ui.preferences.lipstick

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberDropdown
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.model.DropDownItem
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.DARK_THEME
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.LIGHT_THEME

@Composable
fun LookAndFeelScreen(viewmodel: LipstickViewmodel = hiltViewModel<LipstickViewmodelImpl>()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val toastMessage by viewmodel.toastMessage.collectAsState()
    val selectedAppIcon by viewmodel.selectedAppIcon.collectAsState()
    val currentTheme by viewmodel.themeItem.collectAsState()

    val disconnectedBg by viewmodel.whenDisconnectedBackgroundItem.collectAsState()
    val connectedBg by viewmodel.whenConnectedBackgroundItem.collectAsState()
    val aspectRatio by viewmodel.aspectRatioItem.collectAsState()
    val disconnectedSound by viewmodel.whenDisconnectedSoundItem.collectAsState()
    val connectedSound by viewmodel.whenConnectedSoundItem.collectAsState()

    LaunchedEffect(toastMessage) {
        when (val toast = toastMessage) {
            is ToastMessage.Localized -> {
                Toast.makeText(context, toast.message, Toast.LENGTH_SHORT).show()
                viewmodel.clearToast()
            }
            is ToastMessage.Raw -> {
                Toast.makeText(context, toast.message, Toast.LENGTH_SHORT).show()
                viewmodel.clearToast()
            }
            else -> {}
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
                title = "界面外观",
                subtitle = "THEME, DISGUISE ICONS, AUDIO & WALLPAPERS",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Section 1: Theme
                CyberSectionHeader(title = "界面主题")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "选择视觉模式",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isDark = currentTheme.id == DARK_THEME
                            val themeOptions = LookAndFeelHelper.getThemeOptions()

                            // 深色主题预览卡
                            ThemePreviewCard(
                                modifier = Modifier.weight(1f),
                                label = "深色",
                                dark = true,
                                selected = isDark,
                                previewBg = Color(0xFF06090D),
                                ringColor = NeonGreen,
                                onClick = {
                                    themeOptions.firstOrNull { it.id == DARK_THEME }?.let {
                                        viewmodel.onThemeItemSelected(it)
                                    }
                                }
                            )

                            // 浅色主题预览卡
                            ThemePreviewCard(
                                modifier = Modifier.weight(1f),
                                label = "浅色",
                                dark = false,
                                selected = !isDark,
                                previewBg = Color(0xFFEEF2F5),
                                ringColor = Color(0xFF00A47B),
                                onClick = {
                                    themeOptions.firstOrNull { it.id == LIGHT_THEME }?.let {
                                        viewmodel.onThemeItemSelected(it)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: App Disguise Icon
                CyberSectionHeader(title = "桌面伪装隐身图标")
                val iconBitmap = remember(selectedAppIcon) {
                    ContextCompat.getDrawable(context, selectedAppIcon)?.toBitmap()?.asImageBitmap()
                }

                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.CustomIcon.route) },
                    cornerRadius = 20.dp,
                    borderColor = Color(0x26B299FF)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (iconBitmap != null) {
                                Image(
                                    bitmap = iconBitmap,
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x1FFFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_feel),
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "应用伪装图标 (Stealth Icon)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "可伪装为计算器、时钟或极客霓虹图标",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CyberBadge(text = "切换图标", color = NeonPurple)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_forward_arrow_white),
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Background & Wallpaper Customization
                CyberSectionHeader(title = "背景壁纸自定义")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Disconnected background
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "未连线背景",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "断开隧道时的底图壁纸",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberDropdown(
                                label = "Disconnected Bg",
                                selectedItem = disconnectedBg,
                                items = LookAndFeelHelper.getBackgroundOptions(),
                                onItemSelected = { viewmodel.onWhenDisconnectedBackgroundItemSelected(it) },
                                itemToString = { if (it.label.isNotBlank()) it.label else if (it.title != 0) context.getString(it.title) else "" }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

                        // Connected background
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "已连线背景",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "VPN 握手建立后的底图壁纸",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberDropdown(
                                label = "Connected Bg",
                                selectedItem = connectedBg,
                                items = LookAndFeelHelper.getBackgroundOptions(),
                                onItemSelected = { viewmodel.onWhenConnectedBackgroundItemSelected(it) },
                                itemToString = { if (it.label.isNotBlank()) it.label else if (it.title != 0) context.getString(it.title) else "" }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

                        // Aspect ratio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "壁纸缩放比例",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "图像拉伸与填充比例",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberDropdown(
                                label = "Aspect Ratio",
                                selectedItem = aspectRatio,
                                items = LookAndFeelHelper.getAspectRatioOptions(),
                                onItemSelected = { viewmodel.onAspectRatioItemSelected(it) },
                                itemToString = { if (it.label.isNotBlank()) it.label else if (it.title != 0) context.getString(it.title) else "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Sound Notifications
                CyberSectionHeader(title = "连接音效反馈")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Disconnected sound
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "断线音效",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "断开隧道时播放提示音",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberDropdown(
                                label = "Disconnected Sound",
                                selectedItem = disconnectedSound,
                                items = LookAndFeelHelper.getSoundOptions(),
                                onItemSelected = { viewmodel.onWhenDisconnectedSoundItemSelected(it) },
                                itemToString = { if (it.label.isNotBlank()) it.label else if (it.title != 0) context.getString(it.title) else "" }
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))

                        // Connected sound
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "连线成功音效",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "VPN 建立成功时播放提示音",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            CyberDropdown(
                                label = "Connected Sound",
                                selectedItem = connectedSound,
                                items = LookAndFeelHelper.getSoundOptions(),
                                onItemSelected = { viewmodel.onWhenConnectedSoundItemSelected(it) },
                                itemToString = { if (it.label.isNotBlank()) it.label else if (it.title != 0) context.getString(it.title) else "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * 主题预览卡：用一块真实配色的缩略图代替纯文字，选中态描边 + 右下角圆点。
 */
@Composable
private fun ThemePreviewCard(
    modifier: Modifier = Modifier,
    label: String,
    dark: Boolean,
    selected: Boolean,
    previewBg: Color,
    ringColor: Color,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 14.dp,
        borderColor = if (selected) ringColor.copy(alpha = 0.55f) else Color(0x1FFFFFFF),
        backgroundColor = if (selected) Color(0x1FFFFFFF) else Color(0x4D0D1218)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(previewBg),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(1.5.dp, ringColor, RoundedCornerShape(percent = 50))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = if (selected) ringColor else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (selected) ringColor else Color(0x33FFFFFF))
                )
            }
        }
    }
}
