package com.grappim.wayprint.composeapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.grappim.wayprint.composeapp.greeting.GreetingProvider
import com.grappim.wayprint.uikit.theme.WayprintTheme
import org.koin.compose.koinInject

// M0.2/M0.4 placeholder — M4 replaces this with the Canvas route-art renderer.
@Composable
fun WayprintAppContent(modifier: Modifier = Modifier, greetingProvider: GreetingProvider = koinInject()) {
    WayprintTheme {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = greetingProvider.greeting())
        }
    }
}
