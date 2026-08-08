package com.onideity.rommcompanion.download

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onideity.rommcompanion.RommCompanionApp
import com.onideity.rommcompanion.data.local.db.dao.DownloadStateDao
import com.onideity.rommcompanion.data.local.db.entity.DownloadStateEntity
import com.onideity.rommcompanion.data.local.db.entity.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Downloads a single rom's primary file into the user's configured
 * per-platform SAF folder, and extracts it in place when it's a zip/7z
 * archive (many handheld emulators expect loose files, not archives).
 *
 * This is an MVP implementation: single-file roms, no resume-from-partial
 * on process death (WorkManager will simply retry the whole download), and
 * progress is reported coarsely. Good enough to prove the pipeline end to
 * end; tighten as real usage surfaces gaps.
 */
class RomDownloadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_ROM_ID = "rom_id"
        const val KEY_PLATFORM_ID = "platform_id"
        const val KEY_FILE_NAME = "file_name"

        fun uniqueWorkName(romId: Long) = "rom_download_$romId"
    }

    private val container get() = (applicationContext as RommCompanionApp).container

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val romId = inputData.getLong(KEY_ROM_ID, -1L)
        val platformId = inputData.getLong(KEY_PLATFORM_ID, -1L)
        val fileName = inputData.getString(KEY_FILE_NAME)

        if (romId < 0 || platformId < 0 || fileName.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        val downloadStateDao = container.database.downloadStateDao()

        try {
            val folderUriString = container.settingsRepository.romFolderUri(platformId).first()
                ?: throw IllegalStateException("No download folder configured for this platform yet")

            val destDir = DocumentFile.fromTreeUri(applicationContext, Uri.parse(folderUriString))
                ?: throw IllegalStateException("Configured folder is no longer accessible")

            downloadStateDao.upsert(
                DownloadStateEntity(
                    romId = romId,
                    status = DownloadStatus.DOWNLOADING,
                    updatedAt = System.currentTimeMillis(),
                ),
            )

            val body = container.session.requireApi().downloadRomContent(romId, fileName)
            val downloadedFile = downloadToDocument(destDir, fileName, body, romId, downloadStateDao)

            val shouldExtract = container.settingsRepository.autoExtractArchives.first()
            val destinationUri = if (shouldExtract && looksLikeArchive(fileName)) {
                downloadStateDao.upsert(
                    DownloadStateEntity(
                        romId = romId,
                        status = DownloadStatus.EXTRACTING,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
                extractArchive(downloadedFile, destDir, fileName)
                downloadedFile.delete()
                destDir.uri.toString()
            } else {
                downloadedFile.uri.toString()
            }

            downloadStateDao.upsert(
                DownloadStateEntity(
                    romId = romId,
                    status = DownloadStatus.INSTALLED,
                    progressPercent = 100,
                    destinationUri = destinationUri,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            Result.success()
        } catch (t: Throwable) {
            downloadStateDao.upsert(
                DownloadStateEntity(
                    romId = romId,
                    status = DownloadStatus.FAILED,
                    errorMessage = t.message ?: t::class.simpleName,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            Result.failure()
        }
    }

    private suspend fun downloadToDocument(
        destDir: DocumentFile,
        fileName: String,
        body: ResponseBody,
        romId: Long,
        downloadStateDao: DownloadStateDao,
    ): DocumentFile {
        destDir.findFile(fileName)?.delete()
        val target = destDir.createFile("application/octet-stream", fileName)
            ?: throw IllegalStateException("Could not create $fileName in the configured folder")

        val totalBytes = body.contentLength()
        var bytesRead = 0L
        var lastReportedPercent = -1

        applicationContext.contentResolver.openOutputStream(target.uri)?.use { output ->
            body.byteStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    bytesRead += read

                    if (totalBytes > 0) {
                        val percent = ((bytesRead * 100) / totalBytes).toInt()
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            downloadStateDao.upsert(
                                DownloadStateEntity(
                                    romId = romId,
                                    status = DownloadStatus.DOWNLOADING,
                                    progressPercent = percent,
                                    bytesDownloaded = bytesRead,
                                    totalBytes = totalBytes,
                                    updatedAt = System.currentTimeMillis(),
                                ),
                            )
                        }
                    }
                }
            }
        } ?: throw IllegalStateException("Could not open output stream for $fileName")

        return target
    }

    private fun looksLikeArchive(fileName: String) =
        fileName.endsWith(".zip", ignoreCase = true) || fileName.endsWith(".7z", ignoreCase = true)

    private fun extractArchive(archiveDoc: DocumentFile, destDir: DocumentFile, fileName: String) {
        when {
            fileName.endsWith(".zip", ignoreCase = true) -> extractZip(archiveDoc, destDir)
            fileName.endsWith(".7z", ignoreCase = true) -> extractSevenZip(archiveDoc, destDir)
        }
    }

    private fun extractZip(archiveDoc: DocumentFile, destDir: DocumentFile) {
        val resolver = applicationContext.contentResolver
        resolver.openInputStream(archiveDoc.uri)?.use { rawInput ->
            ZipInputStream(rawInput).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val entryName = entry.name.substringAfterLast('/')
                        destDir.findFile(entryName)?.delete()
                        val outDoc = destDir.createFile("application/octet-stream", entryName)
                        if (outDoc != null) {
                            resolver.openOutputStream(outDoc.uri)?.use { out ->
                                zip.copyTo(out)
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
    }

    /**
     * SevenZFile needs random access, so we can't stream straight from SAF;
     * stage the archive in app-private cache first, then copy extracted
     * entries into the SAF destination.
     */
    private fun extractSevenZip(archiveDoc: DocumentFile, destDir: DocumentFile) {
        val resolver = applicationContext.contentResolver
        val staged = File.createTempFile("staged", ".7z", applicationContext.cacheDir)
        try {
            resolver.openInputStream(archiveDoc.uri)?.use { input ->
                staged.outputStream().use { output -> input.copyTo(output) }
            }

            SevenZFile.builder().setFile(staged).get().use { sevenZFile ->
                var entry = sevenZFile.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val entryName = entry.name.substringAfterLast('/')
                        destDir.findFile(entryName)?.delete()
                        val outDoc = destDir.createFile("application/octet-stream", entryName)
                        if (outDoc != null) {
                            resolver.openOutputStream(outDoc.uri)?.use { out ->
                                val buffer = ByteArray(64 * 1024)
                                while (true) {
                                    val read = sevenZFile.read(buffer)
                                    if (read == -1) break
                                    out.write(buffer, 0, read)
                                }
                            }
                        }
                    }
                    entry = sevenZFile.nextEntry
                }
            }
        } finally {
            staged.delete()
        }
    }
}
