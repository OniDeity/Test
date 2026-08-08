package com.onideity.rommcompanion.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local cache of RomM platforms, refreshed from GET /api/platforms. */
@Entity(tableName = "platforms")
data class PlatformEntity(
    @PrimaryKey val id: Long,
    val fsSlug: String,
    val displayName: String,
    val romCount: Int,
    val urlLogo: String?,
    val fsSizeBytes: Long,
)
