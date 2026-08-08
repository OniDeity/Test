package com.onideity.rommcompanion.di

import android.content.Context
import androidx.room.Room
import com.onideity.rommcompanion.data.local.db.RommDatabase
import com.onideity.rommcompanion.data.local.prefs.SecureTokenStore
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import com.onideity.rommcompanion.data.remote.RommSession
import com.onideity.rommcompanion.data.repository.AuthRepository
import com.onideity.rommcompanion.data.repository.DeviceRepository
import com.onideity.rommcompanion.data.repository.LibraryRepository
import com.onideity.rommcompanion.download.DownloadRepository

/**
 * Hand-rolled DI container instead of Hilt/Koin — the object graph here is
 * small and flat enough that annotation processing would add build
 * complexity without buying much. Revisit if this grows past a couple of
 * dozen bindings.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: RommDatabase by lazy {
        Room.databaseBuilder(appContext, RommDatabase::class.java, RommDatabase.DB_NAME).build()
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    val tokenStore: SecureTokenStore by lazy { SecureTokenStore(appContext) }

    val session: RommSession by lazy { RommSession(settingsRepository, tokenStore) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(session, settingsRepository, tokenStore)
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(session, database.platformDao(), database.romDao())
    }

    val deviceRepository: DeviceRepository by lazy { DeviceRepository(session) }

    val downloadRepository: DownloadRepository by lazy {
        DownloadRepository(appContext, settingsRepository, database.downloadStateDao())
    }
}
