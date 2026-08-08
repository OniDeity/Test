package com.onideity.rommcompanion.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "romm_companion_settings")

private object Keys {
    val SERVER_URL = stringPreferencesKey("server_url")
    val CLIENT_DEVICE_IDENTIFIER = stringPreferencesKey("client_device_identifier")
    val PAIRED_DEVICE_ID = stringPreferencesKey("paired_device_id")
    val PAIRED_DEVICE_NAME = stringPreferencesKey("paired_device_name")
    val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
    val AUTO_EXTRACT_ARCHIVES = booleanPreferencesKey("auto_extract_archives")

    fun romFolder(platformId: Long) = stringPreferencesKey("rom_folder_uri_$platformId")
    fun biosFolder(platformId: Long) = stringPreferencesKey("bios_folder_uri_$platformId")
}

/**
 * Non-secret app settings: server address, pairing metadata (the bearer
 * token itself lives in [SecureTokenStore]), download prefs, and per-platform
 * SAF folder URIs picked via ACTION_OPEN_DOCUMENT_TREE.
 */
class SettingsRepository(private val context: Context) {

    val serverUrl: Flow<String?> =
        context.dataStore.data.map { it[Keys.SERVER_URL] }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[Keys.SERVER_URL] = url }
    }

    /** Stable per-install identifier RomM uses to recognize a re-pairing of the same device. */
    suspend fun getOrCreateClientDeviceIdentifier(): String {
        val existing = context.dataStore.data.first()[Keys.CLIENT_DEVICE_IDENTIFIER]
        if (existing != null) return existing

        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { it[Keys.CLIENT_DEVICE_IDENTIFIER] = generated }
        return generated
    }

    val pairedDeviceId: Flow<String?> =
        context.dataStore.data.map { it[Keys.PAIRED_DEVICE_ID] }

    val pairedDeviceName: Flow<String?> =
        context.dataStore.data.map { it[Keys.PAIRED_DEVICE_NAME] }

    suspend fun setPairedDevice(deviceId: String, name: String?) {
        context.dataStore.edit {
            it[Keys.PAIRED_DEVICE_ID] = deviceId
            if (name != null) it[Keys.PAIRED_DEVICE_NAME] = name
        }
    }

    /** Clears local pairing state (settings + token). Does not revoke the device server-side. */
    suspend fun clearPairing(tokenStore: SecureTokenStore) {
        tokenStore.clearToken()
        context.dataStore.edit {
            it.remove(Keys.PAIRED_DEVICE_ID)
            it.remove(Keys.PAIRED_DEVICE_NAME)
        }
    }

    val wifiOnlyDownloads: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.WIFI_ONLY_DOWNLOADS] ?: true }

    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIFI_ONLY_DOWNLOADS] = enabled }
    }

    val autoExtractArchives: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.AUTO_EXTRACT_ARCHIVES] ?: true }

    suspend fun setAutoExtractArchives(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_EXTRACT_ARCHIVES] = enabled }
    }

    fun romFolderUri(platformId: Long): Flow<String?> =
        context.dataStore.data.map { it[Keys.romFolder(platformId)] }

    suspend fun setRomFolderUri(platformId: Long, treeUri: String) {
        context.dataStore.edit { it[Keys.romFolder(platformId)] = treeUri }
    }

    fun biosFolderUri(platformId: Long): Flow<String?> =
        context.dataStore.data.map { it[Keys.biosFolder(platformId)] }

    suspend fun setBiosFolderUri(platformId: Long, treeUri: String) {
        context.dataStore.edit { it[Keys.biosFolder(platformId)] = treeUri }
    }
}
