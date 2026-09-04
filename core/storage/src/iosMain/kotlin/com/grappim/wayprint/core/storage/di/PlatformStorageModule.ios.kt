package com.grappim.wayprint.core.storage.di

import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.core.storage.platform.appDataDir
import kotlinx.io.files.Path
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/** [providePath] is a separate binding from [provideTracksStorage] for the same `verify()` reason as the Android `actual`. */
@Module
@Configuration
actual class PlatformStorageModule {

    @Single
    fun providePath(): Path = Path(appDataDir(), "tracks")

    @Single
    fun provideTracksStorage(directory: Path): TracksStorage = TracksStorage(directory)
}
