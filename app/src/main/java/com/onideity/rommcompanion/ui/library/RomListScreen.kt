package com.onideity.rommcompanion.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.onideity.rommcompanion.data.local.db.entity.DownloadStatus
import com.onideity.rommcompanion.data.local.db.entity.RomEntity
import com.onideity.rommcompanion.ui.common.resolveMediaUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomListScreen(
    viewModel: RomListViewModel,
    platformName: String,
    onBack: () -> Unit,
    onOpenFolderSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(platformName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenFolderSettings) {
                        Icon(Icons.Default.Folder, contentDescription = "Download folder")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!state.hasFolderConfigured) {
                Text(
                    text = "No download folder set for $platformName yet — tap the folder icon above to choose one.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isRefreshing && state.roms.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.roms.isEmpty() -> {
                        Text(
                            text = state.errorMessage ?: "No roms found for this platform yet.",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                        )
                    }
                    else -> {
                        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(state.roms, key = { it.id }) { rom ->
                                RomRow(
                                    rom = rom,
                                    serverUrl = state.serverUrl,
                                    status = state.downloadStates[rom.id]?.status,
                                    progressPercent = state.downloadStates[rom.id]?.progressPercent ?: 0,
                                    canInstall = state.hasFolderConfigured,
                                    onInstall = { viewModel.install(rom) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RomRow(
    rom: RomEntity,
    serverUrl: String?,
    status: DownloadStatus?,
    progressPercent: Int,
    canInstall: Boolean,
    onInstall: () -> Unit,
) {
    Column {
        ListItem(
            headlineContent = { Text(rom.displayTitle) },
            supportingContent = { Text(formatBytes(rom.fsSizeBytes)) },
            leadingContent = {
                AsyncImage(
                    model = resolveMediaUrl(serverUrl, rom.pathCoverSmall),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            },
            trailingContent = {
                when (status) {
                    DownloadStatus.INSTALLED -> Text("Installed")
                    DownloadStatus.DOWNLOADING, DownloadStatus.EXTRACTING -> Text("$progressPercent%")
                    DownloadStatus.FAILED -> Text("Failed", color = MaterialTheme.colorScheme.error)
                    else -> Button(onClick = onInstall, enabled = canInstall) { Text("Install") }
                }
            },
        )
        if (status == DownloadStatus.DOWNLOADING || status == DownloadStatus.EXTRACTING) {
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(value, units[unitIndex])
}
