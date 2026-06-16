package com.kawach.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kawach.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val shakeToSos: Boolean = true,
    val discreetMode: Boolean = false,
    val fakeCallerName: String = "Papa",
    val localPoliceNumber: String = "",
    val localCouncillorNumber: String = ""
)

class SettingsViewModel(private val dataStore: SettingsDataStore) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.shakeToSosEnabled,
        dataStore.discreetModeEnabled,
        dataStore.fakeCallerName,
        dataStore.localPoliceNumber,
        dataStore.localCouncillorNumber
    ) { values ->
        SettingsUiState(
            shakeToSos = values[0] as Boolean,
            discreetMode = values[1] as Boolean,
            fakeCallerName = values[2] as String,
            localPoliceNumber = values[3] as String,
            localCouncillorNumber = values[4] as String
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SettingsUiState())

    fun setShakeToSos(enabled: Boolean) {
        viewModelScope.launch { dataStore.setShakeToSos(enabled) }
    }

    fun setDiscreetMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDiscreetMode(enabled) }
    }

    fun setFakeCallerName(name: String) {
        viewModelScope.launch { dataStore.setFakeCallerName(name) }
    }

    fun setLocalPoliceNumber(number: String) {
        viewModelScope.launch { dataStore.setLocalPoliceNumber(number) }
    }

    fun setLocalCouncillorNumber(number: String) {
        viewModelScope.launch { dataStore.setLocalCouncillorNumber(number) }
    }

    class Factory(private val dataStore: SettingsDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(dataStore) as T
    }
}
