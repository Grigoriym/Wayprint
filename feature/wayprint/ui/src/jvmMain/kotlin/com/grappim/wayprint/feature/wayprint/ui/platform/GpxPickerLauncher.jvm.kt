package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.runtime.Composable
import java.awt.FileDialog
import java.io.File

/** Not wired into any Gradle target yet — see `ImageExporter.jvm.kt`'s doc. */
@Composable
actual fun rememberGpxPickerLauncher(onPick: (PlatformFileHandle) -> Unit): () -> Unit = {
    val dialog = FileDialog(null as java.awt.Frame?, "Import GPX", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name -> name.endsWith(".gpx", ignoreCase = true) }
    dialog.isVisible = true
    val directory = dialog.directory
    val fileName = dialog.file
    if (directory != null && fileName != null) {
        onPick(PlatformFileHandle(File(directory, fileName)))
    }
}
