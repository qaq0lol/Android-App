package com.windscribe.mobile.ui.preferences.excluded_ips

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberSectionHeader
import com.windscribe.mobile.ui.common.CyberTextField
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.NeonPurple
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.helper.MultiDevicePreview
import com.windscribe.mobile.ui.helper.PreviewWithNav
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.vpn.R
import com.windscribe.vpn.localdatabase.tables.ExcludedIpDomain

data class ExcludedIpDomainState(
    val excludedList: List<ExcludedIpDomain> = emptyList(),
    val inputText: String = "",
    val isRefreshing: Boolean = false,
)

data class ExcludedIpDomainActions(
    val onInputTextChange: (String) -> Unit = {},
    val onAddEntry: () -> Unit = {},
    val onDeleteEntry: (ExcludedIpDomain) -> Unit = {},
    val onDeleteAll: () -> Unit = {},
    val onImportFromFile: () -> Unit = {},
    val onRefreshHostnames: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcludedIpDomainScreen(viewModel: ExcludedIpDomainViewModel = hiltViewModel<ExcludedIpDomainViewModelImpl>()) {
    val context = LocalContext.current
    val excludedList by viewModel.excludedList.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let { viewModel.onImportFromFile(it) }
        }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToastMessage()
        }
    }

    ExcludedIpDomainContent(
        state =
            ExcludedIpDomainState(
                excludedList = excludedList,
                inputText = inputText,
                isRefreshing = isRefreshing,
            ),
        actions =
            ExcludedIpDomainActions(
                onInputTextChange = viewModel::onInputTextChange,
                onAddEntry = viewModel::onAddEntry,
                onDeleteEntry = viewModel::onDeleteEntry,
                onDeleteAll = viewModel::onDeleteAll,
                onImportFromFile = { filePickerLauncher.launch("text/*") },
                onRefreshHostnames = viewModel::onRefreshHostnames,
            ),
    )

    // Show validation/import error dialog
    dialogMessage?.let { message ->
        ValidationErrorDialog(
            message = message,
            onDismiss = { viewModel.clearDialogMessage() },
        )
    }
}

@Composable
private fun ValidationErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    val isImportResult = message.startsWith("Import Results:")
    val title = if (isImportResult) "规则导入结果" else "CIDR / 域名格式错误"

    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 24.dp,
            borderColor = if (isImportResult) NeonCyan.copy(alpha = 0.5f) else Color(0xFFFF3366).copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = if (isImportResult) NeonCyan else Color(0xFFFF4D4D),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                CyberButton(
                    text = "确定",
                    onClick = onDismiss,
                    color = NeonCyan,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Stateless excluded IP/domain UI. Everything it needs is passed in, so it renders identically in the
 * app and in `@Preview`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcludedIpDomainContent(
    state: ExcludedIpDomainState,
    actions: ExcludedIpDomainActions,
) {
    val navController = LocalNavController.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = stringResource(R.string.domains_ips),
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Info description banner
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = stringResource(R.string.domains_ips_description),
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Section Card
                CyberSectionHeader(title = "新增排除规则")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CyberTextField(
                            value = state.inputText,
                            onValueChange = actions.onInputTextChange,
                            placeholder = stringResource(R.string.enter_ip_domain_hint)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            CyberButton(
                                text = "添加规则",
                                onClick = actions.onAddEntry,
                                color = NeonCyan,
                                enabled = state.inputText.isNotBlank()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Import & Delete All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CyberButton(
                        text = "从文件导入规则",
                        onClick = actions.onImportFromFile,
                        color = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                    CyberButton(
                        text = "清空全部规则",
                        onClick = { showDeleteAllDialog = true },
                        color = if (state.excludedList.isNotEmpty()) Color(0xFFFF4D4D) else TextMuted,
                        enabled = state.excludedList.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List Section
                CyberSectionHeader(title = "已排除列表 (${state.excludedList.size})")

                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = actions.onRefreshHostnames,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.excludedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = stringResource(R.string.no_excluded_ips_domains),
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(state.excludedList) { _, entry ->
                                GlassSurface(
                                    modifier = Modifier.fillMaxWidth(),
                                    cornerRadius = 14.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                            Text(
                                                text = entry.value,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            CyberBadge(
                                                text = entry.type.name,
                                                color = when (entry.type) {
                                                    ExcludedIpDomain.EntryType.HOSTNAME -> NeonCyan
                                                    ExcludedIpDomain.EntryType.IP -> NeonGreen
                                                    ExcludedIpDomain.EntryType.IP_RANGE -> NeonPurple
                                                }
                                            )
                                        }

                                        Icon(
                                            painter = painterResource(android.R.drawable.ic_menu_delete),
                                            contentDescription = "Delete",
                                            tint = Color(0xFFFF4D4D),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { actions.onDeleteEntry(entry) }
                                                )
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteAllDialog) {
        DeleteAllConfirmDialog(
            onConfirm = {
                actions.onDeleteAll()
                showDeleteAllDialog = false
            },
            onDismiss = { showDeleteAllDialog = false },
        )
    }
}

@Composable
private fun DeleteAllConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 24.dp,
            borderColor = Color(0xFFFF3366).copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.delete_all_dialog_title),
                    color = Color(0xFFFF4D4D),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.delete_all_dialog_message),
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
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    CyberButton(
                        text = stringResource(R.string.delete_button),
                        onClick = onConfirm,
                        color = Color(0xFFFF4D4D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


private class ExcludedIpDomainStateProvider : PreviewParameterProvider<ExcludedIpDomainState> {
    override val values: Sequence<ExcludedIpDomainState>
        get() =
            sequenceOf(
                // Empty state
                ExcludedIpDomainState(
                    excludedList = emptyList(),
                    inputText = "",
                    isRefreshing = false,
                ),
                // State with items
                ExcludedIpDomainState(
                    excludedList =
                        listOf(
                            ExcludedIpDomain(id = 1, value = "example.com", type = ExcludedIpDomain.EntryType.HOSTNAME),
                            ExcludedIpDomain(id = 2, value = "192.168.1.1", type = ExcludedIpDomain.EntryType.IP),
                            ExcludedIpDomain(id = 3, value = "10.0.0.0/24", type = ExcludedIpDomain.EntryType.IP_RANGE),
                            ExcludedIpDomain(id = 4, value = "google.com", type = ExcludedIpDomain.EntryType.HOSTNAME),
                            ExcludedIpDomain(id = 5, value = "netflix.com", type = ExcludedIpDomain.EntryType.HOSTNAME),
                        ),
                    inputText = "test.com",
                    isRefreshing = false,
                ),
            )
}

@Composable
@MultiDevicePreview
private fun ExcludedIpDomainContentPreview(
    @PreviewParameter(ExcludedIpDomainStateProvider::class) state: ExcludedIpDomainState,
) {
    PreviewWithNav {
        ExcludedIpDomainContent(
            state = state,
            actions = ExcludedIpDomainActions(),
        )
    }
}
