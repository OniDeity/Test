package com.onideity.rommcompanion.ui.common

/**
 * RomM's cover-art/logo fields (path_cover_small, url_logo, ...) are server-
 * relative paths, not absolute URLs — resolve against the configured server
 * before handing them to an image loader.
 */
fun resolveMediaUrl(serverUrl: String?, path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    if (serverUrl.isNullOrBlank()) return null

    val base = serverUrl.trimEnd('/')
    val suffix = path.trimStart('/')
    return "$base/$suffix"
}
