package com.onideity.rommcompanion.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PairingScreen(
    viewModel: PairingViewModel,
    onPaired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isApproved) {
        if (state.isApproved) onPaired()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Connect to RomM",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Point this device at your RomM server, then approve it from the RomM web UI.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        val pairing = state.pairing
        if (pairing == null) {
            OutlinedTextField(
                value = state.serverUrlInput,
                onValueChange = viewModel::onServerUrlChanged,
                label = { Text("Server address, e.g. https://romm.example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = viewModel::beginPairing,
                enabled = !state.isStartingPairing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isStartingPairing) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text("Pair this device")
            }
        } else {
            Text("Enter this code on your RomM server:", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = pairing.userCode,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = pairing.verificationPathComplete,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            CircularProgressIndicator()
            Text("Waiting for approval…", style = MaterialTheme.typography.bodySmall)
        }

        state.errorMessage?.let { message ->
            HorizontalDivider()
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }
}
