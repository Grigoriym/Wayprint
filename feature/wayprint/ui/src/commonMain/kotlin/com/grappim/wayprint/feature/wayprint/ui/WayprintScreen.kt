package com.grappim.wayprint.feature.wayprint.ui

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

private val SCREEN_PADDING = 16.dp

@Composable
fun WayprintScreen(modifier: Modifier = Modifier, viewModel: WayprintViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val pickGpx = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.loadFromUri(uri)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = uiState) {
            is WayprintUiState.Empty -> Button(onClick = { pickGpx.launch("*/*") }) {
                Text("Import GPX")
            }

            is WayprintUiState.Loading -> CircularProgressIndicator()

            is WayprintUiState.Success -> WayprintCanvas(
                layout = state.layout,
                preset = DEFAULT_STORY_PRESET,
                modifier = Modifier.fillMaxSize()
            )

            is WayprintUiState.Error -> Column(
                modifier = Modifier.padding(SCREEN_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SCREEN_PADDING)
            ) {
                Text(state.message)
                Button(onClick = { pickGpx.launch("*/*") }) {
                    Text("Retry")
                }
            }
        }
    }
}
