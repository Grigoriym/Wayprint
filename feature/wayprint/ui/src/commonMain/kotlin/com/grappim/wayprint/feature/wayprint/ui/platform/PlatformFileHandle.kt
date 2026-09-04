package com.grappim.wayprint.feature.wayprint.ui.platform

/**
 * A platform file reference — a picked file (in-app picker, [rememberGpxPickerLauncher]) or a
 * share/view-intent handoff (`MainActivity`'s `Uri`, forwarded down through the shell's
 * navigation plumbing) — carried without the caller depending on any platform file API.
 *
 * Lives here rather than in `composeApp` (M5.2's original home) because [RecentsViewModel]
 * (`feature:wayprint:ui`) needs it as [importGpx][RecentsViewModel.importGpx]'s parameter type,
 * and `feature:wayprint:ui` cannot depend on `composeApp` (the dependency runs the other way).
 */
expect class PlatformFileHandle {
    /** The picked file's raw bytes. */
    fun readBytes(): ByteArray

    /** The picked file's display name (e.g. for [TrackMetadata.displayName][com.grappim.wayprint.core.storage.TrackMetadata]). */
    fun displayName(): String
}
