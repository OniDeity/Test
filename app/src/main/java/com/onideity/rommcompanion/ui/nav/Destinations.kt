package com.onideity.rommcompanion.ui.nav

object Destinations {
    const val PAIRING = "pairing"
    const val PLATFORMS = "platforms"
    const val SETTINGS = "settings"
    const val DOWNLOADS = "downloads"

    const val ROMS_PATTERN = "platforms/{platformId}/roms?platformName={platformName}"
    fun roms(platformId: Long, platformName: String) =
        "platforms/$platformId/roms?platformName=${java.net.URLEncoder.encode(platformName, "UTF-8")}"

    const val PLATFORM_FOLDERS_PATTERN = "platforms/{platformId}/folders?platformName={platformName}"
    fun platformFolders(platformId: Long, platformName: String) =
        "platforms/$platformId/folders?platformName=${java.net.URLEncoder.encode(platformName, "UTF-8")}"

    const val ARG_PLATFORM_ID = "platformId"
    const val ARG_PLATFORM_NAME = "platformName"
}
