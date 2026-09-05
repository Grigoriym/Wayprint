@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImage
import kotlin.coroutines.resume

/**
 * Android's `actual` writes to the device gallery/share sheet (`MediaStore`/`FileProvider`);
 * this saves into the user's photo library via `PHPhotoLibrary` ([saveToGallery]) and hands off
 * to the system share sheet via `UIActivityViewController` ([share]) — both presented from the
 * current window's top-most view controller ([topMostViewController], `GpxPickerLauncher.ios.kt`).
 */
actual class ImageExporter {

    actual val supportsShare: Boolean = true

    actual val saveConfirmationMessage: String = "Saved to Photos"

    actual suspend fun saveToGallery(image: ImageBitmap): Boolean {
        val uiImage = image.toUIImage() ?: return false
        return suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                PHAssetChangeRequest.creationRequestForAssetFromImage(uiImage)
            }) { success, _ ->
                continuation.resume(success)
            }
        }
    }

    actual suspend fun share(image: ImageBitmap) {
        val pngBytes = image.toPngBytes() ?: return
        val fileName = "wayprint-${NSDate().timeIntervalSince1970.toLong()}.png"
        val path = Path(NSTemporaryDirectory(), fileName)
        SystemFileSystem.sink(path).buffered().use { it.write(pngBytes) }

        val activityController = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path.toString())),
            applicationActivities = null
        )
        topMostViewController()?.presentViewController(activityController, animated = true, completion = null)
    }
}

private fun ImageBitmap.toUIImage(): UIImage? = toPngData()?.let { UIImage.imageWithData(it) }

private fun ImageBitmap.toPngBytes(): ByteArray? =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes

private fun ImageBitmap.toPngData(): NSData? {
    val pngBytes = toPngBytes() ?: return null
    return pngBytes.usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = pngBytes.size.convert()) }
}
