package com.onideity.rommcompanion.ui.platform

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformFolderSettingsScreen(
    viewModel: PlatformFolderSettingsViewModel,
    platformName: String,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    fun persistAndSave(uri: Uri, onSaved: (String) -> Unit) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        onSaved(uri.toString())
    }

    val romFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) persistAndSave(uri, viewModel::setRomFolderUri)
    }
    val biosFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) persistAndSave(uri, viewModel::setBiosFolderUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$platformName folders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Choose where $platformName roms should be downloaded to. Point Cocoon's " +
                    "library folder for this platform at the same place, and it'll pick up " +
                    "everything installed here.",
                style = MaterialTheme.typography.bodyMedium,
            )

            FolderRow(
                label = "ROM folder",
                currentUri = state.romFolderUri,
                onChoose = { romFolderPicker.launch(null) },
            )

            FolderRow(
                label = "BIOS / firmware folder",
                currentUri = state.biosFolderUri,
                onChoose = { biosFolderPicker.launch(null) },
            )
        }
    }
}

@Composable
private fun FolderRow(label: String, currentUri: String?, onChoose: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(
            text = currentUri?.let { Uri.parse(it).lastPathSegment ?: it } ?: "Not set",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = onChoose) {
            Text(if (currentUri == null) "Choose folder" else "Change folder")
        }
    }
}
