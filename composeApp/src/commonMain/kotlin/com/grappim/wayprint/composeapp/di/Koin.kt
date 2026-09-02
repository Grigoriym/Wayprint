package com.grappim.wayprint.composeapp.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

/**
 * Platform-specific bindings (file picker / share-intent entry point, `MediaStore` access —
 * IMPLEMENTATION_PLAN.md §6). Empty for now: Android is the only actual, added when M5 needs one.
 */
expect class PlatformComponentModule

/**
 * `composeApp` is Android-only today, so `@ComponentScan` alone reaches every `@Single`/`@Module`
 * in this compilation with no `includes` list to maintain. A second target (iOS/Desktop, per root
 * CLAUDE.md's growth roadmap) needs an explicit `includes = [...]` here — TaigaMobileNova's
 * `KoinGraphTest` doc explains why the scan alone doesn't reach across an iOS Native compilation.
 */
@Module
@Configuration
@ComponentScan("com.grappim.wayprint.composeapp")
class AppModule

@KoinApplication
object KoinApp
