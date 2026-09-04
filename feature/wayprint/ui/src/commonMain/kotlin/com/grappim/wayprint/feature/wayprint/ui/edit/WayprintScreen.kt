package com.grappim.wayprint.feature.wayprint.ui.edit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.grappim.wayprint.feature.wayprint.domain.ColorScheme
import com.grappim.wayprint.feature.wayprint.domain.DEFAULT_STORY_PRESET
import com.grappim.wayprint.feature.wayprint.domain.PRESET_COLOR_SCHEMES
import com.grappim.wayprint.feature.wayprint.ui.WayprintCanvas
import com.grappim.wayprint.feature.wayprint.ui.parseHexColor
import com.grappim.wayprint.feature.wayprint.ui.renderWayprintStoryBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val SCREEN_PADDING = 16.dp
private val SWATCH_SIZE = 32.dp
private val SWATCH_SPACING = 8.dp
private val SWATCH_BORDER_SELECTED = 3.dp
private val SWATCH_BORDER_UNSELECTED = 1.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WayprintScreen(
    trackId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WayprintViewModel = koinViewModel { parametersOf(trackId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wayprint") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                }

                layout != null -> Box(modifier = Modifier.fillMaxSize()) {
                    val colorScheme = PRESET_COLOR_SCHEMES[uiState.colorSchemeIndex]
                    WayprintCanvas(
                        layout = layout,
                        preset = DEFAULT_STORY_PRESET,
                        colorScheme = colorScheme,
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
                    ColorSchemeSwatches(
                        schemes = PRESET_COLOR_SCHEMES,
                        selectedIndex = uiState.colorSchemeIndex,
                        onSelect = viewModel::onColorSchemeSelected,
                        modifier = Modifier.align(Alignment.TopEnd).padding(SCREEN_PADDING)
                    )
                    Button(
                        onClick = {
                            val bitmap = renderWayprintStoryBitmap(
                                layout,
                                DEFAULT_STORY_PRESET,
                                colorScheme
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
            }
        }
    }
}

/** A row of tappable circular swatches, one per [schemes] entry, colored by its [ColorScheme.lineColor]. */
@Composable
private fun ColorSchemeSwatches(
    schemes: List<ColorScheme>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(SWATCH_SPACING)) {
        schemes.forEachIndexed { index, scheme ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(SWATCH_SIZE)
                    .clip(CircleShape)
                    .background(parseHexColor(scheme.lineColor))
                    .border(
                        width = if (selected) SWATCH_BORDER_SELECTED else SWATCH_BORDER_UNSELECTED,
                        color = if (selected) Color.Black else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onSelect(index) }
            )
        }
    }
}
