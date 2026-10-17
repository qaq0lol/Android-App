package com.windscribe.vpn.repository.multihop

import com.windscribe.vpn.backend.wireguard.WireGuardVpnProfile
import java.nio.charset.StandardCharsets

/**
 * Validates imported first-hop profiles with the same parser used by the VPN backend.
 * Syntax validation does not guarantee that a peer is reachable or that its routing policy
 * includes the selected Windscribe server.
 */
object FirstHopConfigValidator {
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
