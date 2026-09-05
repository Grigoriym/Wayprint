package com.grappim.wayprint.feature.wayprint.ui.platform

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

actual class ImageExporter(private val context: Context) {

    actual val supportsShare: Boolean = true

    actual val saveConfirmationMessage: String = "Saved to gallery"

    actual suspend fun saveToGallery(image: ImageBitmap): Boolean {
        val uri = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            image.asAndroidBitmap(),
            "wayprint-${System.currentTimeMillis()}",
            null
        )
        return uri != null
    }

    /** Shares [image] via a temp file under `cacheDir` and a [FileProvider] URI — no `MediaStore` write. */
    actual suspend fun share(image: ImageBitmap) {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesDir, "wayprint-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
