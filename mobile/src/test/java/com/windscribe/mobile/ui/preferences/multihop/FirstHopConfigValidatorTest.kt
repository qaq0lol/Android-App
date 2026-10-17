package com.windscribe.mobile.ui.preferences.multihop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FirstHopConfigValidatorTest {
    @Test
    fun validConfigReturnsPeerEndpointHost() {
        val endpoint = FirstHopConfigValidator.validate(validConfig)

        assertEquals("192.0.2.1", endpoint)
    }

    @Test
    fun missingPeerIsRejected() {
        val invalid = validConfig.substringBefore("[Peer]")

        assertThrows(IllegalArgumentException::class.java) {
            FirstHopConfigValidator.validate(invalid)
        }
    }

    @Test
    fun invalidWireGuardSyntaxIsRejected() {
        assertThrows(Exception::class.java) {
            FirstHopConfigValidator.validate("[Interface]\nPrivateKey = not-a-key\n[Peer]\nEndpoint = invalid")
        }
    }

    companion object {
        private val validConfig = """
            [Interface]
            Address = 192.0.2.2/32, 2001:db8:ffff:ffff:ffff:ffff:ffff:ffff/128
            DNS = 192.0.2.0
            PrivateKey = TFlmmEUC7V7VtiDYLKsbP5rySTKLIZq1yn8lMqK83wo=

            [Peer]
            AllowedIPs = 0.0.0.0/0, ::0/0
            Endpoint = 192.0.2.1:51820
            PersistentKeepalive = 0
            PublicKey = vBN7qyUTb5lJtWYJ8LhbPio1Z4RcyBPGnqFBGn6O6Qg=
        """.trimIndent()
    }
}
