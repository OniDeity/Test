package com.onideity.rommcompanion.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import com.onideity.rommcompanion.data.local.db.entity.RomEntity
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import com.onideity.rommcompanion.data.repository.LibraryRepository
import com.onideity.rommcompanion.download.DownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RomListUiState(
    val roms: List<RomEntity> = emptyList(),
    val downloadStates: Map<Long, DownloadStateEntity> = emptyMap(),
    val serverUrl: String? = null,
    val hasFolderConfigured: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class RomListViewModel(
    private val platformId: Long,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val romsWithDownloadStates = combine(
        libraryRepository.observeRoms(platformId),
        downloadRepository.observeAll().map { states -> states.associateBy { it.romId } },
    ) { roms, downloadStates -> roms to downloadStates }

    val uiState: StateFlow<RomListUiState> = combine(
        romsWithDownloadStates,
        settingsRepository.serverUrl,
        settingsRepository.romFolderUri(platformId),
        isRefreshing,
        errorMessage,
    ) { (roms, downloadStates), serverUrl, folderUri, refreshing, error ->
        RomListUiState(
            roms = roms,
            downloadStates = downloadStates,
            serverUrl = serverUrl,
            hasFolderConfigured = folderUri != null,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RomListUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            libraryRepository.refreshRoms(platformId).onFailure { errorMessage.value = it.message }
            isRefreshing.value = false
        }
    }

    fun install(rom: RomEntity) {
        viewModelScope.launch {
            downloadRepository.enqueueInstall(rom.id, rom.platformId, rom.primaryFileName)
        }
    }
}
