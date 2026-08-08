package com.onideity.rommcompanion.data.repository

import com.onideity.rommcompanion.data.local.db.dao.PlatformDao
import com.onideity.rommcompanion.data.local.db.dao.RomDao
import com.onideity.rommcompanion.data.local.db.entity.PlatformEntity
import com.onideity.rommcompanion.data.local.db.entity.RomEntity
import com.onideity.rommcompanion.data.remote.RommSession
import com.onideity.rommcompanion.data.remote.dto.PlatformDto
import com.onideity.rommcompanion.data.remote.dto.RomDto
import kotlinx.coroutines.flow.Flow

private const val ROMS_PAGE_SIZE = 100

/**
 * Browses RomM's library. Platforms/roms are cached in Room so the library
 * is still browsable offline; [refreshPlatforms]/[refreshRoms] pull fresh
 * data and let callers decide when (pull-to-refresh, app open, etc).
 */
class LibraryRepository(
    private val session: RommSession,
    private val platformDao: PlatformDao,
    private val romDao: RomDao,
) {
    fun observePlatforms(): Flow<List<PlatformEntity>> = platformDao.observeAll()

    fun observeRoms(platformId: Long): Flow<List<RomEntity>> = romDao.observeByPlatform(platformId)

    suspend fun refreshPlatforms(): Result<Unit> = runCatching {
        val platforms = session.requireApi().getPlatforms()
        platformDao.replaceAll(platforms.map { it.toEntity() })
    }

    suspend fun refreshRoms(platformId: Long): Result<Unit> = runCatching {
        val api = session.requireApi()
        val allRoms = mutableListOf<RomDto>()
        var offset = 0
        while (true) {
            val page = api.getRoms(platformId = platformId, limit = ROMS_PAGE_SIZE, offset = offset)
            allRoms += page.items
            if (page.items.size < ROMS_PAGE_SIZE) break
            offset += ROMS_PAGE_SIZE
        }
        romDao.replaceForPlatform(platformId, allRoms.map { it.toEntity() })
    }
}

private fun PlatformDto.toEntity() = PlatformEntity(
    id = id,
    fsSlug = fsSlug,
    displayName = displayName,
    romCount = romCount,
    urlLogo = urlLogo,
    fsSizeBytes = fsSizeBytes,
)

private fun RomDto.toEntity(): RomEntity {
    val topLevelFile = files.firstOrNull { it.isTopLevel } ?: files.firstOrNull()
    return RomEntity(
        id = id,
        platformId = platformId,
        platformDisplayName = platformDisplayName,
        primaryFileName = topLevelFile?.fileName ?: fsName,
        displayTitle = name ?: fsName,
        fsSizeBytes = fsSizeBytes,
        pathCoverSmall = pathCoverSmall,
        revision = revision,
        hasMultipleFiles = hasMultipleFiles,
        updatedAt = updatedAt,
    )
}
