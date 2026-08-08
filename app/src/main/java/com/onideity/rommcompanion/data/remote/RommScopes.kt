package com.onideity.rommcompanion.data.remote

/**
 * Scope strings requested during device pairing. RomM reports back which of
 * these the pairing user is actually allowed to grant (see
 * DeviceAuthPendingSchema.allowed_scopes) — verify this exact set against a
 * live server's /api/openapi.json if a future RomM release renames any of
 * them, the "read:x write:x" pattern is confirmed but the full scope list
 * isn't.
 */
object RommScopes {
    const val ROMS_READ = "read:roms"
    const val PLATFORMS_READ = "read:platforms"
    const val FIRMWARE_READ = "read:firmware"
    const val ASSETS_READ = "read:assets"
    const val ASSETS_WRITE = "write:assets"
    const val DEVICES_READ = "read:devices"
    const val DEVICES_WRITE = "write:devices"

    val REQUIRED = listOf(
        ROMS_READ,
        PLATFORMS_READ,
        FIRMWARE_READ,
        ASSETS_READ,
        ASSETS_WRITE,
        DEVICES_READ,
        DEVICES_WRITE,
    )
}
