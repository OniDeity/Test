package com.onideity.rommcompanion.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthInitResponse
import com.onideity.rommcompanion.data.repository.AuthRepository
import com.onideity.rommcompanion.data.repository.PairingPollResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PairingUiState(
    val serverUrlInput: String = "",
    val isStartingPairing: Boolean = false,
    val pairing: DeviceAuthInitResponse? = null,
    val isApproved: Boolean = false,
    val errorMessage: String? = null,
)

class PairingViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState

    private var pollJob: Job? = null

    init {
        viewModelScope.launch {
            val savedUrl = settingsRepository.serverUrl.first()
            if (savedUrl != null) {
                _uiState.update { it.copy(serverUrlInput = savedUrl) }
            }
        }
    }

    fun onServerUrlChanged(value: String) {
        _uiState.update { it.copy(serverUrlInput = value, errorMessage = null) }
    }

    fun beginPairing() {
        val url = _uiState.value.serverUrlInput.trim()
        if (url.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your RomM server address") }
            return
        }

        pollJob?.cancel()
        _uiState.update { it.copy(isStartingPairing = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                authRepository.configureServer(url)
                val pairing = authRepository.startPairing()
                _uiState.update { it.copy(isStartingPairing = false, pairing = pairing) }
                startPolling(pairing)
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(isStartingPairing = false, errorMessage = t.message ?: "Could not reach server")
                }
            }
        }
    }

    private fun startPolling(pairing: DeviceAuthInitResponse) {
        pollJob = viewModelScope.launch {
            var intervalSeconds = pairing.interval.coerceAtLeast(2)
            while (true) {
                delay(intervalSeconds * 1000L)
                when (val result = authRepository.pollOnce(pairing.deviceCode)) {
                    is PairingPollResult.Pending -> Unit
                    is PairingPollResult.SlowDown -> intervalSeconds += 2
                    is PairingPollResult.Approved -> {
                        _uiState.update { it.copy(isApproved = true) }
                        return@launch
                    }
                    is PairingPollResult.Denied -> {
                        _uiState.update { it.copy(pairing = null, errorMessage = "Pairing was denied") }
                        return@launch
                    }
                    is PairingPollResult.Expired -> {
                        _uiState.update { it.copy(pairing = null, errorMessage = "Pairing code expired, try again") }
                        return@launch
                    }
                    is PairingPollResult.UnknownError -> {
                        _uiState.update { it.copy(pairing = null, errorMessage = result.message) }
                        return@launch
                    }
                }
            }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
    }
}
