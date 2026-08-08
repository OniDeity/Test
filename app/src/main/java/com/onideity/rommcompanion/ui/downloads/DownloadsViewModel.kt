package com.onideity.rommcompanion.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import com.onideity.rommcompanion.download.DownloadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DownloadsViewModel(private val downloadRepository: DownloadRepository) : ViewModel() {

    val downloads: StateFlow<List<DownloadStateEntity>> = downloadRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cancel(romId: Long) = downloadRepository.cancelInstall(romId)
}
