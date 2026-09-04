package com.grappim.wayprint.feature.wayprint.ui.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.core.storage.LabelPosition
import com.grappim.wayprint.core.storage.TrackMetadata
import com.grappim.wayprint.core.storage.TracksStorage
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.movedTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.io.ByteArrayInputStream

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

    private val tracksStorage = TracksStorage(context.filesDir)

    /** The raw bytes and stored metadata the current [WayprintUiState.layout] was built from. */
    private var trackGpxBytes: ByteArray? = null
    private var trackMetadata: TrackMetadata? = null

    private val _uiState = MutableStateFlow(WayprintUiState())
    val uiState: StateFlow<WayprintUiState> = _uiState.asStateFlow()

    init {
        loadTrack()
    }

    /** Loads [trackId]'s persisted track, reapplying its label overrides/scheme onto a fresh layout. */
    private fun loadTrack() {
        _uiState.value = WayprintUiState(isLoading = true)
        viewModelScope.launch {
            val restored = withContext(Dispatchers.IO) {
                val track = tracksStorage.load(trackId) ?: return@withContext null
                runCatching {
                    val layout = ByteArrayInputStream(track.gpxBytes).use { buildWayprintLayout(it) }
                    val labels = layout.labels.mapIndexed { index, label ->
                        track.metadata.labelPositions.getOrNull(index)
                            ?.let { label.movedTo(it.x, it.y) }
                            ?: label
                    }
                    Triple(
                        track.gpxBytes,
                        track.metadata,
                        WayprintUiState(
                            layout = layout.copy(labels = labels),
                            colorSchemeIndex = track.metadata.colorSchemeIndex
                        )
                    )
                }.getOrNull()
            }
            if (restored == null) {
                _uiState.value = WayprintUiState(error = "Couldn't load that track")
                return@launch
            }
            val (gpxBytes, metadata, state) = restored
            trackGpxBytes = gpxBytes
            trackMetadata = metadata
            _uiState.value = state
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

    /** Saves the current layout/scheme back under [trackId], so a later force-kill can restore this exact state. */
    private fun persistTrack() {
        val bytes = trackGpxBytes ?: return
        val metadata = trackMetadata ?: return
        val layout = _uiState.value.layout ?: return
        viewModelScope.launch(Dispatchers.IO) {
            tracksStorage.save(
                trackId,
                bytes,
                metadata.copy(
                    labelPositions = layout.labels.map { LabelPosition(it.x, it.y) },
                    colorSchemeIndex = _uiState.value.colorSchemeIndex
                )
            )
        }
    }

    fun exportAndShare(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageUrl = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "wayprint-${System.currentTimeMillis()}",
                null
            ) ?: return@launch
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUrl))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
