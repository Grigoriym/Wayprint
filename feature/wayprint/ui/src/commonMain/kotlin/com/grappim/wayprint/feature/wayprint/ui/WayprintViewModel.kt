package com.grappim.wayprint.feature.wayprint.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.core.storage.DraftMetadata
import com.grappim.wayprint.core.storage.DraftStorage
import com.grappim.wayprint.core.storage.LabelPosition
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.movedTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel
import java.io.ByteArrayInputStream

/**
 * Takes [Context] straight (Koin's `androidContext()` registers it), same precedent as M4.3/M5's
 * shared context: this app has only one KMP target today, so platform APIs go directly in
 * `commonMain` rather than behind an expect/actual.
 */
@KoinViewModel
class WayprintViewModel(private val context: Context) : ViewModel() {

    private val draftStorage = DraftStorage(context.filesDir)

    /** The raw bytes the current [WayprintUiState.layout] was built from — what a draft save persists. */
    private var draftGpxBytes: ByteArray? = null

    private val _uiState = MutableStateFlow(WayprintUiState())
    val uiState: StateFlow<WayprintUiState> = _uiState.asStateFlow()

    init {
        restoreDraft()
    }

    /** Loads a persisted draft (if any) on startup, reapplying its label overrides/scheme onto a fresh layout. */
    private fun restoreDraft() {
        viewModelScope.launch {
            val restored = withContext(Dispatchers.IO) {
                val draft = draftStorage.load() ?: return@withContext null
                runCatching {
                    val layout = ByteArrayInputStream(draft.gpxBytes).use { buildWayprintLayout(it) }
                    val labels = layout.labels.mapIndexed { index, label ->
                        draft.metadata.labelPositions.getOrNull(index)
                            ?.let { label.movedTo(it.x, it.y) }
                            ?: label
                    }
                    draft.gpxBytes to WayprintUiState(
                        layout = layout.copy(labels = labels),
                        colorSchemeIndex = draft.metadata.colorSchemeIndex
                    )
                }.getOrNull()
            } ?: return@launch
            draftGpxBytes = restored.first
            _uiState.value = restored.second
        }
    }

    fun loadFromUri(uri: Uri) {
        _uiState.value = WayprintUiState(isLoading = true)
        viewModelScope.launch {
            val (bytes, state) = withContext(Dispatchers.IO) {
                try {
                    val gpxBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Couldn't open $uri")
                    val layout = ByteArrayInputStream(gpxBytes).use { buildWayprintLayout(it) }
                    gpxBytes to WayprintUiState(layout = layout)
                } catch (e: Exception) {
                    null to WayprintUiState(error = e.message ?: "Couldn't read that file")
                }
            }
            if (bytes != null) draftGpxBytes = bytes
            _uiState.value = state
            if (bytes != null) persistDraft()
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
        persistDraft()
    }

    fun onColorSchemeSelected(index: Int) {
        _uiState.update { it.colorSchemeSelected(index) }
        persistDraft()
    }

    fun undo() {
        _uiState.update { it.undone() }
        persistDraft()
    }

    /** Saves the current layout/scheme as the draft, so a later force-kill can restore this exact state. */
    private fun persistDraft() {
        val bytes = draftGpxBytes ?: return
        val layout = _uiState.value.layout ?: return
        val metadata = DraftMetadata(
            labelPositions = layout.labels.map { LabelPosition(it.x, it.y) },
            colorSchemeIndex = _uiState.value.colorSchemeIndex
        )
        viewModelScope.launch(Dispatchers.IO) {
            draftStorage.save(bytes, metadata)
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
