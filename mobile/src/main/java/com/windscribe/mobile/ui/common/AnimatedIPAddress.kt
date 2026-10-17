package com.windscribe.mobile.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.windscribe.mobile.ui.connection.BridgeApiViewModel
import com.windscribe.mobile.ui.connection.ConnectionViewmodel

/**
 * Displays the current VPN IP in a stable, tabular layout.
 *
 * Per-digit rolling animations repeatedly recreated digit nodes when the IP changed. That
 * caused the address to jump horizontally and occasionally show mixed digits. Keep the address
 * as one text node so updates do not move the surrounding home-screen controls.
 */
@Composable
fun AnimatedIPAddress(
    connectionViewmodel: ConnectionViewmodel,
    bridgeApiViewModel: BridgeApiViewModel,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(fontSize = 20.sp),
    color: Color = Color.Black,
) {
    val ipAddress by connectionViewmodel.ipState.collectAsState()
    val shouldAnimate by connectionViewmodel.shouldAnimateIp.collectAsState()
    val isRotatingIp by bridgeApiViewModel.isRotatingIp.collectAsState()

    AnimatedIPAddressContent(
        ipAddress = ipAddress,
        shouldAnimate = shouldAnimate,
        isRotatingIp = isRotatingIp,
        onIpAnimationComplete = { connectionViewmodel.onIpAnimationComplete() },
        modifier = modifier,
        style = style,
        color = color,
    )
}

@Composable
fun AnimatedIPAddressContent(
    ipAddress: String,
    shouldAnimate: Boolean,
    isRotatingIp: Boolean,
    onIpAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(fontSize = 20.sp),
    color: Color = Color.Black,
) {
    // The previous digit-by-digit animation changed each glyph's width and remounted it on
    // every update. Use tabular numerals and a single Text node to keep the IP stable.
    val displayIp = if (isRotatingIp && ipAddress.contains("--")) "000.000.000.000" else ipAddress

    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            onIpAnimationComplete()
        }
    }

    Text(
        text = displayIp,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
