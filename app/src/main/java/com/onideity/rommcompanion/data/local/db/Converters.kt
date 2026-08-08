package com.onideity.rommcompanion.data.local.db

import androidx.room.TypeConverter
import com.onideity.rommcompanion.data.local.db.entity.DownloadStatus

class Converters {
    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
}
