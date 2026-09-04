package com.grappim.wayprint.feature.wayprint.ui.platform

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class PlatformUiModule {

    @Single
    fun provideImageExporter(): ImageExporter = ImageExporter()
}
