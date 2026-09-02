package com.grappim.wayprint.feature.wayprint.ui

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

/**
 * A module class in another Gradle module is invisible to `composeApp`'s own `@Configuration`
 * discovery — `AppModule` has to list this in its `includes`, same as `../wallosmobile`'s
 * per-feature-module `@ComponentScan` pattern.
 */
@Module
@Configuration
@ComponentScan("com.grappim.wayprint.feature.wayprint.ui")
class WayprintUiModule
