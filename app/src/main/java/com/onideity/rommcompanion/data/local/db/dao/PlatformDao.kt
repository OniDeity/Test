package com.onideity.rommcompanion.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.onideity.rommcompanion.data.local.db.entity.PlatformEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatformDao {
    @Query("SELECT * FROM platforms ORDER BY displayName ASC")
    fun observeAll(): Flow<List<PlatformEntity>>

    @Query("SELECT * FROM platforms WHERE id = :id")
    suspend fun getById(id: Long): PlatformEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(platforms: List<PlatformEntity>)

    @Query("DELETE FROM platforms WHERE id NOT IN (:keepIds)")
    suspend fun deleteMissing(keepIds: List<Long>)

    @Transaction
    suspend fun replaceAll(platforms: List<PlatformEntity>) {
        upsertAll(platforms)
        deleteMissing(platforms.map { it.id })
    }
}
