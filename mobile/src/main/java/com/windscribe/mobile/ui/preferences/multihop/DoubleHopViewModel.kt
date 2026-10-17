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
                        notice = "第一跳配置已加密保存，双重中转已启用。",
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        notice = null,
                        error = e.message ?: "无法导入此 WireGuard 配置。",
                    )
            }
        }

        fun removeConfig() {
            try {
                profileStore.clear()
                _state.value =
                    DoubleHopUiState(
                        notice = "已删除第一跳配置。",
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        notice = null,
                        error = e.message ?: "无法删除已保存的配置。",
                    )
            }
        }

        fun setEnabled(enabled: Boolean) {
            try {
                profileStore.setEnabled(enabled)
                _state.value = _state.value.copy(enabled = profileStore.isEnabled(), notice = null, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(notice = null, error = e.message ?: "无法更新双重中转设置。")
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
                        error = e.message ?: "加密存储不可用。",
                    )
            }
        }
    }
