package com.onideity.rommcompanion.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * RFC-8628-style device pairing (backend/endpoints/device_auth.py). The app
 * pairs by showing [DeviceAuthInitResponse.userCode]/QR, then polling
 * /auth/device/token until the user approves it in the RomM web UI.
 */
@JsonClass(generateAdapter = true)
data class DeviceAuthInitRequest(
    @Json(name = "client_device_identifier") val clientDeviceIdentifier: String,
    val name: String,
    val client: String = "romm-companion-android",
    val platform: String = "android",
    @Json(name = "client_version") val clientVersion: String? = null,
    @Json(name = "requested_scopes") val requestedScopes: List<String>,
)

@JsonClass(generateAdapter = true)
data class DeviceAuthInitResponse(
    @Json(name = "device_code") val deviceCode: String,
    @Json(name = "user_code") val userCode: String,
    @Json(name = "verification_path") val verificationPath: String,
    @Json(name = "verification_path_complete") val verificationPathComplete: String,
    @Json(name = "expires_in") val expiresIn: Int,
    val interval: Int,
)

@JsonClass(generateAdapter = true)
data class DeviceAuthTokenRequest(
    @Json(name = "device_code") val deviceCode: String,
)

@JsonClass(generateAdapter = true)
data class DeviceAuthTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "device_id") val deviceId: String,
    val scopes: List<String>,
    @Json(name = "expires_at") val expiresAt: String? = null,
)

/**
 * Error body the server sends on 400 while polling, e.g.
 * {"detail": "authorization_pending"} / "slow_down" / "access_denied" / "expired_token".
 */
@JsonClass(generateAdapter = true)
data class DeviceAuthErrorDto(
    val detail: String,
)
