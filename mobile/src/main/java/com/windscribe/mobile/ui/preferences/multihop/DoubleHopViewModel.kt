package com.windscribe.mobile.ui.preferences.multihop

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DoubleHopUiState(
    val endpoint: String? = null,
    val notice: String? = null,
    val error: String? = null,
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
                        notice = "WireGuard first-hop profile imported and encrypted.",
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

        fun reportImportError(message: String) {
            _state.value = _state.value.copy(notice = null, error = message)
        }

        fun clearMessages() {
            _state.value = _state.value.copy(notice = null, error = null)
        }

        private fun refresh() {
            try {
                _state.value = DoubleHopUiState(endpoint = profileStore.getEndpoint())
            } catch (e: Exception) {
                _state.value =
                    DoubleHopUiState(
                        error = e.message ?: "Encrypted storage is unavailable.",
                    )
            }
        }
    }
