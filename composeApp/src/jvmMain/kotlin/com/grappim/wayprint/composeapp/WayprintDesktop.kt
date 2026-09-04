package com.grappim.wayprint.composeapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.grappim.wayprint.composeapp.di.KoinApp
import org.koin.plugin.module.dsl.startKoin

fun main() {
    startKoin<KoinApp> { }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Wayprint",
            state = rememberWindowState(width = 480.dp, height = 854.dp)
        ) {
            WayprintAppContent(modifier = Modifier.fillMaxSize())
        }
    }
}
