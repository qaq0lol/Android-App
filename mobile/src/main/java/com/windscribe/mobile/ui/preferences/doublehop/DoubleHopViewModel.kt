package com.windscribe.mobile.ui.preferences.doublehop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windscribe.mobile.ui.model.DropDownIntItem
import com.windscribe.vpn.apppreference.PreferencesHelper
import com.windscribe.vpn.localdatabase.LocalDbInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface DoubleHopViewModel {
    val isDoubleHopEnabled: StateFlow<Boolean>
    val cityOptions: StateFlow<List<DropDownIntItem>>
    val selectedFirstHopId: StateFlow<Int>
    val selectedSecondHopId: StateFlow<Int>

    fun refreshPreferences()
    fun onDoubleHopToggled(enabled: Boolean)
    fun onFirstHopSelected(cityId: Int)
    fun onSecondHopSelected(cityId: Int)
}

@HiltViewModel
class DoubleHopViewModelImpl @Inject constructor(
    private val preferencesHelper: PreferencesHelper,
    private val localDbInterface: LocalDbInterface,
) : ViewModel(), DoubleHopViewModel {

    override val isDoubleHopEnabled = MutableStateFlow(preferencesHelper.isDoubleHopEnabled)
    override val cityOptions = MutableStateFlow<List<DropDownIntItem>>(emptyList())
    override val selectedFirstHopId = MutableStateFlow(preferencesHelper.doubleHopFirstHopId)
    override val selectedSecondHopId = MutableStateFlow(preferencesHelper.doubleHopSecondHopId)

    init {
        loadCities()
    }

    override fun refreshPreferences() {
        isDoubleHopEnabled.value = preferencesHelper.isDoubleHopEnabled
        selectedFirstHopId.value = preferencesHelper.doubleHopFirstHopId
        selectedSecondHopId.value = preferencesHelper.doubleHopSecondHopId
    }

    private fun loadCities() {
        viewModelScope.launch {
            val datacenters = localDbInterface.getDatacentersAsync()
            val options = datacenters.map { dc ->
                val name = dc.nickName ?: dc.nodeName ?: "City #${dc.id}"
                DropDownIntItem(dc.id, name)
            }
            cityOptions.value = options

            if (preferencesHelper.doubleHopFirstHopId == -1 && options.isNotEmpty()) {
                val defaultFirst = options.first().key
                selectedFirstHopId.value = defaultFirst
                preferencesHelper.doubleHopFirstHopId = defaultFirst
            }
            if (preferencesHelper.doubleHopSecondHopId == -1 && options.size > 1) {
                val defaultSecond = options[1].key
                selectedSecondHopId.value = defaultSecond
                preferencesHelper.doubleHopSecondHopId = defaultSecond
            }
        }
    }

    override fun onDoubleHopToggled(enabled: Boolean) {
        isDoubleHopEnabled.value = enabled
        preferencesHelper.isDoubleHopEnabled = enabled
    }

    override fun onFirstHopSelected(cityId: Int) {
        selectedFirstHopId.value = cityId
        preferencesHelper.doubleHopFirstHopId = cityId
    }

    override fun onSecondHopSelected(cityId: Int) {
        selectedSecondHopId.value = cityId
        preferencesHelper.doubleHopSecondHopId = cityId
    }
}
