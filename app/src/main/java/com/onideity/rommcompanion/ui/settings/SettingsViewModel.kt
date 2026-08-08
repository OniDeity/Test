package com.onideity.rommcompanion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import com.onideity.rommcompanion.data.remote.dto.DeviceDto
import com.onideity.rommcompanion.data.repository.AuthRepository
import com.onideity.rommcompanion.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val wifiOnlyDownloads: Boolean = true,
    val autoExtractArchives: Boolean = true,
    val thisDeviceId: String? = null,
    val devices: List<DeviceDto> = emptyList(),
    val isLoadingDevices: Boolean = false,
    val errorMessage: String? = null,
    val signedOut: Boolean = false,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val devices = MutableStateFlow<List<DeviceDto>>(emptyList())
    private val isLoadingDevices = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val signedOut = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.wifiOnlyDownloads,
        settingsRepository.autoExtractArchives,
        settingsRepository.pairedDeviceId,
        devices,
        combine(isLoadingDevices, errorMessage, signedOut) { loading, error, out -> Triple(loading, error, out) },
    ) { wifiOnly, autoExtract, thisDeviceId, deviceList, (loading, error, out) ->
        SettingsUiState(wifiOnly, autoExtract, thisDeviceId, deviceList, loading, error, out)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        loadDevices()
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWifiOnlyDownloads(enabled) }
    }

    fun setAutoExtractArchives(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoExtractArchives(enabled) }
    }

    fun loadDevices() {
        viewModelScope.launch {
            isLoadingDevices.value = true
            runCatching { deviceRepository.listDevices() }
                .onSuccess { devices.value = it }
                .onFailure { errorMessage.value = it.message }
            isLoadingDevices.value = false
        }
    }

    fun revokeDevice(deviceId: String) {
        viewModelScope.launch {
            runCatching { deviceRepository.revokeDevice(deviceId) }
                .onSuccess {
                    if (deviceId == uiState.value.thisDeviceId) {
                        authRepository.signOut()
                        signedOut.value = true
                    } else {
                        loadDevices()
                    }
                }
                .onFailure { errorMessage.value = it.message }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            signedOut.value = true
        }
    }
}
