package com.onideity.rommcompanion.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Mirrors RomM's PlatformSchema (backend/endpoints/responses/platform.py). */
@JsonClass(generateAdapter = true)
data class PlatformDto(
    val id: Long,
    val slug: String,
    @Json(name = "fs_slug") val fsSlug: String,
    @Json(name = "rom_count") val romCount: Int,
    val name: String,
    @Json(name = "custom_name") val customName: String? = null,
    @Json(name = "display_name") val displayName: String,
    val category: String? = null,
    @Json(name = "url_logo") val urlLogo: String? = null,
    @Json(name = "fs_size_bytes") val fsSizeBytes: Long,
    @Json(name = "firmware_count") val firmwareCount: Int = 0,
    @Json(name = "missing_from_fs") val missingFromFs: Boolean = false,
)
