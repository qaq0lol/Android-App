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
        val parsedConfig = WireGuardVpnProfile.createConfigFromString(configText)
        require(parsedConfig.getInterface().getAddresses().isNotEmpty()) {
            "The WireGuard Interface section must contain at least one Address."
        }
        return WireGuardVpnProfile.getHostName(configText)
            ?: throw IllegalArgumentException("The WireGuard peer must have a valid Endpoint.")
    }
}
