package com.windscribe.mobile.ui.preferences.multihop

import PreferencesNavBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.windscribe.mobile.ui.common.PreferenceBackground
import com.windscribe.mobile.ui.nav.LocalNavController
import com.windscribe.mobile.ui.theme.font14
import com.windscribe.mobile.ui.theme.font16
import com.windscribe.mobile.ui.theme.primaryTextColor
import com.windscribe.vpn.R
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

@Composable
fun DoubleHopScreen(
    viewModel: DoubleHopViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val navController = LocalNavController.current

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                try {
                    val configText =
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            readBounded(stream, MAX_CONFIG_BYTES)
                        } ?: throw IllegalArgumentException("无法打开所选文件。")
                    viewModel.importConfig(configText)
                } catch (e: Exception) {
                    viewModel.reportImportError(e.message ?: "无法读取所选文件。")
                }
            }
        }

    PreferenceBackground {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            PreferencesNavBar(stringResource(R.string.double_hop)) {
                navController.popBackStack()
            }
            Spacer(Modifier.height(20.dp))

            Text(
                text = "第一跳：WireGuard",
                style = font16,
                color = MaterialTheme.colorScheme.primaryTextColor,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "导入 WireGuard .conf 配置作为第一跳。程序会校验配置，并使用 Android 加密存储保存私钥。",
                style = font14,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.hasProfile) "第一跳配置已保存" else "尚未导入第一跳配置",
                        style = font16,
                        color = MaterialTheme.colorScheme.primaryTextColor,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text =
                            state.endpoint?.let { "WireGuard 服务器：$it" }
                                ?: "请选择 WireGuard 配置文件。",
                        style = font14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "启用双重中转",
                                style = font16,
                                color = MaterialTheme.colorScheme.primaryTextColor,
                            )
                            Text(
                                text = if (state.enabled) "已启用，下一次连接生效" else "已关闭",
                                style = font14,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.enabled,
                            enabled = state.hasProfile,
                            onCheckedChange = viewModel::setEnabled,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("text/plain", "application/octet-stream", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.hasProfile) "替换配置" else "导入 WireGuard 配置")
                    }
                    if (state.hasProfile) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = viewModel::removeConfig,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("删除配置")
                        }
                    }
                }
            }

            state.notice?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = font14,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            state.error?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = font14,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "第二跳要求",
                style = font16,
                color = MaterialTheme.colorScheme.primaryTextColor,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "目前第二跳必须选择 Windscribe WireGuard。启用双重中转时，客户端会阻止 OpenVPN 或 IKEv2 连接，避免静默退化为单跳。第一跳 Peer 的 AllowedIPs 还必须包含所选 Windscribe WireGuard 服务器的地址。",
                style = font14,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun readBounded(
    input: InputStream,
    maximumBytes: Int,
): String {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(4096)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maximumBytes) { "配置文件不能超过 64 KiB。" }
        output.write(buffer, 0, count)
    }
    return output.toString(StandardCharsets.UTF_8.name())
}

private const val MAX_CONFIG_BYTES = 64 * 1024
