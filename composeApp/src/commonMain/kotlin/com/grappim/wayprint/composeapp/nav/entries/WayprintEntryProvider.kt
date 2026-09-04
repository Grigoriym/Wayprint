package com.grappim.wayprint.composeapp.nav.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.wayprint.core.navigation.Navigator
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintEditRoute
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintScreen
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsRoute
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsScreen
import com.grappim.wayprint.feature.wayprint.ui.platform.PlatformFileHandle

/**
 * The one place that knows both a route and its screen — keeps `feature:wayprint:ui`'s two
 * screens from depending on each other, and is the only place their navigation callbacks are
 * wired, since those belong to the shell rather than to any ViewModel.
 *
 * Plain `commonMain` again as of M15.7 — it was `expect`/`actual` per platform only because
 * `RecentsScreen`/`WayprintScreen` themselves were `androidMain`-only at the time (M15.6); now
 * that both are portable, there's nothing platform-specific left here to split on.
 *
 * [pendingImportUri]/[clearPendingImport] carry `MainActivity`'s share/view-intent file handle
 * (M5.2) into `RecentsScreen`'s own `TracksStorage`-backed import — see `WayprintAppContent`'s
 * doc.
 */
fun EntryProviderScope<NavKey>.wayprintEntry(
    navigator: Navigator,
    pendingImportUri: PlatformFileHandle?,
    clearPendingImport: () -> Unit
) {
    entry<RecentsRoute> {
        RecentsScreen(
            onTrackClick = { id -> navigator.navigate(WayprintEditRoute(id)) },
            pendingImportUri = pendingImportUri,
            clearPendingImport = clearPendingImport
        )
    }
    entry<WayprintEditRoute> { route ->
        WayprintScreen(trackId = route.trackId, onBackClick = { navigator.goBack() })
    }
}
