package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.io.File

/**
 * Desktop has no gallery or share sheet — [saveToGallery] opens a native "Save As" dialog instead
 * of writing to a media store, and [supportsShare] is `false` since there's nothing for [share]
 * to hand off to (M15's shared context's open design question, resolved this way in M15.7).
 *
 * Not wired into any Gradle target yet: `core:storage`'s `build.gradle.kts`/this module's own
 * only target Android today — `jvmMain` becomes a real, compiled source set once M15.8 adds
 * `jvm()` to `configureKmp()`, which is also where this gets its first real compile/run check.
 */
actual class ImageExporter {

    actual val supportsShare: Boolean = false

    actual suspend fun saveToGallery(image: ImageBitmap): Boolean {
        val dialog = FileDialog(null as java.awt.Frame?, "Save Wayprint Image", FileDialog.SAVE)
        dialog.file = "wayprint-${System.currentTimeMillis()}.png"
        dialog.isVisible = true
        val directory = dialog.directory ?: return false
        val fileName = dialog.file ?: return false
        val pngBytes = Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
            ?: return false
        File(directory, fileName).writeBytes(pngBytes)
        return true
    }

    /** Never called — the UI checks [supportsShare] before offering Share at all. */
    actual suspend fun share(image: ImageBitmap) {
        error("Desktop has no share surface — check supportsShare before calling share()")
    }
}
