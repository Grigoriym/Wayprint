package com.grappim.wayprint.composeapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.grappim.wayprint.composeapp.nav.WayprintNavHost
import com.grappim.wayprint.composeapp.nav.navSavedStateConfiguration
import com.grappim.wayprint.core.navigation.Navigator
import com.grappim.wayprint.core.navigation.rememberNavigationState
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsRoute
import com.grappim.wayprint.uikit.theme.WayprintTheme

/**
 * [pendingImportUri] is `MainActivity`'s share/view-intent file handle (M5.2), forwarded down to
 * `RecentsScreen` (the only screen that owns a `TracksStorage`-backed import) rather than
 * imported here — a share arriving while [WayprintEditRoute][com.grappim.wayprint.feature.wayprint.ui.edit.WayprintEditRoute]
 * is on screen must first land back on Recents, since that is where the shared route becomes a
 * saved track.
 */
@Composable
fun WayprintAppContent(
    modifier: Modifier = Modifier,
    pendingImportUri: PlatformFileHandle? = null,
    clearPendingImport: () -> Unit = {}
) {
    WayprintTheme {
        val navigationState = rememberNavigationState(
            startKey = RecentsRoute,
            topLevelKeys = setOf(RecentsRoute),
            configuration = navSavedStateConfiguration
        )
        val navigator = remember(navigationState) { Navigator(navigationState) }

        LaunchedEffect(pendingImportUri) {
            if (pendingImportUri != null) navigator.navigate(RecentsRoute)
        }

        WayprintNavHost(
            navigationState = navigationState,
            navigator = navigator,
            pendingImportUri = pendingImportUri,
            clearPendingImport = clearPendingImport,
            modifier = modifier.fillMaxSize()
        )
    }
}
