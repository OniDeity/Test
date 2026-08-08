package com.onideity.rommcompanion.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    EXTRACTING,
    INSTALLED,
    FAILED,
}

/** Tracks install state for a rom independent of the WorkManager job lifecycle. */
@Entity(tableName = "download_states")
data class DownloadStateEntity(
    @PrimaryKey val romId: Long,
    val status: DownloadStatus,
    val progressPercent: Int = 0,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    /** SAF tree/document URI of the installed file(s), once INSTALLED. */
    val destinationUri: String? = null,
    val errorMessage: String? = null,
    val updatedAt: Long,
)
