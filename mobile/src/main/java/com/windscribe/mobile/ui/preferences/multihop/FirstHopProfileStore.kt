package com.windscribe.mobile.ui.preferences.multihop

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the optional user-supplied WireGuard first-hop configuration.
 *
 * The configuration contains a private key, so storage is encrypted and fails closed:
 * this class deliberately does not fall back to plaintext preferences if encryption fails.
 *
 * Importing a profile alone does not activate multi-hop routing. The VPN packet-routing
 * integration must be enabled separately before this profile can be used as an upstream.
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

            // Commit before reporting success, so a storage failure is visible to the user.
            check(
                preferences
                    .edit()
                    .putString(KEY_CONFIG, configText)
                    .putString(KEY_ENDPOINT, endpoint)
                    .commit(),
            ) {
                "Could not save the encrypted WireGuard configuration."
            }
            return endpoint
        }

        fun getConfig(): String? = preferences.getString(KEY_CONFIG, null)

        fun getEndpoint(): String? = preferences.getString(KEY_ENDPOINT, null)

        fun clear() {
            check(
                preferences
                    .edit()
                    .remove(KEY_CONFIG)
                    .remove(KEY_ENDPOINT)
                    .commit(),
            ) {
                "Could not remove the saved WireGuard configuration."
            }
        }

        companion object {
            private const val PREFERENCES_FILE = "double_hop_first_hop_secure"
            private const val KEY_CONFIG = "first_hop_wireguard_config"
            private const val KEY_ENDPOINT = "first_hop_wireguard_endpoint"
        }
    }
