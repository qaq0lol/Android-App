package com.windscribe.vpn.repository.multihop

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted storage for an imported first-hop WireGuard profile.
 *
 * Configurations contain private keys. If Android encrypted storage cannot be initialized,
 * operations fail closed and never fall back to plaintext preferences.
 */
@Singleton
class FirstHopProfileStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val appContext = context.applicationContext

        private val preferences: SharedPreferences by lazy {
            val masterKey =
                MasterKey
                    .Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                appContext,
                PREFERENCES_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        fun save(configText: String): String {
            val endpoint = FirstHopConfigValidator.validate(configText)
            check(
                preferences.edit()
                    .putString(KEY_CONFIG, configText)
                    .putString(KEY_ENDPOINT, endpoint)
                    .putBoolean(KEY_ENABLED, true)
                    .commit(),
            ) {
                "Could not save the encrypted WireGuard configuration."
            }
            return endpoint
        }

        fun getConfig(): String? = preferences.getString(KEY_CONFIG, null)

        fun getEndpoint(): String? = preferences.getString(KEY_ENDPOINT, null)

        fun isEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, false) && getConfig() != null

        fun setEnabled(enabled: Boolean) {
            require(!enabled || getConfig() != null) {
                "Import a WireGuard configuration before enabling double-hop."
            }
            check(preferences.edit().putBoolean(KEY_ENABLED, enabled).commit()) {
                "Could not save the double-hop setting."
            }
        }

        fun clear() {
            check(
                preferences.edit()
                    .remove(KEY_CONFIG)
                    .remove(KEY_ENDPOINT)
                    .putBoolean(KEY_ENABLED, false)
                    .commit(),
            ) {
                "Could not remove the saved WireGuard configuration."
            }
        }

        companion object {
            private const val PREFERENCES_FILE = "double_hop_first_hop_secure"
            private const val KEY_CONFIG = "first_hop_wireguard_config"
            private const val KEY_ENDPOINT = "first_hop_wireguard_endpoint"
            private const val KEY_ENABLED = "double_hop_enabled"
        }
    }
