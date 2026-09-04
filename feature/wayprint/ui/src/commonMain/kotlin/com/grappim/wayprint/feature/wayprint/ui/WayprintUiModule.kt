package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.feature.wayprint.ui.platform.PlatformUiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * A module class in another Gradle module is invisible to `composeApp`'s own `@Configuration`
 * discovery — `AppModule` has to list this in its `includes`, same as `../wallosmobile`'s
 * per-feature-module `@ComponentScan` pattern.
 *
 * [PlatformUiModule] (provides [com.grappim.wayprint.feature.wayprint.ui.platform.ImageExporter],
 * M15.7) is listed explicitly too, even though it sits under this class's own `@ComponentScan`
 * prefix — belt-and-suspenders over relying on the scan to also auto-discover a sibling
 * `@Configuration` class.
 */
@Module(includes = [PlatformUiModule::class])
@Configuration
@ComponentScan("com.grappim.wayprint.feature.wayprint.ui")
class WayprintUiModule
