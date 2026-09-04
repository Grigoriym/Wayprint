package com.grappim.wayprint.core.storage.platform

import java.io.File

private const val APP_DIR_NAME = "Wayprint"

/**
 * Per-user, durable application-support directory for the desktop/JVM build — ported from
 * `../TaigaMobileNova/core/storage/src/jvmMain/.../platform/AppDataDir.jvm.kt`, replacing
 * `java.io.tmpdir`, which the OS can clear at any time.
 *
 * Not wired into any Gradle target yet — `jvmMain` only becomes a real source set once M15.8 adds
 * `jvm()` to `configureKmp()` (`core:storage`'s own build.gradle.kts still only targets Android
 * today), so this file compiles and is exercised for the first time then.
 */
fun appDataDir(): File {
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()
    val baseDir = when {
        osName.contains("win") -> File(System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming")
        osName.contains("mac") -> File("$userHome/Library/Application Support")
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$userHome/.local/share")
    }
    return File(baseDir, APP_DIR_NAME).apply { mkdirs() }
}
