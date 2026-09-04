package com.grappim.wayprint.core.storage.di

import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.core.storage.platform.appDataDir
import kotlinx.io.files.Path
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Not wired into any Gradle target yet — see `platform/AppDataDir.jvm.kt`'s doc. [providePath] is
 * split out from [provideTracksStorage] for the same `verify()` reason as the Android `actual` —
 * see its doc.
 */
@Module
@Configuration
actual class PlatformStorageModule {

    @Single
    fun providePath(): Path = Path(appDataDir().absolutePath, "tracks")

    @Single
    fun provideTracksStorage(directory: Path): TracksStorage = TracksStorage(directory)
}
