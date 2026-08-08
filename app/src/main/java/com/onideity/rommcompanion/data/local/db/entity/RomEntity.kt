package com.onideity.rommcompanion.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Local cache of a platform's roms, refreshed from GET /api/roms. */
@Entity(
    tableName = "roms",
    indices = [Index("platformId")],
)
data class RomEntity(
    @PrimaryKey val id: Long,
    val platformId: Long,
    val platformDisplayName: String,
    /** The top-level file name to request from /api/roms/{id}/content/{fileName}. */
    val primaryFileName: String,
    val displayTitle: String,
    val fsSizeBytes: Long,
    val pathCoverSmall: String?,
    val revision: String?,
    val hasMultipleFiles: Boolean,
    val updatedAt: String,
)
