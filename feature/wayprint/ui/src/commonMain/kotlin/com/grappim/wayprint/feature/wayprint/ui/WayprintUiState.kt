package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout

sealed interface WayprintUiState {
    data object Empty : WayprintUiState
    data object Loading : WayprintUiState
    data class Success(val layout: WayprintLayout) : WayprintUiState
    data class Error(val message: String) : WayprintUiState
}
