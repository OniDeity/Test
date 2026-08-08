package com.onideity.rommcompanion.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import com.onideity.rommcompanion.data.local.db.entity.PlatformEntity
import com.onideity.rommcompanion.ui.common.resolveMediaUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformListScreen(
    viewModel: PlatformListViewModel,
    onPlatformClick: (PlatformEntity) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Platforms") },
                actions = {
                    IconButton(onClick = onOpenDownloads) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Downloads")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            when {
                state.isRefreshing && state.platforms.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.platforms.isEmpty() -> {
                    Text(
                        text = state.errorMessage
                            ?: "No platforms yet. Pull to refresh once your RomM library has content.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                    )
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(state.platforms, key = { it.id }) { platform ->
                            ListItem(
                                headlineContent = { Text(platform.displayName) },
                                supportingContent = { Text("${platform.romCount} games") },
                                leadingContent = {
                                    AsyncImage(
                                        model = resolveMediaUrl(state.serverUrl, platform.urlLogo),
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                    )
                                },
                                modifier = Modifier.clickable { onPlatformClick(platform) },
                            )
                        }
                    }
                }
            }
        }
    }
}
