package com.windscribe.mobile.ui.preferences.multihop

import androidx.lifecycle.ViewModel
import com.windscribe.vpn.repository.multihop.FirstHopProfileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DoubleHopUiState(
    val endpoint: String? = null,
    val notice: String? = null,
    val error: String? = null,
    val enabled: Boolean = false,
) {
    val hasProfile: Boolean get() = !endpoint.isNullOrBlank()
}

@HiltViewModel
class DoubleHopViewModel
    @Inject
    constructor(
        private val profileStore: FirstHopProfileStore,
    ) : ViewModel() {
        private val _state = MutableStateFlow(DoubleHopUiState())
        val state: StateFlow<DoubleHopUiState> = _state.asStateFlow()

        init {
            refresh()
        }

        fun importConfig(configText: String) {
            try {
                val endpoint = profileStore.save(configText)
                _state.value =
                    DoubleHopUiState(
                        endpoint = endpoint,
                        enabled = profileStore.isEnabled(),
                        notice = "WireGuard first-hop profile imported and encrypted. Double-hop is enabled for the next connection.",
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        notice = null,
                        error = e.message ?: "Could not import this WireGuard profile.",
                    )
            }
        }

        fun removeConfig() {
            try {
                profileStore.clear()
                _state.value =
                    DoubleHopUiState(
                        notice = "Saved first-hop profile removed.",
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        notice = null,
                        error = e.message ?: "Could not remove the saved profile.",
                    )
            }
        }

        fun setEnabled(enabled: Boolean) {
            try {
                profileStore.setEnabled(enabled)
                _state.value = _state.value.copy(enabled = profileStore.isEnabled(), notice = null, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(notice = null, error = e.message ?: "Could not update double-hop.")
            }
        }

        fun reportImportError(message: String) {
            _state.value = _state.value.copy(notice = null, error = message)
        }

        fun clearMessages() {
            _state.value = _state.value.copy(notice = null, error = null)
        }

        private fun refresh() {
            try {
                _state.value = DoubleHopUiState(endpoint = profileStore.getEndpoint(), enabled = profileStore.isEnabled())
            } catch (e: Exception) {
                _state.value =
                    DoubleHopUiState(
                        error = e.message ?: "Encrypted storage is unavailable.",
                    )
            }
        }
    }
