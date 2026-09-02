package com.grappim.wayprint.composeapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.grappim.wayprint.feature.wayprint.domain.DEFAULT_STORY_PRESET
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.ui.WayprintCanvas
import com.grappim.wayprint.uikit.theme.WayprintTheme
import java.io.ByteArrayInputStream

@Composable
fun WayprintAppContent(modifier: Modifier = Modifier) {
    WayprintTheme {
        val layout = remember { buildWayprintLayout(ByteArrayInputStream(DEMO_GPX.toByteArray())) }
        WayprintCanvas(layout = layout, preset = DEFAULT_STORY_PRESET, modifier = modifier.fillMaxSize())
    }
}
