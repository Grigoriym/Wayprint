package com.grappim.wayprint.feature.wayprint.ui.platform

/**
 * Provides [ImageExporter] — the one piece of `feature:wayprint:ui` that's genuinely
 * platform-specific and needs a real per-target constructor (Android's needs a `Context`,
 * Desktop's needs none). Lives under `WayprintUiModule`'s own `@ComponentScan` prefix, so no
 * `includes` entry is needed for it the way `core:storage`'s `PlatformStorageModule` needs one in
 * `composeApp`'s `AppModule` (different Gradle module, outside that scan's prefix).
 */
expect class PlatformUiModule
