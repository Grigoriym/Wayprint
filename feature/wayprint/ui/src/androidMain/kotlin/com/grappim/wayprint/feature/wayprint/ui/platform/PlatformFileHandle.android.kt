package com.grappim.wayprint.feature.wayprint.ui.platform

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

actual class PlatformFileHandle(val uri: Uri, private val context: Context) {

    actual fun readBytes(): ByteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("Couldn't open $uri")

    /** [OpenableColumns.DISPLAY_NAME] on [uri]; falls back for a share-intent `Uri` that has no such column. */
    actual fun displayName(): String {
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
        return name ?: "Untitled route"
    }
}
