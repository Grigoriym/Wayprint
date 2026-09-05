package com.grappim.wayprint.composeapp

import androidx.compose.ui.window.ComposeUIViewController
import com.grappim.wayprint.composeapp.di.KoinApp
import org.koin.plugin.module.dsl.startKoin
import platform.UIKit.UIViewController

// PascalCase is required — this is the entry point Swift calls into.
@Suppress("unused", "FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController {
    startKoin<KoinApp> { }

    WayprintAppContent()
}
