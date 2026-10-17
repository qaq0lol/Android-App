package com.windscribe.mobile.ui.upgrade

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.AppBackground
import com.windscribe.mobile.ui.common.AppProgressBar
import com.windscribe.mobile.ui.helper.MultiDevicePreview
import com.windscribe.mobile.ui.helper.PreviewWithNav
import com.windscribe.mobile.ui.helper.hapticClickable
import com.windscribe.mobile.ui.theme.font12
import com.windscribe.mobile.ui.theme.font14

/**
 * Callbacks the upgrade UI can raise. Hoisted out of the composables so the stateless
 * [UpgradeContent] never needs to know about the view models — previews supply no-op lambdas.
 */
class UpgradeActions(
    val onClose: () -> Unit = {},
    val onRestore: () -> Unit = {},
    val onSelectMonthly: () -> Unit = {},
    val onSelectYearly: () -> Unit = {},
    val onSubscribe: () -> Unit = {},
    val onTermsClick: () -> Unit = {},
    val onPrivacyClick: () -> Unit = {},
    val onDismissError: () -> Unit = {},
)

/**
 * Stateless upgrade UI mirroring the original `activity_upgrade.xml`. Everything it needs is
 * passed in, so it renders identically in the app and in `@Preview`. The flavor-specific
 * `UpgradeScreen` owns the state and wires the actions.
 */
@Composable
fun UpgradeContent(
    state: UpgradeState,
    actions: UpgradeActions,
) {
    AppBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeroSection(state, actions)
            FeatureRow(
                titleRes = com.windscribe.vpn.R.string.unlimited_everything,
                subtitleRes = com.windscribe.vpn.R.string.use_on_all_devices_with_no_data_limits,
                topPadding = 24.dp,
            )
            FeatureRow(
                titleRes = com.windscribe.vpn.R.string.all_vpn_locations,
                subtitleRes = com.windscribe.vpn.R.string.servers_in_cities_more_than_any_other_vpn,
                topPadding = 10.dp,
            )
            FeatureRow(
                titleRes = com.windscribe.vpn.R.string.increased_speed_and_security,
                subtitleRes = com.windscribe.vpn.R.string.blocks_malicious_websites_trackers,
                topPadding = 10.dp,
            )
            PlansRow(state, actions)
            GradientGlareButton(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                text = stringResource(com.windscribe.vpn.R.string.subscribe),
                enabled = state.selectedSku != null,
                onClick = actions.onSubscribe,
            )
            Text(
                text = stringResource(com.windscribe.vpn.R.string.subscriptions_info),
                style = font12.copy(fontSize = 10.sp, textAlign = TextAlign.Start),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp),
            )
            TermsLine(actions)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (state.errorMessage != null) {
            ErrorOverlay(message = state.errorMessage, onDismiss = actions.onDismissError)
        }
        AppProgressBar(state.showProgress, message = state.loadingMessage ?: "")
    }
}

/**
 * Material You hero header using the current dynamic palette and a native security icon.
 */
@Composable
private fun HeroSection(
    state: UpgradeState,
    actions: UpgradeActions,
) {
    val foreground = MaterialTheme.colorScheme.onPrimaryContainer
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = foreground,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.IconButton(onClick = actions.onClose) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(com.windscribe.vpn.R.string.cancel),
                        tint = foreground,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (state.showRestore) {
                    androidx.compose.material3.TextButton(onClick = actions.onRestore) {
                        Text(
                            text = stringResource(com.windscribe.vpn.R.string.restore),
                            style = MaterialTheme.typography.labelLarge,
                            color = foreground,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(com.windscribe.vpn.R.string.upgrade_to_pro),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = foreground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(com.windscribe.vpn.R.string.go_pro_for_unlimited_everything),
                style = MaterialTheme.typography.bodyLarge,
                color = foreground.copy(alpha = 0.82f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun FeatureRow(
    titleRes: Int,
    subtitleRes: Int,
    topPadding: Dp,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = topPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(titleRes),
                style = font14.copy(textAlign = TextAlign.Start),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(subtitleRes),
                style = font12.copy(textAlign = TextAlign.Start),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_star),
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun PlansRow(
    state: UpgradeState,
    actions: UpgradeActions,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.monthly?.let { tile ->
            PlanTileView(
                modifier = Modifier.weight(1f),
                tile = tile,
                titleRes = com.windscribe.vpn.R.string.plan_monthly,
                selected = state.monthlySelected,
                showRadio = !state.isPromo,
                onClick = actions.onSelectMonthly,
            )
        }
        state.yearly?.let { tile ->
            PlanTileView(
                modifier = Modifier.weight(1f),
                tile = tile,
                titleRes = com.windscribe.vpn.R.string.plan_yearly,
                selected = !state.monthlySelected,
                showRadio = !state.isPromo,
                onClick = actions.onSelectYearly,
            )
        }
    }
}

@Composable
private fun PlanTileView(
    modifier: Modifier,
    tile: PlanTile,
    titleRes: Int,
    selected: Boolean,
    showRadio: Boolean,
    onClick: () -> Unit,
) {
    val cardShape = MaterialTheme.shapes.large
    val cardColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val cardTextColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Box(
        modifier =
            modifier
                .height(142.dp)
                .clip(cardShape)
                .background(cardColor)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = cardShape,
                )
                .hapticClickable { onClick() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(titleRes),
                    style = font14.copy(fontWeight = FontWeight.Normal, textAlign = TextAlign.Start),
                    color = cardTextColor,
                )
                tile.discountLabel?.let { label ->
                    Text(
                        text = label,
                        style = font14.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier =
                            Modifier
                                .padding(start = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.extraSmall,
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tile.price,
                style = font14.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Start),
                color = cardTextColor,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text =
                    buildAnnotatedString {
                        tile.billedLabel.strikeThroughPrice?.let { struck ->
                            withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                                append(struck)
                            }
                            append(" ")
                        }
                        append(tile.billedLabel.text)
                    },
                style = font14.copy(fontWeight = FontWeight.Normal, textAlign = TextAlign.Start),
                color = cardTextColor.copy(alpha = 0.82f),
                maxLines = 2,
            )
        }
        if (showRadio) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp)) {
                if (selected) {
                    Image(
                        painter = painterResource(R.drawable.checked_radio_circle),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                ),
                    )
                }
            }
        }
        tile.promoLabel?.let { promo ->
            Text(
                text = promo.uppercase(),
                style = font14.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.extraSmall,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun TermsLine(actions: UpgradeActions) {
    val appName = stringResource(com.windscribe.vpn.R.string.app_name)
    val termsPolicy = stringResource(com.windscribe.vpn.R.string.terms_policy_en)
    val linkStyles =
        TextLinkStyles(
            style =
                SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = TextDecoration.Underline,
                ),
        )
    val annotated =
        buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))) {
                append(appName)
                append(" ")
            }
            val separatorIndex = termsPolicy.indexOf('&')
            if (separatorIndex == -1) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))) {
                    append(termsPolicy)
                }
            } else {
                val termsPart = termsPolicy.substring(0, separatorIndex).trim()
                val privacyPart = termsPolicy.substring(separatorIndex + 1).trim()
                withLink(LinkAnnotation.Clickable("terms", linkStyles) { actions.onTermsClick() }) {
                    append(termsPart)
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))) {
                    append(" & ")
                }
                withLink(LinkAnnotation.Clickable("privacy", linkStyles) { actions.onPrivacyClick() }) {
                    append(privacyPart)
                }
            }
        }
    Text(
        text = annotated,
        style = font12.copy(fontSize = 10.sp, textAlign = TextAlign.Start),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
    )
}

@Composable
private fun BoxScope.ErrorOverlay(
    message: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                .hapticClickable { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                    ).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = font14,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "OK",
                style = font14,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .hapticClickable { onDismiss() }
                        .padding(8.dp),
            )
        }
    }
}

@MultiDevicePreview
@Composable
private fun UpgradeContentPreview() {
    PreviewWithNav {
        UpgradeContent(
            state =
                UpgradeState(
                    loadingMessage = null,
                    monthly =
                        PlanTile(
                            sku = "monthly_sku",
                            title = "Monthly",
                            price = "USD 9.00",
                            billedLabel = PlanBilledLabel(text = "Billed Monthly"),
                        ),
                    yearly =
                        PlanTile(
                            sku = "yearly_sku",
                            title = "Yearly",
                            price = "USD 69.00",
                            billedLabel = PlanBilledLabel(text = "$ 8.00/month, Billed Annually"),
                            discountLabel = "-33%",
                        ),
                    isPromo = false,
                    monthlySelected = true,
                    showRestore = true,
                ),
            actions = UpgradeActions(),
        )
    }
}
