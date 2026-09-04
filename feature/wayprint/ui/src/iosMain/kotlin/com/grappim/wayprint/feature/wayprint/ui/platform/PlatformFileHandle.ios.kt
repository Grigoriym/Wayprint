package com.grappim.wayprint.feature.wayprint.ui.platform

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import platform.Foundation.NSURL

/**
 * Wraps the security-scoped [NSURL] `UIDocumentPickerViewController` hands back
 * ([rememberGpxPickerLauncher]'s iOS `actual`). [readBytes] brackets the read in
 * `startAccessingSecurityScopedResource`/`stopAccessingSecurityScopedResource`, required to read
 * a URL from outside the app's own sandbox, and reads via kotlinx-io (already a project
 * dependency for [kotlinx.io.files.Path]) rather than raw `NSData` Foundation interop.
 */
actual class PlatformFileHandle(private val url: NSURL) {

    actual fun readBytes(): ByteArray {
        val accessing = url.startAccessingSecurityScopedResource()
        try {
            val path = requireNotNull(url.path) { "Couldn't resolve a path for $url" }
            return SystemFileSystem.source(Path(path)).buffered().use { it.readByteArray() }
        } finally {
            if (accessing) url.stopAccessingSecurityScopedResource()
        }
    }

    actual fun displayName(): String = url.lastPathComponent ?: "Untitled route"
}
