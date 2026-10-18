package com.windscribe.mobile.ui.preferences.doublehop

import PreferencesNavBar
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CustomDropDownInt
import com.windscribe.mobile.ui.common.PreferenceBackground
import com.windscribe.mobile.ui.common.SwitchItemView
import com.windscribe.mobile.ui.model.DropDownIntItem
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.theme.font14
import com.windscribe.mobile.ui.theme.preferencesSubtitleColor
import com.windscribe.mobile.ui.theme.primaryTextColor

data class DoubleHopState(
    val isEnabled: Boolean = false,
    val cityOptions: List<DropDownIntItem> = emptyList(),
    val selectedFirstHopId: Int = -1,
    val selectedSecondHopId: Int = -1,
)

class DoubleHopActions(
    val onDoubleHopToggled: (Boolean) -> Unit = {},
    val onFirstHopSelected: (Int) -> Unit = {},
    val onSecondHopSelected: (Int) -> Unit = {},
)

@Composable
fun DoubleHopScreen(
    viewModel: DoubleHopViewModel = hiltViewModel<DoubleHopViewModelImpl>()
) {
    val isEnabled by viewModel.isDoubleHopEnabled.collectAsState()
    val cityOptions by viewModel.cityOptions.collectAsState()
    val selectedFirstHopId by viewModel.selectedFirstHopId.collectAsState()
    val selectedSecondHopId by viewModel.selectedSecondHopId.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshPreferences()
    }

    DoubleHopContent(
        state = DoubleHopState(
            isEnabled = isEnabled,
            cityOptions = cityOptions,
            selectedFirstHopId = selectedFirstHopId,
            selectedSecondHopId = selectedSecondHopId,
        ),
        actions = DoubleHopActions(
            onDoubleHopToggled = viewModel::onDoubleHopToggled,
            onFirstHopSelected = viewModel::onFirstHopSelected,
            onSecondHopSelected = viewModel::onSecondHopSelected,
        ),
    )
}

@Composable
fun DoubleHopContent(
    state: DoubleHopState,
    actions: DoubleHopActions,
) {
    val navController = LocalNavController.current
    val scrollState = rememberScrollState()

    PreferenceBackground {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)) {
            PreferencesNavBar(stringResource(com.windscribe.vpn.R.string.double_hop_title)) {
                navController.popBackStack()
            }
            Spacer(modifier = Modifier.height(20.dp))
            ScreenDescription()
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding()
                    .verticalScroll(scrollState),
            ) {
                // Toggle Switch Section
                SwitchItemView(
                    title = com.windscribe.vpn.R.string.double_hop_title,
                    icon = R.drawable.ic_anti_censorship_icon,
                    description = com.windscribe.vpn.R.string.double_hop_wireguard_only,
                    enabled = state.isEnabled,
                    shape = RoundedCornerShape(12.dp),
                    onSelect = { actions.onDoubleHopToggled(!state.isEnabled) },
                )

                if (state.isEnabled && state.cityOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // First Hop Selector
                    CustomDropDownInt(
                        title = com.windscribe.vpn.R.string.first_hop_entry,
                        items = state.cityOptions,
                        selectedItemKey = state.selectedFirstHopId,
                        shape = RoundedCornerShape(12.dp),
                    ) { item ->
                        actions.onFirstHopSelected(item.key)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Second Hop Selector
                    CustomDropDownInt(
                        title = com.windscribe.vpn.R.string.second_hop_exit,
                        items = state.cityOptions,
                        selectedItemKey = state.selectedSecondHopId,
                        shape = RoundedCornerShape(12.dp),
                    ) { item ->
                        actions.onSecondHopSelected(item.key)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryTextColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
    ) {
        Text(
            text = stringResource(com.windscribe.vpn.R.string.double_hop_description),
            style = font14.copy(textAlign = TextAlign.Start),
            color = MaterialTheme.colorScheme.preferencesSubtitleColor,
        )
    }
}
