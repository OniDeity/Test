package com.onideity.rommcompanion.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Mirrors RomM's DeviceSchema (backend/endpoints/responses/device.py). */
@JsonClass(generateAdapter = true)
data class DeviceDto(
    val id: String,
    val name: String? = null,
    val platform: String? = null,
    val client: String? = null,
    @Json(name = "client_version") val clientVersion: String? = null,
    @Json(name = "sync_enabled") val syncEnabled: Boolean = true,
    @Json(name = "last_seen") val lastSeen: String? = null,
    @Json(name = "created_at") val createdAt: String,
)
