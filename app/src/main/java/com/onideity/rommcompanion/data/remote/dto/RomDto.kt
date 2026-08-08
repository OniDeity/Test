package com.onideity.rommcompanion.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Trimmed mirror of RomM's SimpleRomSchema (backend/endpoints/responses/rom.py) —
 * only the fields the app currently uses. Extend as new screens need more.
 */
@JsonClass(generateAdapter = true)
data class RomDto(
    val id: Long,
    @Json(name = "platform_id") val platformId: Long,
    @Json(name = "platform_fs_slug") val platformFsSlug: String,
    @Json(name = "platform_display_name") val platformDisplayName: String,
    @Json(name = "fs_name") val fsName: String,
    @Json(name = "fs_size_bytes") val fsSizeBytes: Long,
    val name: String? = null,
    val summary: String? = null,
    @Json(name = "path_cover_small") val pathCoverSmall: String? = null,
    @Json(name = "path_cover_large") val pathCoverLarge: String? = null,
    val regions: List<String> = emptyList(),
    val revision: String? = null,
    val tags: List<String> = emptyList(),
    @Json(name = "has_multiple_files") val hasMultipleFiles: Boolean = false,
    @Json(name = "missing_from_fs") val missingFromFs: Boolean = false,
    @Json(name = "updated_at") val updatedAt: String,
    val files: List<RomFileDto> = emptyList(),
    @Json(name = "rom_user") val romUser: RomUserDto? = null,
)

@JsonClass(generateAdapter = true)
data class RomFileDto(
    val id: Long,
    @Json(name = "rom_id") val romId: Long,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "file_size_bytes") val fileSizeBytes: Long,
    @Json(name = "is_top_level") val isTopLevel: Boolean,
    @Json(name = "crc_hash") val crcHash: String? = null,
)

@JsonClass(generateAdapter = true)
data class RomUserDto(
    val id: Long,
    @Json(name = "last_played") val lastPlayed: String? = null,
    @Json(name = "now_playing") val nowPlaying: Boolean = false,
    val backlogged: Boolean = false,
    val hidden: Boolean = false,
)

/** RomM paginates roms with fastapi-pagination's LimitOffsetPage. */
@JsonClass(generateAdapter = true)
data class RomPageDto(
    val items: List<RomDto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)
