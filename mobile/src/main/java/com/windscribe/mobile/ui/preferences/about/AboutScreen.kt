package com.windscribe.mobile.ui.preferences.about

import androidx.annotation.StringRes
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
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
import com.windscribe.mobile.ui.common.openUrl
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.vpn.constants.NetworkKeyConstants

@Composable
fun AboutScreen() {
    val navController = LocalNavController.current
    val scrollState = rememberScrollState()

    val aboutItems = listOf(
        com.windscribe.vpn.R.string.status to NetworkKeyConstants.URL_STATUS,
        com.windscribe.vpn.R.string.about to NetworkKeyConstants.URL_ABOUT,
        com.windscribe.vpn.R.string.privacy_policy to NetworkKeyConstants.URL_PRIVACY,
        com.windscribe.vpn.R.string.terms_title to NetworkKeyConstants.URL_TERMS,
        com.windscribe.vpn.R.string.blog to NetworkKeyConstants.URL_BLOG,
        com.windscribe.vpn.R.string.jobs to NetworkKeyConstants.URL_JOB,
        com.windscribe.vpn.R.string.software_licenses to NetworkKeyConstants.URL_VIEW_LICENCE,
        com.windscribe.vpn.R.string.changelog to NetworkKeyConstants.URL_CHANGELOG,
    )

    val context = LocalContext.current
    val versionLabel = runCatching {
        context.packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.PackageInfoFlags.of(0)
        ).versionName
    }.getOrNull() ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "关于 Windscribe",
                subtitle = "RELEASE INFO, PRIVACY & LICENSES",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Brand Header Card
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    borderColor = NeonCyan.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_about),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "WINDSCRIBE",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "版本 $versionLabel · 纯原生 Android 构建",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CyberBadge(text = "Android 16+ API 36", color = NeonCyan)
                            CyberBadge(text = "Kernel TUN Engine", color = NeonGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                CyberSectionHeader(title = "官方站点与法律条款")

                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        aboutItems.forEachIndexed { index, (titleRes, url) ->
                            CyberAboutItem(
                                titleRes = titleRes,
                                path = url,
                                showDivider = index != aboutItems.lastIndex
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CyberAboutItem(
    @StringRes titleRes: Int,
    path: String,
    showDivider: Boolean = true,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { context.openUrl(path) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(titleRes),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_forward_arrow_white),
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LocalGlassColors.current.borderSubtle)
            )
        }
    }
}
