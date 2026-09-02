package com.grappim.wayprint.feature.wayprint.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel

/**
 * Takes [Context] straight (Koin's `androidContext()` registers it), same precedent as M4.3/M5's
 * shared context: this app has only one KMP target today, so platform APIs go directly in
 * `commonMain` rather than behind an expect/actual.
 */
@KoinViewModel
class WayprintViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(WayprintUiState())
    val uiState: StateFlow<WayprintUiState> = _uiState.asStateFlow()

    fun loadFromUri(uri: Uri) {
        _uiState.value = WayprintUiState(isLoading = true)
        viewModelScope.launch {
            _uiState.value = withContext(Dispatchers.IO) {
                try {
                    val layout = context.contentResolver.openInputStream(uri)?.use { input ->
                        buildWayprintLayout(input)
                    } ?: error("Couldn't open $uri")
                    WayprintUiState(layout = layout)
                } catch (e: Exception) {
                    WayprintUiState(error = e.message ?: "Couldn't read that file")
                }
            }
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
    }

    fun undo() {
        _uiState.update { it.undone() }
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
