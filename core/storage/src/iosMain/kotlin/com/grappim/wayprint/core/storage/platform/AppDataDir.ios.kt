package com.grappim.wayprint.core.storage.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * The app's Documents directory — sandboxed, durable, backed-up-by-default storage on iOS,
 * analogous to [appDataDir]'s per-user directory on JVM (`AppDataDir.jvm.kt`) and
 * `context.filesDir` on Android.
 */
@OptIn(ExperimentalForeignApi::class)
fun appDataDir(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(documentDirectory?.path) { "Couldn't resolve the Documents directory" }
}
