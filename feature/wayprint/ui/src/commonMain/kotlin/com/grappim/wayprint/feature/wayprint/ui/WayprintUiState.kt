package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout

/**
 * Same flags-on-one-data-class shape as `../wallosmobile`'s `UiState`s (e.g.
 * `CurrenciesUiState`), not a sealed hierarchy — `Empty` is the all-defaults case (no
 * `isLoading`, no `layout`, no `error`).
 */
data class WayprintUiState(
    val isLoading: Boolean = false,
    val layout: WayprintLayout? = null,
    val error: String? = null
)
