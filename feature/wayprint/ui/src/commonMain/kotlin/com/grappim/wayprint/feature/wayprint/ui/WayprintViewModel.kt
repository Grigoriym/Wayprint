package com.grappim.wayprint.feature.wayprint.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.wayprint.feature.wayprint.domain.buildWayprintLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow<WayprintUiState>(WayprintUiState.Empty)
    val uiState: StateFlow<WayprintUiState> = _uiState.asStateFlow()

    fun loadFromUri(uri: Uri) {
        _uiState.value = WayprintUiState.Loading
        viewModelScope.launch {
            _uiState.value = withContext(Dispatchers.IO) {
                try {
                    val layout = context.contentResolver.openInputStream(uri)?.use { input ->
                        buildWayprintLayout(input)
                    } ?: error("Couldn't open $uri")
                    WayprintUiState.Success(layout)
                } catch (e: Exception) {
                    WayprintUiState.Error(e.message ?: "Couldn't read that file")
                }
            }
        }
    }
}
