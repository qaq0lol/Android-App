package com.windscribe.mobile.ui.preferences.advance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.ui.common.CyberBackground
import com.windscribe.mobile.ui.common.CyberBadge
import com.windscribe.mobile.ui.common.CyberButton
import com.windscribe.mobile.ui.common.CyberTopAppBar
import com.windscribe.mobile.ui.common.GlassSurface
import com.windscribe.mobile.ui.common.NeonCyan
import com.windscribe.mobile.ui.common.NeonGreen
import com.windscribe.mobile.ui.common.TextMuted
import com.windscribe.mobile.ui.common.TextPrimary
import com.windscribe.mobile.ui.common.TextSecondary
import com.windscribe.mobile.ui.helper.MultiDevicePreview
import com.windscribe.mobile.ui.helper.PreviewWithNav
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.vpn.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Callbacks the advance-parameters UI can raise. Hoisted out of the composables so the stateless
 * [AdvanceContent] never needs to know about [AdvanceViewModel] - previews supply no-op lambdas.
 */
class AdvanceActions(
    val onParamsChange: (String) -> Unit = {},
    val onSaveClick: (String) -> Unit = {},
    val onClearClick: () -> Unit = {},
)

/**
 * Stateful entry point. Owns the [AdvanceViewModel], collects its flows and wires the toast
 * side effect, then delegates rendering to [AdvanceContent].
 */
@Composable
fun AdvanceScreen(viewModel: AdvanceViewModel = hiltViewModel<AdvanceViewModelImpl>()) {
    val params by viewModel.parameters.collectAsState()
    AdvanceContent(
        params = params,
        toastMessage = viewModel.toastMessage,
        actions =
            AdvanceActions(
                onParamsChange = viewModel::updateParams,
                onSaveClick = viewModel::saveAdvanceParams,
                onClearClick = viewModel::clearAdvanceParams,
            ),
    )
}

/**
 * Stateless advance-parameters UI. Everything it needs is passed in, so it renders identically
 * in the app and in `@Preview`. This is the composable previews target.
 */
@Composable
fun AdvanceContent(
    params: String,
    toastMessage: Flow<String>,
    actions: AdvanceActions,
) {
    val navController = LocalNavController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CyberTopAppBar(
                title = "高级参数",
                subtitle = "CUSTOM ENGINE PARAMETERS & OVERRIDES",
                onBack = { navController.popBackStack() },
                trailingContent = {
                    CyberBadge(
                        text = if (params.isBlank()) "未配置" else "已生效",
                        color = if (params.isBlank()) TextMuted else NeonGreen
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Info Card
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "底盘引擎指令注入",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "在此输入的每行指令将直接追加至 OpenVPN 或 WireGuard 的守护进程配置文件中。请确保语法准确，错误参数可能导致 VPN 拨号失败。",
                            color = TextMuted,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Terminal Code Editor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xF0080D17))
                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = params,
                        onValueChange = actions.onParamsChange,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = {
                            Text(
                                text = "# 每行输入一组键值配置，例如:\n# ping-restart=60\n# mtu-disc=yes",
                                color = TextMuted.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = NeonCyan,
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CyberButton(
                        text = "清除所有参数",
                        onClick = {
                            focusManager.clearFocus()
                            actions.onClearClick()
                        },
                        modifier = Modifier.weight(1f),
                        color = TextMuted
                    )
                    CyberButton(
                        text = "保存并应用",
                        onClick = {
                            focusManager.clearFocus()
                            actions.onSaveClick(params)
                        },
                        modifier = Modifier.weight(1f),
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        HandleToast(toastMessage)
    }
}

@Composable
private fun HandleToast(toastMessage: Flow<String>) {
    val context = LocalContext.current
    val message by toastMessage.collectAsState(initial = "")
    LaunchedEffect(message) {
        if (message.isBlank()) {
            return@LaunchedEffect
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Feeds representative parameter text into the preview so the renderer draws [AdvanceContent]
 * without a VM.
 */
private class AdvanceStateProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("", "key=value")
}

@Composable
@MultiDevicePreview
private fun AdvanceContentPreview(
    @PreviewParameter(AdvanceStateProvider::class) params: String,
) {
    PreviewWithNav {
        AdvanceContent(
            params = params,
            toastMessage = emptyFlow(),
            actions = AdvanceActions(),
        )
    }
}
