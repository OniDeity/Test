package com.onideity.rommcompanion.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.onideity.rommcompanion.data.local.db.dao.DownloadStateDao
import com.onideity.rommcompanion.data.local.db.dao.PlatformDao
import com.onideity.rommcompanion.data.local.db.dao.RomDao
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import com.onideity.rommcompanion.data.local.db.entity.PlatformEntity
import com.onideity.rommcompanion.data.local.db.entity.RomEntity

@Database(
    entities = [PlatformEntity::class, RomEntity::class, DownloadStateEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RommDatabase : RoomDatabase() {
    abstract fun platformDao(): PlatformDao
    abstract fun romDao(): RomDao
    abstract fun downloadStateDao(): DownloadStateDao

    companion object {
        const val DB_NAME = "romm_companion.db"
    }
}
