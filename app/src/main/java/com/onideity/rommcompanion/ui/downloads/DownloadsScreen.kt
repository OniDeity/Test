package com.onideity.rommcompanion.ui.downloads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import com.onideity.rommcompanion.data.local.db.entity.DownloadStatus

/**
 * Shows raw rom ids for now rather than titles — joining against the rom
 * cache for a friendly name is a natural next step once this list needs to
 * be more than a debug-ish status view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onBack: () -> Unit,
) {
    val downloads by viewModel.downloads.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            if (downloads.isEmpty()) {
                Text(
                    text = "No downloads yet.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(downloads, key = { it.romId }) { download ->
                        DownloadRow(download = download, onCancel = { viewModel.cancel(download.romId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(download: DownloadStateEntity, onCancel: () -> Unit) {
    Column {
        ListItem(
            headlineContent = { Text("Rom #${download.romId}") },
            supportingContent = {
                Text(
                    when (download.status) {
                        DownloadStatus.QUEUED -> "Queued"
                        DownloadStatus.DOWNLOADING -> "Downloading — ${download.progressPercent}%"
                        DownloadStatus.EXTRACTING -> "Extracting…"
                        DownloadStatus.INSTALLED -> "Installed"
                        DownloadStatus.FAILED -> download.errorMessage ?: "Failed"
                    },
                    color = if (download.status == DownloadStatus.FAILED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
            trailingContent = {
                if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.QUEUED) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                }
            },
        )
        if (download.status == DownloadStatus.DOWNLOADING) {
            LinearProgressIndicator(
                progress = { download.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
