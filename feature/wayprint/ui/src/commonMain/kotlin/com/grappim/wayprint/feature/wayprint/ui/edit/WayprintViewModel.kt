package com.grappim.wayprint.feature.wayprint.ui.edit

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.core.storage.CombinedTrackMetadata
import com.grappim.wayprint.core.storage.TrackMetadata
import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.feature.wayprint.domain.STORY_PRESETS
import com.grappim.wayprint.feature.wayprint.domain.buildCombinedWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.placeNewLabel
import com.grappim.wayprint.feature.wayprint.ui.platform.ImageExporter
import com.grappim.wayprint.feature.wayprint.ui.toPlacedLabel
import com.grappim.wayprint.feature.wayprint.ui.toSavedLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Clock

/**
 * [trackId] arrives through `parametersOf` at the call site — Koin's `verify()` whitelists
 * `String` on its own, so [InjectedParam] is here for the compiler plugin, which would otherwise
 * look for a `String` definition in the graph. [tracksStorage]/[imageExporter] are both injected
 * — the platform-specific pieces (storage directory, gallery/share/save-as) live behind
 * [com.grappim.wayprint.core.storage.di.PlatformStorageModule]/[com.grappim.wayprint.feature.wayprint.ui.platform.PlatformUiModule].
 */
@KoinViewModel
class WayprintViewModel(
    @InjectedParam private val trackId: String,
    private val tracksStorage: TracksStorage,
    private val imageExporter: ImageExporter
) : ViewModel() {

    /** The raw bytes and stored metadata the current [WayprintUiState.layout] was built from. */
    private var loadedTrack: LoadedTrack? = null

    private val _uiState = MutableStateFlow(WayprintUiState())
    val uiState: StateFlow<WayprintUiState> = _uiState.asStateFlow()

    /** One-shot signal that [saveToGallery] finished, for the screen to show as a [Snackbar][androidx.compose.material3.Snackbar]. */
    private val _saveConfirmations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveConfirmations: SharedFlow<Unit> = _saveConfirmations.asSharedFlow()

    /** Whether this platform has a real share surface — the screen hides the Share action when false. */
    val supportsShare: Boolean = imageExporter.supportsShare

    init {
        loadTrack()
    }

    /**
     * Loads [trackId]'s persisted track, reapplying its label overrides/scheme onto a fresh
     * layout. [trackId] may name either a single track ([TracksStorage.load]) or a combined one
     * ([TracksStorage.loadCombined]) — on-disk shape decides, per [TracksStorage]'s own docs — so
     * a single track is tried first and a combined one only on a miss.
     */
    private fun loadTrack() {
        _uiState.value = WayprintUiState(isLoading = true)
        viewModelScope.launch {
            val restored = withContext(Dispatchers.IO) {
                val single = tracksStorage.load(trackId)
                if (single != null) {
                    runCatching {
                        val preset = STORY_PRESETS[single.metadata.storyPresetIndex]
                        val layout = buildWayprintLayout(Buffer().apply { write(single.gpxBytes) }, preset)
                        val labels = single.metadata.labels.map { it.toPlacedLabel() }
                        RestoredTrack(
                            loaded = LoadedTrack.Single(single.gpxBytes, single.metadata),
                            colorSchemeIndex = single.metadata.colorSchemeIndex,
                            storyPresetIndex = single.metadata.storyPresetIndex,
                            layout = EditableWayprintLayout.Single(layout.copy(labels = labels))
                        )
                    }.getOrNull()
                } else {
                    val combined = tracksStorage.loadCombined(trackId) ?: return@withContext null
                    runCatching {
                        val preset = STORY_PRESETS[combined.metadata.storyPresetIndex]
                        val inputs = combined.gpxBlobs.map { Buffer().apply { write(it) } }
                        val layout = buildCombinedWayprintLayout(inputs, preset)
                        val labels = combined.metadata.labels.map { it.toPlacedLabel() }
                        RestoredTrack(
                            loaded = LoadedTrack.Combined(combined.gpxBlobs, combined.metadata),
                            colorSchemeIndex = 0,
                            storyPresetIndex = combined.metadata.storyPresetIndex,
                            layout = EditableWayprintLayout.Combined(layout.copy(labels = labels))
                        )
                    }.getOrNull()
                }
            }
            if (restored == null) {
                _uiState.value = WayprintUiState(error = "Couldn't load that track")
                return@launch
            }
            loadedTrack = restored.loaded
            _uiState.value = WayprintUiState(
                layout = restored.layout,
                colorSchemeIndex = restored.colorSchemeIndex,
                storyPresetIndex = restored.storyPresetIndex
            )
        }
    }

    fun onLabelDragStart() {
        _uiState.update { it.dragStarted() }
    }

    fun onLabelDragged(index: Int, x: Double, y: Double) {
        _uiState.update { it.labelMoved(index, x, y) }
    }

    fun onLabelDragEnd() {
        _uiState.update { it.dragEnded() }
        persistTrack()
    }

    fun onColorSchemeSelected(index: Int) {
        _uiState.update { it.colorSchemeSelected(index) }
        persistTrack()
    }

    fun undo() {
        _uiState.update { it.undone() }
        persistTrack()
    }

    /** Adds a new freeform label (M10.3) at ([x], [y]) with [text], undoable like drag/color-scheme edits. */
    fun addLabel(x: Double, y: Double, text: String) {
        val id = Clock.System.now().toEpochMilliseconds().toString()
        _uiState.update { it.labelAdded(placeNewLabel(id = id, text = text, x = x, y = y)) }
        persistTrack()
    }

    /** Removes the freeform label [id] (M10.3), undoable like drag/color-scheme edits. */
    fun removeLabel(id: String) {
        _uiState.update { it.labelRemoved(id) }
        persistTrack()
    }

    /** Selects (or, with `null`, deselects) the label [id] for the delete affordance (M10.3). */
    fun onLabelSelected(id: String?) {
        _uiState.update { it.labelSelected(id) }
    }

    /** Saves the current layout/scheme back under [trackId], so a later force-kill can restore this exact state. */
    private fun persistTrack() {
        val layout = _uiState.value.layout ?: return
        when (val loaded = loadedTrack) {
            is LoadedTrack.Single -> viewModelScope.launch(Dispatchers.IO) {
                tracksStorage.save(
                    trackId,
                    loaded.gpxBytes,
                    loaded.metadata.copy(
                        labels = layout.labels.map { it.toSavedLabel() },
                        colorSchemeIndex = _uiState.value.colorSchemeIndex
                    )
                )
            }

            is LoadedTrack.Combined -> viewModelScope.launch(Dispatchers.IO) {
                tracksStorage.saveCombined(
                    trackId,
                    loaded.gpxBlobs,
                    loaded.metadata.copy(labels = layout.labels.map { it.toSavedLabel() })
                )
            }

            null -> Unit
        }
    }

    /** Persists [image] permanently via [ImageExporter.saveToGallery], then confirms via [saveConfirmations]. */
    fun saveToGallery(image: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            if (imageExporter.saveToGallery(image)) _saveConfirmations.emit(Unit)
        }
    }

    /** Hands [image] to [ImageExporter.share]. Only called when [ImageExporter.supportsShare] is true. */
    fun share(image: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            imageExporter.share(image)
        }
    }
}

/** The raw source bytes and stored metadata [WayprintViewModel.loadTrack] restored [WayprintUiState.layout] from. */
private sealed interface LoadedTrack {
    data class Single(val gpxBytes: ByteArray, val metadata: TrackMetadata) : LoadedTrack
    data class Combined(val gpxBlobs: List<ByteArray>, val metadata: CombinedTrackMetadata) : LoadedTrack
}

/** What [WayprintViewModel.loadTrack] read back for one track, before it becomes [WayprintUiState]. */
private data class RestoredTrack(
    val loaded: LoadedTrack,
    val colorSchemeIndex: Int,
    val storyPresetIndex: Int,
    val layout: EditableWayprintLayout
)
