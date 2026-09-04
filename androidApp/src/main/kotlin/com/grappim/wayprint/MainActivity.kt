package com.grappim.wayprint

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import com.grappim.wayprint.composeapp.WayprintAppContent

/**
 * [pendingImportUri] carries a share/view-intent `Uri` (M5.2) into [WayprintAppContent], which
 * forwards it to `RecentsScreen`'s own `TracksStorage`-backed import (M9 replaced the single
 * `WayprintViewModel.loadFromUri` this used to call with the per-track import model).
 */
class MainActivity : ComponentActivity() {
    private var pendingImportUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        pendingImportUri = sharedOrViewedUri(intent)
        setContent {
            WayprintAppContent(
                pendingImportUri = pendingImportUri,
                clearPendingImport = { pendingImportUri = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingImportUri = sharedOrViewedUri(intent)
    }

    private fun sharedOrViewedUri(intent: Intent): Uri? = when (intent.action) {
        Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        Intent.ACTION_VIEW -> intent.data
        else -> null
    }
}
