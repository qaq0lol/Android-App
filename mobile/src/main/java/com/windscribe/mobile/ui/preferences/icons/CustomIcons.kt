package com.windscribe.mobile.ui.preferences.icons

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.LocalGlassColors
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.helper.MultiDevicePreview
import com.windscribe.mobile.ui.helper.PreviewWithNav
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.preferences.icons.AppIconManager.IconConfig
import com.windscribe.vpn.constants.ExtraConstants

/**
 * Callbacks the custom-icons UI can raise. Hoisted out of the composables so the stateless
 * [CustomIconsContent] never needs to know about [CustomIconsViewModel].
 */
class CustomIconsActions(
    val onIconSelected: (AppIcon) -> Unit = {},
    val onConfirmIconChange: () -> Unit = {},
    val onDismissDialog: () -> Unit = {},
)

@Composable
fun CustomIconsScreen(viewModel: CustomIconsViewModel = hiltViewModel<CustomIconsViewModelImpl>()) {
    val icons by viewModel.icons.collectAsState()
    val showDialog by viewModel.showConfirmDialog.collectAsState()

    CustomIconsContent(
        icons = icons,
        showDialog = showDialog,
        actions =
            CustomIconsActions(
                onIconSelected = viewModel::selectIcon,
                onConfirmIconChange = viewModel::confirmIconChange,
                onDismissDialog = viewModel::dismissDialog,
            ),
    )
}

@Composable
fun CustomIconsContent(
    icons: Map<String, AppIcon>,
    showDialog: AppIcon?,
    actions: CustomIconsActions,
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
                title = stringResource(com.windscribe.vpn.R.string.app_icon),
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Info header card
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                painter = painterResource(id = R.drawable.ic_appearance),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "桌面图标伪装与自订",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "支持伪装为计算器、时钟或切换多套赛博风格图标",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppIconCategorySection(
                    title = "伪装与隐蔽图标 (Discreet)",
                    category = AppIconCategory.Discreet,
                    icons = icons,
                    onIconSelected = actions.onIconSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppIconCategorySection(
                    title = "官方与定制主题 (Windscribe)",
                    category = AppIconCategory.Windscribe,
                    icons = icons,
                    onIconSelected = actions.onIconSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppIconCategorySection(
                    title = "彩蛋与扩展图标 (Other)",
                    category = AppIconCategory.Other,
                    icons = icons,
                    onIconSelected = actions.onIconSelected
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Show confirmation dialog
    showDialog?.let { appIcon ->
        IconChangeConfirmDialog(
            iconName = appIcon.name,
            onConfirm = actions.onConfirmIconChange,
            onDismiss = actions.onDismissDialog,
        )
    }
}

@Composable
private fun IconChangeConfirmDialog(
    iconName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 24.dp,
            borderColor = NeonCyan.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_appearance),
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "更换桌面启动图标？",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "将桌面图标更换为「$iconName」将会重启应用。\n您可以稍后在系统桌面通过新图标重新启动。",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberButton(
                        text = "取消",
                        onClick = onDismiss,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    CyberButton(
                        text = "确认更换",
                        onClick = onConfirm,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIconCategorySection(
    title: String,
    category: AppIconCategory,
    icons: Map<String, AppIcon>,
    onIconSelected: (AppIcon) -> Unit,
) {
    val filteredIcons by remember(icons, category) {
        derivedStateOf {
            icons.values.filter { it.category == category }
        }
    }

    if (filteredIcons.isEmpty()) return

    CyberSectionHeader(title = title)

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            filteredIcons.forEachIndexed { index, item ->
                AppIconRow(
                    icon = item,
                    onClick = { onIconSelected(item) }
                )
                if (index < filteredIcons.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LocalGlassColors.current.borderSubtle)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIconRow(
    icon: AppIcon,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap = remember(icon.icon) {
        ContextCompat.getDrawable(context, icon.icon)?.toBitmap()?.asImageBitmap()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bitmap?.let { imageBitmap ->
            Image(
                bitmap = imageBitmap,
                contentDescription = icon.name,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (icon.isSelected) NeonCyan else Color(0x22FFFFFF),
                        RoundedCornerShape(12.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = icon.name,
                color = if (icon.isSelected) NeonCyan else TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (icon.isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (icon.isSelected) "当前使用的桌面图标" else "点击应用此图标",
                color = if (icon.isSelected) NeonGreen else TextMuted,
                fontSize = 10.sp
            )
        }

        if (icon.isSelected) {
            CyberBadge(
                text = "ACTIVE",
                color = NeonCyan
            )
        }
    }
}


private class CustomIconsStateProvider : PreviewParameterProvider<AppIcon?> {
    override val values =
        sequenceOf(
            null,
        )
}

@Composable
@MultiDevicePreview
private fun CustomIconScreenPreview(
    @PreviewParameter(CustomIconsStateProvider::class) showDialog: AppIcon?,
) {
    val context = LocalContext.current
    val icons = previewIcons(context)
    PreviewWithNav {
        CustomIconsContent(
            icons = icons,
            showDialog = showDialog,
            actions = CustomIconsActions(),
        )
    }
}

private fun previewIcons(context: Context): Map<String, AppIcon> {
    val selectedCustomIcon = "Clock"
    // List of icon configurations - add or remove items here
    val iconConfigs =
        listOf(
            IconConfig(
                "Clock",
                com.windscribe.mobile.R.mipmap.ic_launcher_clock,
                AppIconCategory.Discreet,
            ),
            IconConfig(
                "Calculator",
                com.windscribe.mobile.R.mipmap.ic_launcher_calculator,
                AppIconCategory.Discreet,
            ),
            IconConfig(
                ExtraConstants.DEFAULT_ICON,
                com.windscribe.vpn.R.mipmap.ws_launcher,
                AppIconCategory.Windscribe,
            ),
            IconConfig(
                "Vapor",
                com.windscribe.mobile.R.mipmap.ic_launcher_vapor,
                AppIconCategory.Windscribe,
            ),
            IconConfig(
                "Glitch",
                com.windscribe.mobile.R.mipmap.ic_launcher_glitch,
                AppIconCategory.Windscribe,
            ),
            IconConfig(
                "WindPass",
                com.windscribe.mobile.R.mipmap.ic_launcher_pass,
                AppIconCategory.Other,
            ),
            IconConfig(
                "BSVpn",
                com.windscribe.mobile.R.mipmap.ic_launcher_bs,
                AppIconCategory.Other,
            ),
            IconConfig(
                "DickButt",
                com.windscribe.mobile.R.mipmap.ic_launcher_butt,
                AppIconCategory.Other,
            ),
        )
    return iconConfigs.associate { config ->
        val component = AppIconManager.getComponentName(context, config.name)
        val appIcon =
            AppIcon(
                name = config.name,
                icon = config.iconRes,
                component = component,
                category = config.category,
                isSelected = selectedCustomIcon == config.name,
            )
        config.name to appIcon
    }
}
