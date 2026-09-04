package com.grappim.wayprint.feature.wayprint.ui.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.core.storage.CombinedTrackMetadata
import com.grappim.wayprint.core.storage.TrackMetadata
import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.feature.wayprint.domain.STORY_PRESETS
import com.grappim.wayprint.feature.wayprint.domain.buildCombinedWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.placeNewLabel
import com.grappim.wayprint.feature.wayprint.ui.toPlacedLabel
import com.grappim.wayprint.feature.wayprint.ui.toSavedLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Takes [Context] straight (Koin's `androidContext()` registers it), same precedent as M4.3/M5's
 * shared context: this app has only one KMP target today, so platform APIs go directly in
 * `commonMain` rather than behind an expect/actual.
 *
 * [trackId] arrives through `parametersOf` at the call site — Koin's `verify()` whitelists
 * `String` on its own, so [InjectedParam] is here for the compiler plugin, which would otherwise
 * look for a `String` definition in the graph.
 */
@KoinViewModel
class WayprintViewModel(@InjectedParam private val trackId: String, private val context: Context) : ViewModel() {

    private val tracksStorage = TracksStorage(Path(context.filesDir.absolutePath))

    /** The raw bytes and stored metadata the current [WayprintUiState.layout] was built from. */
    private var loadedTrack: LoadedTrack? = null

    private val _uiState = MutableStateFlow(WayprintUiState())
    val uiState: StateFlow<WayprintUiState> = _uiState.asStateFlow()

    /** One-shot signal that [saveToGallery] finished, for the screen to show as a [Snackbar][androidx.compose.material3.Snackbar]. */
    private val _saveConfirmations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveConfirmations: SharedFlow<Unit> = _saveConfirmations.asSharedFlow()

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
                        val layout = ByteArrayInputStream(single.gpxBytes).asSource().buffered()
                            .use { buildWayprintLayout(it, preset) }
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
                        val inputs = combined.gpxBlobs.map { ByteArrayInputStream(it).asSource().buffered() }
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
        val id = System.currentTimeMillis().toString()
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

    /** Writes [bitmap] into the device gallery via `MediaStore`, a permanent save, then confirms via [saveConfirmations]. */
    fun saveToGallery(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "wayprint-${System.currentTimeMillis()}",
                null
            ) ?: return@launch
            _saveConfirmations.emit(Unit)
        }
    }

    /** Shares [bitmap] via a temp file under `cacheDir` and a [FileProvider] URI — no `MediaStore` write. */
    fun share(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesDir, "wayprint-${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
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
