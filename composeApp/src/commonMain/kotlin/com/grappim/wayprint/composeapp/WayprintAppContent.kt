package com.grappim.wayprint.composeapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.grappim.wayprint.composeapp.greeting.GreetingProvider
import org.koin.compose.koinInject

// M0.2/M0.4 placeholder — M4 replaces this with the Canvas route-art renderer.
@Composable
fun WayprintAppContent(modifier: Modifier = Modifier, greetingProvider: GreetingProvider = koinInject()) {
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = greetingProvider.greeting())
            }
        }
    }
}
