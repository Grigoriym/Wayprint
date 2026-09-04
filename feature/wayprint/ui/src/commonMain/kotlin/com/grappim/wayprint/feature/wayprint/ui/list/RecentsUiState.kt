package com.grappim.wayprint.feature.wayprint.ui.list

/**
 * One list row, ready-to-display strings precomputed in [RecentsViewModel]. [isCombinable]
 * (M11.4) is `true` only for a single track ([com.grappim.wayprint.core.storage.TrackListEntry.Single]) —
 * combining an already-combined track isn't supported, so it's excluded from multi-select.
 */
data class RecentTrackUiItem(
    val id: String,
    val displayName: String,
    val importedDate: String,
    val distanceLabel: String,
    val isCombinable: Boolean
)

/** [selectedIds] is ordered by selection, not display order — [RecentsViewModel.combineSelected] combines in that order. */
data class RecentsUiState(
    val tracks: List<RecentTrackUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIds: List<String> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && error == null && tracks.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
