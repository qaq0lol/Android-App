package com.windscribe.mobile.ui.serverlist
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.ui.connection.BridgeApiViewModel
import com.windscribe.mobile.ui.connection.BridgeApiViewModelImpl
import com.windscribe.mobile.ui.connection.ConnectionViewmodel
import com.windscribe.mobile.ui.connection.ConnectionViewmodelImpl
import com.windscribe.mobile.ui.home.HomeViewmodel
import com.windscribe.mobile.ui.home.HomeViewmodelImpl
import com.windscribe.mobile.ui.theme.serverListBackgroundColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    viewModel: ServerViewModel = hiltViewModel<ServerViewModelImpl>(),
    connectionViewModel: ConnectionViewmodel = hiltViewModel<ConnectionViewmodelImpl>(),
    bridgeApiViewModel: BridgeApiViewModel = hiltViewModel<BridgeApiViewModelImpl>(),
    configViewmodel: ConfigViewmodel = hiltViewModel<ConfigViewmodelImpl>(),
    homeViewmodel: HomeViewmodel = hiltViewModel<HomeViewmodelImpl>(),
    modifier: Modifier = Modifier,
) {
    val selectedType by viewModel.selectedServerListType.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0.0f, pageCount = { 4 })
    val hapticFeedback by homeViewmodel.hapticFeedbackEnabled.collectAsState()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedType) {
        val targetPage = selectedType.toPageIndex()
        if (pagerState.currentPage != targetPage) {
            if (hapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            pagerState.animateScrollToPage(targetPage)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            viewModel.setSelectedType(page.toServerListType())
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.serverListBackgroundColor)
            .padding(top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        ServerListNavigation(
            modifier = Modifier.fillMaxWidth(),
            viewModel = viewModel,
            bridgeApiViewModel = bridgeApiViewModel,
            onTabSelected = { index -> coroutineScope.launch { viewModel.setSelectedType(index.toServerListType()) } },
        )
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 3,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { pageIndex ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                when (pageIndex) {
                    0 -> AllServerList(viewModel, connectionViewModel, homeViewmodel)
                    1 -> FavouriteList(viewModel, connectionViewModel, homeViewmodel)
                    2 -> StaticIPServerList(viewModel, connectionViewModel)
                    3 -> ConfigServerList(viewModel, connectionViewModel, configViewmodel)
                }
            }
        }
    }
}

private fun ServerListType.toPageIndex(): Int =
    when (this) {
        ServerListType.All -> 0
        ServerListType.Fav -> 1
        ServerListType.Static -> 2
        ServerListType.Config -> 3
    }

private fun Int.toServerListType(): ServerListType =
    when (this) {
        0 -> ServerListType.All
        1 -> ServerListType.Fav
        2 -> ServerListType.Static
        3 -> ServerListType.Config
        else -> ServerListType.All
    }
