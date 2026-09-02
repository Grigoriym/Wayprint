package com.grappim.wayprint.composeapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grappim.wayprint.feature.wayprint.ui.WayprintScreen
import com.grappim.wayprint.uikit.theme.WayprintTheme

@Composable
fun WayprintAppContent(modifier: Modifier = Modifier) {
    WayprintTheme {
        WayprintScreen(modifier = modifier.fillMaxSize())
    }
}
