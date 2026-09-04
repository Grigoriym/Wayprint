package com.grappim.wayprint.composeapp.nav.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.wayprint.composeapp.PlatformFileHandle
import com.grappim.wayprint.core.navigation.Navigator

/**
 * The one place that knows both a route and its screen — keeps `feature:wayprint:ui`'s two
 * screens from depending on each other, and is the only place their navigation callbacks are
 * wired, since those belong to the shell rather than to any ViewModel.
 *
 * `actual` per platform because it wires the two screens directly: both still take a raw
 * platform file handle (`RecentsScreen`'s own `Uri` param, `feature:wayprint:ui`, is Android-only
 * until M15.7), so each platform's `actual` unwraps [PlatformFileHandle] itself.
 *
 * [pendingImportUri]/[clearPendingImport] carry `MainActivity`'s share/view-intent file handle
 * (M5.2) into `RecentsScreen`'s own `TracksStorage`-backed import — see `WayprintAppContent`'s
 * doc.
 */
expect fun EntryProviderScope<NavKey>.wayprintEntry(
    navigator: Navigator,
    pendingImportUri: PlatformFileHandle?,
    clearPendingImport: () -> Unit
)
