package com.grappim.wayprint.feature.wayprint.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.wayprint.feature.wayprint.ui.platform.PlatformFileHandle
import com.grappim.wayprint.feature.wayprint.ui.platform.rememberGpxPickerLauncher
import org.koin.compose.viewmodel.koinViewModel

private val SCREEN_PADDING = 16.dp
private val ROW_VERTICAL_PADDING = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    pendingImportUri: PlatformFileHandle? = null,
    clearPendingImport: () -> Unit = {},
    viewModel: RecentsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var pendingTemplatePick by remember { mutableStateOf<TemplatePickTarget?>(null) }
    var isReordering by remember { mutableStateOf(false) }

    val pickGpx = rememberGpxPickerLauncher { handle -> pendingTemplatePick = TemplatePickTarget.Import(handle) }

    LaunchedEffect(Unit) {
        viewModel.imported.collect { id -> onTrackClick(id) }
    }

    LaunchedEffect(pendingImportUri) {
        if (pendingImportUri != null) {
            pendingTemplatePick = TemplatePickTarget.Import(pendingImportUri)
            clearPendingImport()
        }
    }

    val templatePick = pendingTemplatePick
    if (templatePick != null) {
        TemplatePickDialog(
            onSelect = { storyPresetIndex ->
                when (templatePick) {
                    is TemplatePickTarget.Import -> viewModel.importGpx(templatePick.handle, storyPresetIndex)
                    is TemplatePickTarget.Combine -> viewModel.combineSelected(storyPresetIndex)
                }
                pendingTemplatePick = null
            },
            onDismiss = { pendingTemplatePick = null }
        )
    }

    if (isReordering) {
        ReorderTracksDialog(
            tracks = uiState.selectedIds.mapNotNull { id -> uiState.tracks.find { it.id == id } },
            onMoveUp = viewModel::moveSelectedUp,
            onMoveDown = viewModel::moveSelectedDown,
            onConfirm = {
                isReordering = false
                pendingTemplatePick = TemplatePickTarget.Combine
            },
            onDismiss = { isReordering = false }
        )
    }

    val deleteId = pendingDeleteId
    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete track?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDeleteId = null
                    viewModel.deleteTrack(deleteId)
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isSelectionMode) {
                CenterAlignedTopAppBar(
                    title = { Text("${uiState.selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel selection")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isReordering = true },
                            enabled = uiState.selectedIds.size >= 2
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Combine")
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(title = { Text("Wayprint") })
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = pickGpx,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Import GPX") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val error = uiState.error
            when {
                uiState.isLoading -> CircularProgressIndicator()

                error != null -> Text(error, modifier = Modifier.padding(SCREEN_PADDING))

                uiState.isEmpty -> Text(
                    "No tracks yet — import a GPX to get started.",
                    modifier = Modifier.padding(SCREEN_PADDING)
                )

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.tracks, key = { it.id }) { item ->
                        RecentTrackRow(
                            item = item,
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = item.id in uiState.selectedIds,
                            onClick = {
                                when {
                                    uiState.isSelectionMode && item.isCombinable -> viewModel.toggleSelected(item.id)
                                    uiState.isSelectionMode -> Unit
                                    else -> onTrackClick(item.id)
                                }
                            },
                            onLongClick = { if (item.isCombinable) viewModel.enterSelection(item.id) },
                            onDeleteClick = { pendingDeleteId = item.id }
                        )
                    }
                }
            }
        }
    }
}

/**
 * What a [TemplatePickDialog] choice is for — a fresh [Import] (share-intent or file-picker
 * handle) or a [Combine] of the current selection. The picked `storyPresetIndex` (M12) is
 * threaded into [RecentsViewModel.importGpx]/[RecentsViewModel.combineSelected] once the user
 * answers.
 */
private sealed interface TemplatePickTarget {
    data class Import(val handle: PlatformFileHandle) : TemplatePickTarget
    data object Combine : TemplatePickTarget
}

/** Locks in the new track's canvas template (M12) — story or square — before it's ever saved. */
@Composable
private fun TemplatePickDialog(
    onSelect: (storyPresetIndex: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Choose a canvas shape") },
        text = {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(0) }
                        .padding(vertical = 8.dp)
                ) {
                    Text("Story")
                    Text("1080×1920, portrait — matches Instagram/social media story format")
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(1) }
                        .padding(vertical = 8.dp)
                ) {
                    Text("Square")
                    Text("1080×1080 — matches a square social media post")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Lets the user fix up the combine order before confirming: [tracks] arrives in
 * [RecentsUiState.selectedIds] order, which is just UI tap order and has no relation to a
 * multi-day trip's real chronology — left uncorrected, the combined image's global Start/Finish
 * labels (anchored to the first/last track in that order) can land in the middle of the route
 * instead of its actual ends. This dialog is where the user reorders before [onConfirm] proceeds
 * to the template picker and the actual combine.
 */
@Composable
private fun ReorderTracksDialog(
    tracks: List<RecentTrackUiItem>,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Order the tracks") },
        text = {
            Column {
                Text("This becomes the route's Start-to-Finish order.")
                tracks.forEachIndexed { index, track ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}. ${track.displayName}",
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { onMoveUp(track.id) }, enabled = index > 0) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                        }
                        IconButton(onClick = { onMoveDown(track.id) }, enabled = index < tracks.lastIndex) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Next") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * [isSelected]/checkbox only shows for a [RecentTrackUiItem.isCombinable] row — combining a
 * combined track isn't supported (M11.4). The leading content is weighted and every [Text] here
 * is capped to one line: a combined track's [RecentTrackUiItem.displayName] is every constituent
 * name joined with no length limit (`RecentsViewModel.combineSelected`), which otherwise pushes
 * the trailing delete [IconButton] outside the row's visible bounds — a `Row` doesn't shrink an
 * unweighted, unbounded-width child to make room for a sibling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentTrackRow(
    item: RecentTrackUiItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = SCREEN_PADDING, vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (isSelectionMode && item.isCombinable) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
            }
            Column {
                Text(item.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                item.mergedTrackNames.forEach { name ->
                    Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${item.importedDate} · ${item.distanceLabel}", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (!isSelectionMode) {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}
