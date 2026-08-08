package com.onideity.rommcompanion.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onideity.rommcompanion.data.remote.dto.DeviceDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) onSignedOut()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text("Wi-Fi only downloads") },
                supportingContent = { Text("Skip cellular data for rom installs") },
                trailingContent = {
                    Switch(
                        checked = state.wifiOnlyDownloads,
                        onCheckedChange = viewModel::setWifiOnlyDownloads,
                    )
                },
            )
            ListItem(
                headlineContent = { Text("Auto-extract archives") },
                supportingContent = { Text("Unzip/un-7z downloads that many emulators expect as loose files") },
                trailingContent = {
                    Switch(
                        checked = state.autoExtractArchives,
                        onCheckedChange = viewModel::setAutoExtractArchives,
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Paired devices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            state.devices.forEach { device ->
                DeviceRow(
                    device = device,
                    isThisDevice = device.id == state.thisDeviceId,
                    onRevoke = { viewModel.revokeDevice(device.id) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
                TextButton(onClick = viewModel::signOut) {
                    Text("Sign out of this device")
                }
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceDto, isThisDevice: Boolean, onRevoke: () -> Unit) {
    ListItem(
        headlineContent = { Text(device.name ?: device.id) },
        supportingContent = {
            Text(if (isThisDevice) "This device" else device.client ?: "Unknown client")
        },
        trailingContent = {
            Button(onClick = onRevoke) {
                Text(if (isThisDevice) "Sign out" else "Revoke")
            }
        },
    )
}
