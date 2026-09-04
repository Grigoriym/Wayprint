package com.grappim.wayprint.composeapp.nav.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.wayprint.composeapp.PlatformFileHandle
import com.grappim.wayprint.core.navigation.Navigator
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintEditRoute
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintScreen
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsRoute
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsScreen

actual fun EntryProviderScope<NavKey>.wayprintEntry(
    navigator: Navigator,
    pendingImportUri: PlatformFileHandle?,
    clearPendingImport: () -> Unit
) {
    entry<RecentsRoute> {
        RecentsScreen(
            onTrackClick = { id -> navigator.navigate(WayprintEditRoute(id)) },
            pendingImportUri = pendingImportUri?.uri,
            clearPendingImport = clearPendingImport
        )
    }
    entry<WayprintEditRoute> { route ->
        WayprintScreen(trackId = route.trackId, onBackClick = { navigator.goBack() })
    }
}
