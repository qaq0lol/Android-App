package com.windscribe.mobile.ui.preferences.multihop

import com.windscribe.vpn.backend.wireguard.WireGuardVpnProfile
import java.nio.charset.StandardCharsets

/**
 * Validates an imported first-hop profile before it is written to encrypted storage.
 * This validates the WireGuard syntax only; it does not prove the endpoint is reachable.
 */
internal object FirstHopConfigValidator {
    private const val MAX_CONFIG_BYTES = 64 * 1024

    fun validate(configText: String): String {
        require(configText.toByteArray(StandardCharsets.UTF_8).size <= MAX_CONFIG_BYTES) {
            "WireGuard configuration is larger than 64 KiB."
        }
        require(configText.contains("[Interface]", ignoreCase = true)) {
            "The selected file does not contain a WireGuard [Interface] section."
        }
        require(configText.contains("[Peer]", ignoreCase = true)) {
            "The selected file does not contain a WireGuard [Peer] section."
        }

        WireGuardVpnProfile.createConfigFromString(configText)
        return WireGuardVpnProfile.getHostName(configText)
            ?: throw IllegalArgumentException("The WireGuard peer must have a valid Endpoint.")
    }
}
