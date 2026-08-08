package com.onideity.rommcompanion.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Save and state responses both extend RomM's BaseAsset
 * (backend/endpoints/responses/assets.py); duplicated here rather than shared
 * via inheritance since Moshi codegen doesn't need it and it keeps each DTO
 * readable on its own.
 */
@JsonClass(generateAdapter = true)
data class SaveDto(
    val id: Long,
    @Json(name = "rom_id") val romId: Long,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_size_bytes") val fileSizeBytes: Long,
    val emulator: String? = null,
    val slot: String? = null,
    @Json(name = "content_hash") val contentHash: String? = null,
    @Json(name = "is_public") val isPublic: Boolean = false,
    @Json(name = "origin_device_id") val originDeviceId: String? = null,
    @Json(name = "device_syncs") val deviceSyncs: List<DeviceSyncDto> = emptyList(),
    @Json(name = "updated_at") val updatedAt: String,
)

@JsonClass(generateAdapter = true)
data class StateDto(
    val id: Long,
    @Json(name = "rom_id") val romId: Long,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_size_bytes") val fileSizeBytes: Long,
    val emulator: String? = null,
    @Json(name = "updated_at") val updatedAt: String,
)

@JsonClass(generateAdapter = true)
data class DeviceSyncDto(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_name") val deviceName: String? = null,
    @Json(name = "last_synced_at") val lastSyncedAt: String,
    @Json(name = "is_untracked") val isUntracked: Boolean,
    @Json(name = "is_current") val isCurrent: Boolean,
)

@JsonClass(generateAdapter = true)
data class FirmwareDto(
    val id: Long,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_size_bytes") val fileSizeBytes: Long,
    @Json(name = "is_verified") val isVerified: Boolean,
    @Json(name = "missing_from_fs") val missingFromFs: Boolean = false,
)
