package com.onideity.rommcompanion.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.onideity.rommcompanion.data.local.db.dao.DownloadStateDao
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Enqueues rom installs as WorkManager jobs and exposes their live state. */
class DownloadRepository(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val downloadStateDao: DownloadStateDao,
) {
    fun observeAll(): Flow<List<DownloadStateEntity>> = downloadStateDao.observeAll()

    fun observeForRom(romId: Long): Flow<DownloadStateEntity?> = downloadStateDao.observeForRom(romId)

    suspend fun enqueueInstall(romId: Long, platformId: Long, fileName: String) {
        val wifiOnly = settingsRepository.wifiOnlyDownloads.first()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<RomDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putLong(RomDownloadWorker.KEY_ROM_ID, romId)
                    .putLong(RomDownloadWorker.KEY_PLATFORM_ID, platformId)
                    .putString(RomDownloadWorker.KEY_FILE_NAME, fileName)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            RomDownloadWorker.uniqueWorkName(romId),
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelInstall(romId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(RomDownloadWorker.uniqueWorkName(romId))
    }
}
