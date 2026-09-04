package com.grappim.wayprint.core.storage.di

import android.content.Context
import com.grappim.wayprint.core.storage.TracksStorage
import kotlinx.io.files.Path
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * [providePath] is a separate binding, not inlined into [provideTracksStorage], so Koin's
 * `verify()` (which resolves a factory-provided singleton by reflecting on the *returned type's*
 * own constructor, not the factory function's parameters — confirmed by its error naming
 * [TracksStorage]'s own `directory` constructor parameter even when the factory here took
 * `Context`) has a real `Path` definition to find.
 */
@Module
@Configuration
actual class PlatformStorageModule {

    @Single
    fun providePath(context: Context): Path = Path(context.filesDir.absolutePath)

    @Single
    fun provideTracksStorage(directory: Path): TracksStorage = TracksStorage(directory)
}
