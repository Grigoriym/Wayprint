package com.grappim.wayprint.feature.wayprint.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grappim.wayprint.feature.wayprint.domain.DEFAULT_STORY_PRESET
import org.koin.compose.viewmodel.koinViewModel

private const val TAG = "WayprintScreen"

private val SCREEN_PADDING = 16.dp

@Composable
fun WayprintScreen(modifier: Modifier = Modifier, viewModel: WayprintViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val pickGpx = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.loadFromUri(uri)
    }

    val layout = uiState.layout
    val error = uiState.error

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> CircularProgressIndicator()

            error != null -> Column(
                modifier = Modifier.padding(SCREEN_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SCREEN_PADDING)
            ) {
                Text(error)
                Button(onClick = { pickGpx.launch("*/*") }) {
                    Text("Retry")
                }
            }

            layout != null -> Box(modifier = Modifier.fillMaxSize()) {
                WayprintCanvas(
                    layout = layout,
                    preset = DEFAULT_STORY_PRESET,
                    modifier = Modifier.fillMaxSize()
                )
                Button(
                    onClick = {
                        val bitmap = renderWayprintStoryBitmap(layout, DEFAULT_STORY_PRESET)
                        Log.d(TAG, "Exported ${bitmap.width}x${bitmap.height} bitmap")
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(SCREEN_PADDING)
                ) {
                    Text("Export")
                }
            }

            else -> Button(onClick = { pickGpx.launch("*/*") }) {
                Text("Import GPX")
            }
        }
    }
}
