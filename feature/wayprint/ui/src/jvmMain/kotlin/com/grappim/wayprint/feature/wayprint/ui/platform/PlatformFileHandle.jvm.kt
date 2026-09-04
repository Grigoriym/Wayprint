package com.grappim.wayprint.feature.wayprint.ui.platform

import java.io.File

/** Not wired into any Gradle target yet — see `ImageExporter.jvm.kt`'s doc. */
actual class PlatformFileHandle(val file: File) {

    actual fun readBytes(): ByteArray = file.readBytes()

    actual fun displayName(): String = file.name
}
