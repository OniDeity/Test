package com.onideity.rommcompanion.data.repository

import android.os.Build
import com.onideity.rommcompanion.data.local.prefs.SecureTokenStore
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import com.onideity.rommcompanion.data.remote.RommScopes
import com.onideity.rommcompanion.data.remote.RommSession
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthInitRequest
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthInitResponse
import com.onideity.rommcompanion.data.remote.dto.DeviceAuthTokenRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

sealed interface PairingPollResult {
    data object Pending : PairingPollResult
    data object SlowDown : PairingPollResult
    data object Denied : PairingPollResult
    data object Expired : PairingPollResult
    data class Approved(val deviceId: String) : PairingPollResult
    data class UnknownError(val message: String) : PairingPollResult
}

/**
 * Drives RomM's device-code pairing flow (backend/endpoints/device_auth.py):
 * start a pairing request, show the user code/QR, poll until the user
 * approves it from the RomM web UI, then persist the issued token.
 */
class AuthRepository(
    private val session: RommSession,
    private val settingsRepository: SettingsRepository,
    private val tokenStore: SecureTokenStore,
) {
    val isPaired: Flow<Boolean> = settingsRepository.pairedDeviceId.map { it != null }

    /** Persists [url] and rebuilds the API client for it, synchronously — call before [startPairing]. */
    suspend fun configureServer(url: String) {
        settingsRepository.setServerUrl(url)
        session.useServerUrl(url)
    }

    suspend fun startPairing(): DeviceAuthInitResponse {
        val clientDeviceIdentifier = settingsRepository.getOrCreateClientDeviceIdentifier()
        return session.requireApi().initDevicePairing(
            DeviceAuthInitRequest(
                clientDeviceIdentifier = clientDeviceIdentifier,
                name = "${Build.MANUFACTURER} ${Build.MODEL}",
                clientVersion = null,
                requestedScopes = RommScopes.REQUIRED,
            ),
        )
    }

    suspend fun pollOnce(deviceCode: String): PairingPollResult {
        val response = session.requireApi().pollDevicePairing(DeviceAuthTokenRequest(deviceCode))

        if (response.isSuccessful) {
            val body = response.body() ?: return PairingPollResult.UnknownError("Empty response")
            tokenStore.saveToken(body.accessToken)
            settingsRepository.setPairedDevice(body.deviceId, null)
            return PairingPollResult.Approved(body.deviceId)
        }

        val detail = runCatching {
            JSONObject(response.errorBody()?.string().orEmpty()).optString("detail")
        }.getOrNull()

        return when (detail) {
            "authorization_pending" -> PairingPollResult.Pending
            "slow_down" -> PairingPollResult.SlowDown
            "access_denied" -> PairingPollResult.Denied
            "expired_token" -> PairingPollResult.Expired
            else -> PairingPollResult.UnknownError(detail ?: "HTTP ${response.code()}")
        }
    }

    /** Clears local pairing state. Does not revoke the device server-side — use DeviceRepository.revokeDevice for that. */
    suspend fun signOut() {
        settingsRepository.clearPairing(tokenStore)
    }
}
