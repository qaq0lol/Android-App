package com.windscribe.mobile.ui.serverlist

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.connection.BridgeApiViewModel
import com.windscribe.mobile.ui.helper.hapticClickable
import com.windscribe.mobile.ui.theme.serverListBackgroundColor
import com.windscribe.mobile.ui.theme.serverListNavigationGradientEnd
import com.windscribe.mobile.ui.theme.serverListSecondaryColor

data class ServerTabIcon(
    val unfilledIcon: Int,
    val filledIcon: Int,
    val type: ServerListType,
)

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun ServerListNavigation(
    modifier: Modifier,
    viewModel: ServerViewModel,
    bridgeApiViewModel: BridgeApiViewModel,
    onTabSelected: (Int) -> Unit,
) {
    val selectedType by viewModel.selectedServerListType.collectAsState()
    val favouriteAnimationCounter by bridgeApiViewModel.favouriteIconAnimation.collectAsState()
    var animateFavIcon by remember { mutableStateOf(false) }

    LaunchedEffect(favouriteAnimationCounter) {
        if (favouriteAnimationCounter > 0) {
            animateFavIcon = true
            kotlinx.coroutines.delay(400)
            animateFavIcon = false
        }
    }

    val serverTabs =
        listOf(
            ServerTabIcon(
                R.drawable.ic_location_all,
                R.drawable.ic_location_all_filled,
                ServerListType.All,
            ),
            ServerTabIcon(
                R.drawable.ic_location_fav,
                R.drawable.ic_location_fav_filled,
                ServerListType.Fav,
            ),
            ServerTabIcon(
                R.drawable.ic_location_static,
                R.drawable.ic_location_static_filled,
                ServerListType.Static,
            ),
            ServerTabIcon(
                R.drawable.ic_location_config,
                R.drawable.ic_location_config_filled,
                ServerListType.Config,
            ),
        )
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth().height(54.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            serverTabs.forEachIndexed { index, tab ->
                val isSelected = selectedType == tab.type
                val isFavTab = tab.type == ServerListType.Fav
                val scale by animateFloatAsState(
                    targetValue = if (animateFavIcon && isFavTab) 1.20f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "favIconScale",
                )
                val description =
                    when (tab.type) {
                        ServerListType.All -> com.windscribe.vpn.R.string.all_vpn_locations
                        ServerListType.Fav -> com.windscribe.vpn.R.string.favourite
                        ServerListType.Static -> com.windscribe.vpn.R.string.static_ip
                        ServerListType.Config -> com.windscribe.vpn.R.string.custom_configs
                    }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(46.dp)
                            .padding(horizontal = 2.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            )
                            .testTag("tab_${tab.type.name.lowercase()}")
                            .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(if (isSelected) tab.filledIcon else tab.unfilledIcon),
                        contentDescription = androidx.compose.ui.res.stringResource(description),
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale).height(24.dp).width(24.dp),
                        colorFilter =
                            ColorFilter.tint(
                                if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .testTag("search_open")
                        .hapticClickable { viewModel.toggleSearch() },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_location_search),
                    contentDescription = androidx.compose.ui.res.stringResource(com.windscribe.vpn.R.string.search),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }
    }
}
