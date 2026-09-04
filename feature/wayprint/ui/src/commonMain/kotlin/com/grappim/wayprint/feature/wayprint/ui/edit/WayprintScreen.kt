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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.grappim.wayprint.feature.wayprint.domain.PRESET_COLOR_SCHEMES
import com.grappim.wayprint.feature.wayprint.domain.STORY_PRESETS
import com.grappim.wayprint.feature.wayprint.domain.StoryPreset
import com.grappim.wayprint.feature.wayprint.ui.CombinedWayprintCanvas
import com.grappim.wayprint.feature.wayprint.ui.WayprintCanvas
import com.grappim.wayprint.feature.wayprint.ui.parseHexColor
import com.grappim.wayprint.feature.wayprint.ui.renderCombinedWayprintStoryBitmap
import com.grappim.wayprint.feature.wayprint.ui.renderWayprintStoryBitmap
import kotlinx.coroutines.flow.collect
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

    var pendingSaveBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingAddPosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val requestSavePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingSaveBitmap?.let(viewModel::saveToGallery)
        pendingSaveBitmap = null
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.saveConfirmations.collect { snackbarHostState.showSnackbar("Saved to gallery") }
    }

    val layout = uiState.layout
    val error = uiState.error

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    val preset = STORY_PRESETS[uiState.storyPresetIndex]
                    when (layout) {
                        is EditableWayprintLayout.Single -> {
                            WayprintCanvas(
                                layout = layout.layout,
                                preset = preset,
                                colorScheme = PRESET_COLOR_SCHEMES[uiState.colorSchemeIndex],
                                modifier = Modifier.fillMaxSize(),
                                onDragStart = viewModel::onLabelDragStart,
                                onDrag = viewModel::onLabelDragged,
                                onDragEnd = viewModel::onLabelDragEnd,
                                onLabelTap = viewModel::onLabelSelected
                            )
                            ColorSchemeSwatches(
                                schemes = PRESET_COLOR_SCHEMES,
                                selectedIndex = uiState.colorSchemeIndex,
                                onSelect = viewModel::onColorSchemeSelected,
                                modifier = Modifier.align(Alignment.TopEnd).padding(SCREEN_PADDING)
                            )
                        }

                        is EditableWayprintLayout.Combined -> {
                            CombinedWayprintCanvas(
                                layout = layout.layout,
                                preset = preset,
                                modifier = Modifier.fillMaxSize(),
                                onDragStart = viewModel::onLabelDragStart,
                                onDrag = viewModel::onLabelDragged,
                                onDragEnd = viewModel::onLabelDragEnd,
                                onLabelTap = viewModel::onLabelSelected
                            )
                        }
                    }
                    FloatingActionButton(
                        onClick = { pendingAddPosition = preset.canvasWidth / 2 to preset.canvasHeight / 2 },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(SCREEN_PADDING)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add label")
                    }
                    if (uiState.canUndo) {
                        Button(
                            onClick = viewModel::undo,
                            modifier = Modifier.align(Alignment.TopStart).padding(SCREEN_PADDING)
                        ) {
                            Text("Undo")
                        }
                    }
                    uiState.selectedLabelId?.let { selectedLabelId ->
                        Button(
                            onClick = { viewModel.removeLabel(selectedLabelId) },
                            modifier = Modifier.align(Alignment.BottomStart).padding(SCREEN_PADDING)
                        ) {
                            Text("Delete label")
                        }
                    }
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(SCREEN_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(SCREEN_PADDING)
                    ) {
                        Button(
                            onClick = {
                                val bitmap = renderStoryBitmap(layout, preset, uiState.colorSchemeIndex)
                                val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) != PackageManager.PERMISSION_GRANTED
                                if (needsPermission) {
                                    pendingSaveBitmap = bitmap
                                    requestSavePermission.launch(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                } else {
                                    viewModel.saveToGallery(bitmap)
                                }
                            }
                        ) {
                            Text("Save")
                        }
                        Button(
                            onClick = {
                                viewModel.share(renderStoryBitmap(layout, preset, uiState.colorSchemeIndex))
                            }
                        ) {
                            Text("Share")
                        }
                    }
                    pendingAddPosition?.let { (x, y) ->
                        AddLabelDialog(
                            onConfirm = { text ->
                                viewModel.addLabel(x, y, text)
                                pendingAddPosition = null
                            },
                            onDismiss = { pendingAddPosition = null }
                        )
                    }
                }
            }
        }
    }
}

/** Renders [layout] to a bitmap for Save/Share, dispatching on which [EditableWayprintLayout] kind it is. */
private fun renderStoryBitmap(layout: EditableWayprintLayout, preset: StoryPreset, colorSchemeIndex: Int): Bitmap =
    when (layout) {
        is EditableWayprintLayout.Single -> renderWayprintStoryBitmap(
            layout.layout,
            preset,
            PRESET_COLOR_SCHEMES[colorSchemeIndex]
        )

        is EditableWayprintLayout.Combined -> renderCombinedWayprintStoryBitmap(layout.layout, preset)
    }

/** Prompts for a new freeform label's text (M10.3), confirming via [onConfirm] once it's non-blank. */
@Composable
private fun AddLabelDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Add label") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
