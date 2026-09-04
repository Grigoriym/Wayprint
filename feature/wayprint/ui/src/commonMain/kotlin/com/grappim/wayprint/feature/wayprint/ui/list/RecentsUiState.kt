package com.grappim.wayprint.feature.wayprint.ui.list

/** One list row, ready-to-display strings precomputed in [RecentsViewModel]. */
data class RecentTrackUiItem(
    val id: String,
    val displayName: String,
    val importedDate: String,
    val distanceLabel: String
)

data class RecentsUiState(
    val tracks: List<RecentTrackUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && tracks.isEmpty()
}
