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
import androidx.compose.foundation.layout.weight
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
                        } ?: throw IllegalArgumentException("The selected file could not be opened.")
                    viewModel.importConfig(configText)
                } catch (e: Exception) {
                    viewModel.reportImportError(e.message ?: "Could not read the selected file.")
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
                text = "WireGuard first hop",
                style = font16,
                color = MaterialTheme.colorScheme.primaryTextColor,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Import a WireGuard .conf file to save it as the planned upstream tunnel. The profile is validated with the existing WireGuard parser and stored using encrypted Android preferences.",
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
                        text = if (state.hasProfile) "First-hop profile saved" else "No first-hop profile",
                        style = font16,
                        color = MaterialTheme.colorScheme.primaryTextColor,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text =
                            state.endpoint?.let { "WireGuard endpoint: $it" }
                                ?: "Choose a WireGuard configuration file to begin.",
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
                                text = "Enable double-hop",
                                style = font16,
                                color = MaterialTheme.colorScheme.primaryTextColor,
                            )
                            Text(
                                text = if (state.enabled) "Enabled for the next connection" else "Disabled",
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
                        Text(if (state.hasProfile) "Replace configuration" else "Import WireGuard configuration")
                    }
                    if (state.hasProfile) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = viewModel::removeConfig,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Remove configuration")
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
                text = "Second hop requirements",
                style = font16,
                color = MaterialTheme.colorScheme.primaryTextColor,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Double-hop currently requires WireGuard as the Windscribe second-hop protocol. The client will refuse to start OpenVPN or IKEv2 while double-hop is enabled, so it cannot silently fall back to a single-hop connection. The first-hop peer's AllowedIPs must also include the selected Windscribe WireGuard server endpoint.",
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
        require(total <= maximumBytes) { "The selected file is larger than 64 KiB." }
        output.write(buffer, 0, count)
    }
    return output.toString(StandardCharsets.UTF_8.name())
}

private const val MAX_CONFIG_BYTES = 64 * 1024
