package com.grappim.wayprint.feature.wayprint.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.grappim.wayprint.feature.wayprint.domain.DEFAULT_STORY_PRESET
import com.grappim.wayprint.feature.wayprint.domain.PRESET_COLOR_SCHEMES
import org.koin.compose.viewmodel.koinViewModel

private val SCREEN_PADDING = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WayprintScreen(modifier: Modifier = Modifier, viewModel: WayprintViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pickGpx = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.loadFromUri(uri)
    }

    var pendingExportBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val requestExportPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingExportBitmap?.let(viewModel::exportAndShare)
        pendingExportBitmap = null
    }

    val layout = uiState.layout
    val error = uiState.error

    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text("Wayprint") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
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
                        colorScheme = PRESET_COLOR_SCHEMES.first(),
                        modifier = Modifier.fillMaxSize(),
                        onDragStart = viewModel::onLabelDragStart,
                        onDrag = viewModel::onLabelDragged,
                        onDragEnd = viewModel::onLabelDragEnd
                    )
                    if (uiState.canUndo) {
                        Button(
                            onClick = viewModel::undo,
                            modifier = Modifier.align(Alignment.TopStart).padding(SCREEN_PADDING)
                        ) {
                            Text("Undo")
                        }
                    }
                    Button(
                        onClick = {
                            val bitmap = renderWayprintStoryBitmap(
                                layout,
                                DEFAULT_STORY_PRESET,
                                PRESET_COLOR_SCHEMES.first()
                            )
                            val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                pendingExportBitmap = bitmap
                                requestExportPermission.launch(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            } else {
                                viewModel.exportAndShare(bitmap)
                            }
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
}
