package com.onideity.rommcompanion.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onideity.rommcompanion.data.local.db.entity.PlatformEntity
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import com.onideity.rommcompanion.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlatformListUiState(
    val platforms: List<PlatformEntity> = emptyList(),
    val serverUrl: String? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class PlatformListViewModel(
    private val libraryRepository: LibraryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PlatformListUiState> = combine(
        libraryRepository.observePlatforms(),
        settingsRepository.serverUrl,
        isRefreshing,
        errorMessage,
    ) { platforms, serverUrl, refreshing, error ->
        PlatformListUiState(platforms, serverUrl, refreshing, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlatformListUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            libraryRepository.refreshPlatforms().onFailure { errorMessage.value = it.message }
            isRefreshing.value = false
        }
    }
}
