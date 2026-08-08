package com.onideity.rommcompanion.data.local.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val PREFS_FILE_NAME = "secure_prefs"
private const val KEY_ACCESS_TOKEN = "access_token"

/**
 * Holds the RomM device-pairing bearer token in Keystore-backed encrypted
 * storage. Kept separate from [SettingsRepository]'s plain DataStore prefs
 * (see backup_rules.xml / data_extraction_rules.xml, which exclude this
 * file's backing shared-prefs file from backup) so the token never leaves
 * the device via auto-backup.
 */
class SecureTokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun getToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }
}
