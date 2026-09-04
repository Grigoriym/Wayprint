package com.grappim.wayprint.composeapp.nav.entries

import android.net.Uri
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.wayprint.core.navigation.Navigator
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintEditRoute
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintScreen
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsRoute
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsScreen

/**
 * The one place that knows both a route and its screen — keeps `feature:wayprint:ui`'s two
 * screens from depending on each other, and is the only place their navigation callbacks are
 * wired, since those belong to the shell rather than to any ViewModel.
 *
 * [pendingImportUri]/[clearPendingImport] carry `MainActivity`'s share/view-intent `Uri` (M5.2) into
 * `RecentsScreen`'s own `TracksStorage`-backed import — see `WayprintAppContent`'s doc.
 */
fun EntryProviderScope<NavKey>.wayprintEntry(
    navigator: Navigator,
    pendingImportUri: Uri?,
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
