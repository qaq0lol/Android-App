package com.windscribe.mobile.ui.preferences.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.R
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberDropdown
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
import com.windscribe.mobile.ui.common.openUrl
import com.windscribe.mobile.ui.helper.MultiDevicePreview
import com.windscribe.mobile.ui.helper.PreviewWithNav
import com.windscribe.mobile.ui.model.DropDownStringItem
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.popup.FullScreenDialog
import com.windscribe.vpn.api.response.QueryType

class TicketActions(
    val onQueryTypeSelected: (QueryType) -> Unit = {},
    val onEmailChanged: (String) -> Unit = {},
    val onSubjectChanged: (String) -> Unit = {},
    val onMessageChanged: (String) -> Unit = {},
    val onSendTicketClicked: () -> Unit = {},
    val onGarryOpened: () -> Unit = {},
)

@Composable
fun TicketScreen(viewModel: TicketViewModel = hiltViewModel<TicketViewModelImpl>()) {
    val submitTicketState by viewModel.submitTicketState.collectAsState()
    val isButtonEnabled by viewModel.buttonEnabled.collectAsState()
    val email by viewModel.email.collectAsState()
    TicketContent(
        submitTicketState = submitTicketState,
        isButtonEnabled = isButtonEnabled,
        email = email,
        actions =
            TicketActions(
                onQueryTypeSelected = viewModel::onQueryTypeSelected,
                onEmailChanged = viewModel::onEmailChanged,
                onSubjectChanged = viewModel::onSubjectChanged,
                onMessageChanged = viewModel::onMessageChanged,
                onSendTicketClicked = viewModel::onSendTicketClicked,
                onGarryOpened = viewModel::onGarryOpened,
            ),
    )
}

@Composable
fun TicketContent(
    submitTicketState: SubmitTicketState,
    isButtonEnabled: Boolean,
    email: String,
    actions: TicketActions,
) {
    val navController = LocalNavController.current
    var selectedQueryType by remember { mutableStateOf(QueryType.Account) }
    var emailInput by remember(email) { mutableStateOf(email) }
    var subjectInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = stringResource(com.windscribe.vpn.R.string.contact_humans),
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .imePadding()
            ) {
                // Info header card
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    borderColor = Color(0x14FFFFFF)
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
                                painter = painterResource(id = R.drawable.ic_ticket),
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "人工技术支持工单",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(com.windscribe.vpn.R.string.how_to_send_ticket),
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Query Category Selection
                CyberSectionHeader(title = "咨询类型")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "工单分类",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "选择最符合您问题的类别",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        CyberDropdown(
                            label = "Category",
                            selectedItem = selectedQueryType.name,
                            items = QueryType.entries.map { it.name },
                            onItemSelected = { selectedName ->
                                val found = QueryType.entries.firstOrNull { it.name == selectedName } ?: QueryType.Account
                                selectedQueryType = found
                                actions.onQueryTypeSelected(found)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ticket Details Form
                CyberSectionHeader(title = "工单详情")
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    borderColor = Color(0x14FFFFFF)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(com.windscribe.vpn.R.string.email),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CyberTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    actions.onEmailChanged(it)
                                },
                                placeholder = "your-email@example.com"
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(com.windscribe.vpn.R.string.subject),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CyberTextField(
                                value = subjectInput,
                                onValueChange = {
                                    subjectInput = it
                                    actions.onSubjectChanged(it)
                                },
                                placeholder = "简短描述遇到的问题"
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(com.windscribe.vpn.R.string.what_is_the_issue),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            CyberTextField(
                                value = messageInput,
                                onValueChange = {
                                    messageInput = it
                                    actions.onMessageChanged(it)
                                },
                                placeholder = "请详细描述您遇到的问题现象、网络环境或错误信息...",
                                singleLine = false,
                                modifier = Modifier.height(110.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Action
                CyberButton(
                    text = if (submitTicketState is SubmitTicketState.Loading) "正在加密提交工单..." else stringResource(com.windscribe.vpn.R.string.send),
                    onClick = actions.onSendTicketClicked,
                    enabled = isButtonEnabled && submitTicketState !is SubmitTicketState.Loading,
                    color = NeonCyan,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        HandleState(submitTicketState, actions.onGarryOpened)
    }
}

@Composable
private fun HandleState(
    submitTicketState: SubmitTicketState,
    onGarryOpened: () -> Unit,
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val message =
        when (submitTicketState) {
            is SubmitTicketState.Error -> {
                submitTicketState.message
            }

            else -> {
                ""
            }
        }
    LaunchedEffect(submitTicketState) {
        when (submitTicketState) {
            is SubmitTicketState.OpenGarry -> {
                context.openUrl(submitTicketState.url)
                onGarryOpened()
            }

            is SubmitTicketState.Error -> {
                showDialog.value = true
            }

            else -> Unit
        }
    }
    if (showDialog.value) {
        FullScreenDialog(message, error = submitTicketState is SubmitTicketState.Error) {
            showDialog.value = false
        }
    }
}


private class TicketStateProvider : PreviewParameterProvider<SubmitTicketState> {
    override val values =
        sequenceOf(
            SubmitTicketState.Idle,
            SubmitTicketState.Loading,
            SubmitTicketState.Error("Failed to submit ticket."),
        )
}

@Composable
@MultiDevicePreview
private fun TicketContentPreview(
    @PreviewParameter(TicketStateProvider::class) submitTicketState: SubmitTicketState,
) {
    PreviewWithNav {
        TicketContent(
            submitTicketState = submitTicketState,
            isButtonEnabled = submitTicketState is SubmitTicketState.Idle,
            email = "",
            actions = TicketActions(),
        )
    }
}
