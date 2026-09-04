package com.grappim.wayprint.composeapp.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.grappim.wayprint.composeapp.PlatformFileHandle
import com.grappim.wayprint.composeapp.nav.entries.wayprintEntry
import com.grappim.wayprint.core.navigation.NavigationState
import com.grappim.wayprint.core.navigation.Navigator
import com.grappim.wayprint.core.navigation.toEntries

/**
 * `../wallosmobile`'s `MainNavHost` shape, minus the drawer sections it has and this app doesn't
 * (M9's shared context: `topLevelKeys` is a single-element set here).
 */
@Composable
fun WayprintNavHost(
    navigationState: NavigationState,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    pendingImportUri: PlatformFileHandle? = null,
    clearPendingImport: () -> Unit = {}
) {
    val entryProvider = entryProvider {
        wayprintEntry(navigator, pendingImportUri, clearPendingImport)
    }

    NavDisplay(
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) togetherWith
                fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        popTransitionSpec = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) togetherWith
                fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
        },
        onBack = { navigator.goBack() },
        entries = navigationState.toEntries(entryProvider)
    )
}

private const val TRANSITION_DURATION_MS = 150
