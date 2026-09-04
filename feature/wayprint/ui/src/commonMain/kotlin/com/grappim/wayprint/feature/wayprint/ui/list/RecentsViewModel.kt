package com.grappim.wayprint.feature.wayprint.ui.list

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.core.storage.CombinedTrackMetadata
import com.grappim.wayprint.core.storage.TrackListEntry
import com.grappim.wayprint.core.storage.TrackMetadata
import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.feature.wayprint.domain.buildCombinedWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.ui.toSavedLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Takes [Context] straight, same precedent as [com.grappim.wayprint.feature.wayprint.ui.edit.WayprintViewModel].
 */
@KoinViewModel
class RecentsViewModel(private val context: Context) : ViewModel() {

    private val tracksStorage = TracksStorage(context.filesDir)

    private val _uiState = MutableStateFlow(RecentsUiState())
    val uiState: StateFlow<RecentsUiState> = _uiState.asStateFlow()

    /** One-off, per `../wallosmobile` `CurrencyEditorViewModel`'s precedent: a successful import is a signal the screen navigates on, never UI state. */
    private val _imported = Channel<String>()
    val imported = _imported.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { tracksStorage.list().map { it.toUiItem() } }
            _uiState.update { it.copy(isLoading = false, tracks = items) }
        }
    }

    /** Parses [uri], saves it as a new track, and emits the new id via [imported] for the caller to navigate on. */
    fun importGpx(uri: Uri) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val gpxBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Couldn't open $uri")
                    val layout = ByteArrayInputStream(gpxBytes).use { buildWayprintLayout(it) }
                    val id = System.currentTimeMillis().toString()
                    tracksStorage.save(
                        id,
                        gpxBytes,
                        TrackMetadata(
                            labels = layout.labels.map { it.toSavedLabel() },
                            colorSchemeIndex = 0,
                            displayName = resolveDisplayName(uri),
                            importedAtEpochMillis = System.currentTimeMillis(),
                            distanceKm = layout.totalDistanceKm
                        )
                    )
                    id to tracksStorage.list().map { it.toUiItem() }
                }
            }
            result
                .onSuccess { (id, items) ->
                    _uiState.update { it.copy(isLoading = false, tracks = items) }
                    _imported.send(id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Couldn't read that file") }
                }
        }
    }

    fun deleteTrack(id: String) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                tracksStorage.delete(id)
                tracksStorage.list().map { it.toUiItem() }
            }
            _uiState.update { it.copy(tracks = items) }
        }
    }

    /** Enters multi-select (M11.4) with just [id] selected — long-press on a combinable row. */
    fun enterSelection(id: String) {
        _uiState.update { it.copy(selectedIds = listOf(id)) }
    }

    /** Toggles [id]'s selection; appended to the end so [RecentsUiState.selectedIds] stays selection-ordered. */
    fun toggleSelected(id: String) {
        _uiState.update { state ->
            val selected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
            state.copy(selectedIds = selected)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptyList()) }
    }

    /**
     * Builds a new combined track (M11) from the currently selected tracks' GPX bytes, in
     * selection order, and emits its id via [imported] like a fresh import. A no-op below 2
     * selected tracks — nothing to combine.
     */
    fun combineSelected() {
        val ids = _uiState.value.selectedIds
        if (ids.size < 2) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val tracks = ids.map { id -> tracksStorage.load(id) ?: error("Missing track $id") }
                    val gpxBlobs = tracks.map { it.gpxBytes }
                    val layout = buildCombinedWayprintLayout(gpxBlobs.map { ByteArrayInputStream(it) })
                    val id = System.currentTimeMillis().toString()
                    tracksStorage.saveCombined(
                        id,
                        gpxBlobs,
                        CombinedTrackMetadata(
                            labels = layout.labels.map { it.toSavedLabel() },
                            displayName = tracks.joinToString(" + ") { it.metadata.displayName },
                            importedAtEpochMillis = System.currentTimeMillis(),
                            distanceKm = layout.totalDistanceKm
                        )
                    )
                    id to tracksStorage.list().map { it.toUiItem() }
                }
            }
            result
                .onSuccess { (id, items) ->
                    _uiState.update { it.copy(isLoading = false, tracks = items, selectedIds = emptyList()) }
                    _imported.send(id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Couldn't combine those tracks") }
                }
        }
    }

    /** [OpenableColumns.DISPLAY_NAME] on [uri]; falls back for a share-intent `Uri` that has no such column. */
    private fun resolveDisplayName(uri: Uri): String {
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
        return name ?: "Untitled route"
    }

    private fun TrackListEntry.toUiItem(): RecentTrackUiItem {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return RecentTrackUiItem(
            id = id,
            displayName = displayName,
            importedDate = dateFormat.format(Date(importedAtEpochMillis)),
            distanceLabel = String.format(Locale.ROOT, "%.1f km", distanceKm),
            isCombinable = this is TrackListEntry.Single
        )
    }
}
