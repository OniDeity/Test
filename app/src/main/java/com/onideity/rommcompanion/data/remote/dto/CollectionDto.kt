package com.onideity.rommcompanion.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionDto(
    val id: Long,
    val name: String,
    @Json(name = "is_smart") val isSmart: Boolean = false,
    @Json(name = "rom_count") val romCount: Int = 0,
)
