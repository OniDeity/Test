package com.onideity.rommcompanion.data.remote

import com.onideity.rommcompanion.data.local.prefs.SecureTokenStore
import com.onideity.rommcompanion.data.local.prefs.SettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

/**
 * Rebuilds the [RommApi] client whenever the user (re)configures a server
 * URL, since — unlike a normal SaaS backend — the base URL isn't known until
 * the user points the app at their own RomM instance.
 */
class RommSession(
    private val settingsRepository: SettingsRepository,
    private val tokenStore: SecureTokenStore,
) {
    @Volatile
    private var cachedBaseUrl: String? = null

    @Volatile
    private var cachedApi: RommApi? = null

    /** Call once at app start to keep [api] in sync as the server URL changes elsewhere (e.g. Settings). */
    suspend fun watchServerUrl() {
        settingsRepository.serverUrl.filterNotNull().distinctUntilChanged().collect { url ->
            useServerUrl(url)
        }
    }

    /** Synchronously (re)builds the client for [url] — no network call, so no race to wait out. */
    fun useServerUrl(url: String) {
        if (url == cachedBaseUrl) return
        cachedBaseUrl = url
        cachedApi = RommApiFactory.create(url) { tokenStore.getToken() }
    }

    /** The current [RommApi], if a server URL has been configured. */
    fun api(): RommApi? = cachedApi

    fun requireApi(): RommApi =
        cachedApi ?: error("RomM server URL not configured yet")
}
