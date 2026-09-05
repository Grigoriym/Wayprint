package com.grappim.wayprint.feature.wayprint.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.core.storage.CombinedTrackMetadata
import com.grappim.wayprint.core.storage.TrackListEntry
import com.grappim.wayprint.core.storage.TrackMetadata
import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.feature.wayprint.domain.STORY_PRESETS
import com.grappim.wayprint.feature.wayprint.domain.buildCombinedWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.ui.platform.PlatformFileHandle
import com.grappim.wayprint.feature.wayprint.ui.toSavedLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.Buffer
import org.koin.core.annotation.KoinViewModel
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Instant

/** [tracksStorage] is injected — its platform-resolved directory comes from [com.grappim.wayprint.core.storage.di.PlatformStorageModule]. */
@KoinViewModel
class RecentsViewModel(private val tracksStorage: TracksStorage) : ViewModel() {

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

    /** Reads [handle], saves it as a new track under [storyPresetIndex], and emits the new id via [imported] for the caller to navigate on. */
    fun importGpx(handle: PlatformFileHandle, storyPresetIndex: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val gpxBytes = handle.readBytes()
                    val preset = STORY_PRESETS[storyPresetIndex]
                    val layout = buildWayprintLayout(Buffer().apply { write(gpxBytes) }, preset)
                    val id = Clock.System.now().toEpochMilliseconds().toString()
                    tracksStorage.save(
                        id,
                        gpxBytes,
                        TrackMetadata(
                            labels = layout.labels.map { it.toSavedLabel() },
                            colorSchemeIndex = 0,
                            displayName = handle.displayName(),
                            importedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                            distanceKm = layout.totalDistanceKm,
                            storyPresetIndex = storyPresetIndex
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
     * Builds a new combined track (M11) under [storyPresetIndex] from the currently selected
     * tracks' GPX bytes, in selection order, and emits its id via [imported] like a fresh import.
     * A no-op below 2 selected tracks — nothing to combine.
     */
    fun combineSelected(storyPresetIndex: Int) {
        val ids = _uiState.value.selectedIds
        if (ids.size < 2) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val tracks = ids.map { id -> tracksStorage.load(id) ?: error("Missing track $id") }
                    val gpxBlobs = tracks.map { it.gpxBytes }
                    val preset = STORY_PRESETS[storyPresetIndex]
                    val inputs = gpxBlobs.map { Buffer().apply { write(it) } }
                    val layout = buildCombinedWayprintLayout(inputs, preset)
                    val id = Clock.System.now().toEpochMilliseconds().toString()
                    tracksStorage.saveCombined(
                        id,
                        gpxBlobs,
                        CombinedTrackMetadata(
                            labels = layout.labels.map { it.toSavedLabel() },
                            displayName = tracks.joinToString(" + ") { it.metadata.displayName },
                            importedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                            distanceKm = layout.totalDistanceKm,
                            storyPresetIndex = storyPresetIndex,
                            trackNames = tracks.map { it.metadata.displayName }
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

    private fun TrackListEntry.toUiItem(): RecentTrackUiItem = RecentTrackUiItem(
        id = id,
        displayName = displayName,
        importedDate = formatImportedDate(importedAtEpochMillis),
        distanceLabel = formatDistanceKm(distanceKm),
        isCombinable = this is TrackListEntry.Single,
        mergedTrackNames = if (this is TrackListEntry.Combined) metadata.trackNames else emptyList()
    )
}

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

/** e.g. "Mar 5, 2026" — no `java.text.SimpleDateFormat`/`Locale`, neither portable past the JVM. */
private fun formatImportedDate(epochMillis: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${MONTH_NAMES[date.month.ordinal]} ${date.day}, ${date.year}"
}

/** e.g. "12.3 km" — always non-negative here (an imported track's total distance), unlike domain's general-purpose formatter. */
private fun formatDistanceKm(distanceKm: Double): String {
    val tenths = round(distanceKm * DISTANCE_LABEL_SCALE).toLong()
    return "${tenths / DISTANCE_LABEL_SCALE}.${tenths % DISTANCE_LABEL_SCALE} km"
}

private const val DISTANCE_LABEL_SCALE = 10L
