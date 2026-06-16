package com.kawach.app.ui.home

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kawach.app.data.local.entity.EmergencyContact
import com.kawach.app.data.repository.ContactRepository
import com.kawach.app.data.repository.IncidentRepository
import com.kawach.app.service.WalkingTimerService
import com.kawach.app.util.LocationHelper
import com.kawach.app.util.ShakeDetector
import com.kawach.app.util.SosManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val sosActive: Boolean = false,
    val shakeEnabled: Boolean = true,
    val personalContactCount: Int = 0,
    val walkingTimerRunning: Boolean = false,
    val walkingTimerRemaining: Long = 0L,
    val locationShareLoading: Boolean = false,
    val sharedLocationLink: String? = null,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val contactRepository: ContactRepository,
    private val incidentRepository: IncidentRepository,
    private val sosManager: SosManager,
    private val settingsShakeFlow: Flow<Boolean>
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val personalContacts: StateFlow<List<EmergencyContact>> =
        contactRepository.personalContacts
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var shakeDetector: ShakeDetector? = null

    init {
        // Sync shake toggle from settings
        viewModelScope.launch {
            settingsShakeFlow.collect { enabled ->
                _uiState.update { it.copy(shakeEnabled = enabled) }
            }
        }
        // Observe personal contact count
        viewModelScope.launch {
            contactRepository.personalContactCount.collect { count ->
                _uiState.update { it.copy(personalContactCount = count) }
            }
        }
        // Observe walking timer state from service
        viewModelScope.launch {
            WalkingTimerService.isRunning.collect { running ->
                _uiState.update { it.copy(walkingTimerRunning = running) }
            }
        }
        viewModelScope.launch {
            WalkingTimerService.remainingSeconds.collect { remaining ->
                _uiState.update { it.copy(walkingTimerRemaining = remaining) }
            }
        }
    }

    fun buildShakeDetector(onShake: () -> Unit): ShakeDetector {
        return ShakeDetector {
            if (_uiState.value.shakeEnabled && !_uiState.value.sosActive) {
                onShake()
            }
        }.also { shakeDetector = it }
    }

    fun activateSos(context: Context) {
        if (sosManager.sosRunning) return
        _uiState.update { it.copy(sosActive = true) }
        viewModelScope.launch {
            sosManager.activate(personalContacts.value, viewModelScope)
        }
    }

    fun deactivateSos() {
        sosManager.deactivate()
        _uiState.update { it.copy(sosActive = false) }
    }

    fun shareLocation(context: Context) {
        _uiState.update { it.copy(locationShareLoading = true, sharedLocationLink = null) }
        viewModelScope.launch {
            try {
                val location = LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    val link = LocationHelper.toMapsLink(location.latitude, location.longitude)
                    _uiState.update { it.copy(locationShareLoading = false, sharedLocationLink = link) }
                } else {
                    _uiState.update {
                        it.copy(locationShareLoading = false, errorMessage = "Location unavailable")
                    }
                }
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(locationShareLoading = false, errorMessage = "Location permission required")
                }
            }
        }
    }

    fun clearSharedLocation() {
        _uiState.update { it.copy(sharedLocationLink = null) }
    }

    fun startWalkingTimer(context: Context, durationMinutes: Int) {
        val intent = Intent(context, WalkingTimerService::class.java).apply {
            action = WalkingTimerService.ACTION_START
            putExtra(WalkingTimerService.EXTRA_DURATION_SECONDS, durationMinutes * 60L)
        }
        context.startForegroundService(intent)
    }

    fun cancelWalkingTimer(context: Context) {
        val intent = Intent(context, WalkingTimerService::class.java).apply {
            action = WalkingTimerService.ACTION_CANCEL
        }
        context.startService(intent)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val contactRepository: ContactRepository,
        private val incidentRepository: IncidentRepository,
        private val sosManager: SosManager,
        private val shakeFlow: Flow<Boolean>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(contactRepository, incidentRepository, sosManager, shakeFlow) as T
    }
}
