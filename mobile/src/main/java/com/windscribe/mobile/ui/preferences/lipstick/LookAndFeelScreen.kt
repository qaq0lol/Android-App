package com.windscribe.mobile.ui.preferences.lipstick

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.connection.ToastMessage
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.nav.Screen
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.DARK_THEME
import com.windscribe.vpn.apppreference.PreferencesKeyConstants.LIGHT_THEME

@Composable
fun LookAndFeelScreen() {
    val activity = androidx.activity.compose.LocalActivity.current as? com.windscribe.mobile.ui.AppStartActivity
    val viewmodel: LipstickViewmodel = activity?.let {
        hiltViewModel<LipstickViewmodelImpl>(viewModelStoreOwner = it)
    } ?: hiltViewModel<LipstickViewmodelImpl>()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val toastMessage by viewmodel.toastMessage.collectAsState()
    val selectedAppIcon by viewmodel.selectedAppIcon.collectAsState()
    val currentTheme by viewmodel.themeItem.collectAsState()

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
                subtitle = "THEME & DISGUISE ICONS",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // ================= Section 1: 主题 (1:1 原型 Screen 11) =================
                CyberSectionHeader(title = "界面主题")
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

                Spacer(modifier = Modifier.height(16.dp))

                // ================= Section 2: 伪装图标 (1:1 原型 Screen 11) =================
                CyberSectionHeader(title = "桌面伪装隐身图标")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x26B299FF)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "伪装图标",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "在桌面显示为计算器、时钟或极客图标",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x26B299FF))
                                    .clickable { navController.navigate(Screen.CustomIcon.route) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "全部 ›",
                                    color = NeonPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Horizontal Disguise Icon Preview Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Calculator
                            IconPreviewBox(
                                resId = com.windscribe.mobile.R.mipmap.ic_launcher_calculator,
                                onClick = { navController.navigate(Screen.CustomIcon.route) }
                            )
                            // Clock
                            IconPreviewBox(
                                resId = com.windscribe.mobile.R.mipmap.ic_launcher_clock,
                                onClick = { navController.navigate(Screen.CustomIcon.route) }
                            )
                            // 80 Retro
                            IconPreviewBox(
                                resId = com.windscribe.mobile.R.mipmap.ic_launcher_eighty,
                                onClick = { navController.navigate(Screen.CustomIcon.route) }
                            )
                            // Neon
                            IconPreviewBox(
                                resId = com.windscribe.mobile.R.mipmap.ic_launcher_neon,
                                onClick = { navController.navigate(Screen.CustomIcon.route) }
                            )
                            // '+' More
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable { navController.navigate(Screen.CustomIcon.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ================= Section 4: 实时生效说明卡 =================
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "主题与伪装图标切换后全局即刻生效",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        CyberBadge(text = "实时", color = NeonGreen)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun IconPreviewBox(
    resId: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(resId) {
        ContextCompat.getDrawable(context, resId)?.toBitmap()?.asImageBitmap()
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1AFFFFFF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

/**
 * 主题预览卡：真实微缩图 + 选中态发光环与圆点。
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
                    .height(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(previewBg),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(1.5.dp, ringColor, CircleShape)
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
                        .clip(CircleShape)
                        .background(if (selected) ringColor else Color(0x33FFFFFF))
                )
            }
        }
    }
}
