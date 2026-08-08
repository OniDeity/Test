package com.onideity.rommcompanion.ui.platform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlatformFolderUiState(
    val romFolderUri: String? = null,
    val biosFolderUri: String? = null,
)

class PlatformFolderSettingsViewModel(
    private val platformId: Long,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<PlatformFolderUiState> = combine(
        settingsRepository.romFolderUri(platformId),
        settingsRepository.biosFolderUri(platformId),
    ) { romFolder, biosFolder ->
        PlatformFolderUiState(romFolder, biosFolder)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlatformFolderUiState())

    fun setRomFolderUri(uri: String) {
        viewModelScope.launch { settingsRepository.setRomFolderUri(platformId, uri) }
    }

    fun setBiosFolderUri(uri: String) {
        viewModelScope.launch { settingsRepository.setBiosFolderUri(platformId, uri) }
    }
}
