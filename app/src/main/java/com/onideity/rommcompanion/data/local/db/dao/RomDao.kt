package com.onideity.rommcompanion.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.onideity.rommcompanion.data.local.db.entity.RomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RomDao {
    @Query("SELECT * FROM roms WHERE platformId = :platformId ORDER BY displayTitle ASC")
    fun observeByPlatform(platformId: Long): Flow<List<RomEntity>>

    @Query("SELECT * FROM roms WHERE id = :id")
    suspend fun getById(id: Long): RomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(roms: List<RomEntity>)

    @Query("DELETE FROM roms WHERE platformId = :platformId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(platformId: Long, keepIds: List<Long>)

    @Transaction
    suspend fun replaceForPlatform(platformId: Long, roms: List<RomEntity>) {
        upsertAll(roms)
        deleteMissing(platformId, roms.map { it.id })
    }
}
