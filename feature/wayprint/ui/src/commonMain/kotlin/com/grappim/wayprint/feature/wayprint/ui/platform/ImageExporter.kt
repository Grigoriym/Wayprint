package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Writes an exported route-art image out of the app. Android's `actual` writes to the device
 * gallery ([saveToGallery], via `MediaStore`) and hands off to the share sheet ([share], via a
 * `FileProvider` URI + `ACTION_SEND`) — both unchanged from the pre-M15.7 `WayprintViewModel`.
 *
 * Desktop has no gallery or share-sheet equivalent (M15's shared context flagged this as an open
 * question): its `actual` remaps [saveToGallery] to a native "Save As" file dialog and reports
 * [supportsShare] `false`, since there's nothing meaningful for [share] to do — callers must
 * check [supportsShare] before offering the action at all, rather than calling [share] anyway.
 */
expect class ImageExporter {
    /** Persists [image] permanently; returns whether the user actually completed a save. */
    suspend fun saveToGallery(image: ImageBitmap): Boolean

    /** Hands [image] to the platform's share surface. Only called when [supportsShare] is true. */
    suspend fun share(image: ImageBitmap)

    /** Whether this platform has a real share surface — gates whether the UI offers Share at all. */
    val supportsShare: Boolean

    /** Snackbar text for a completed [saveToGallery], worded for what that platform actually did. */
    val saveConfirmationMessage: String
}
