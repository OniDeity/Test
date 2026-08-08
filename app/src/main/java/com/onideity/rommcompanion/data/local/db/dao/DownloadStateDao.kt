package com.onideity.rommcompanion.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadStateDao {
    @Query("SELECT * FROM download_states")
    fun observeAll(): Flow<List<DownloadStateEntity>>

    @Query("SELECT * FROM download_states WHERE romId = :romId")
    fun observeForRom(romId: Long): Flow<DownloadStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: DownloadStateEntity)

    @Query("DELETE FROM download_states WHERE romId = :romId")
    suspend fun delete(romId: Long)
}
