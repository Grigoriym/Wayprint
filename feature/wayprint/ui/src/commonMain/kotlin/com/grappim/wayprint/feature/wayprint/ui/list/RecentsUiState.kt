package com.grappim.wayprint.feature.wayprint.ui.list

/**
 * One list row, ready-to-display strings precomputed in `RecentsViewModel` (`androidMain` since
 * M14, not linkable from this `commonMain` KDoc). [isCombinable]
 * (M11.4) is `true` only for a single track ([com.grappim.wayprint.core.storage.TrackListEntry.Single]) —
 * combining an already-combined track isn't supported, so it's excluded from multi-select.
 * [mergedTrackNames] is empty for a single track; for a combined one it's each constituent track's
 * own name, shown as its own row under the title (which otherwise only shows the joined
 * [displayName], truncated).
 */
data class RecentTrackUiItem(
    val id: String,
    val displayName: String,
    val importedDate: String,
    val distanceLabel: String,
    val isCombinable: Boolean,
    val mergedTrackNames: List<String> = emptyList()
)

/** [selectedIds] is ordered by selection, not display order — `RecentsViewModel.combineSelected` combines in that order. */
data class RecentsUiState(
    val tracks: List<RecentTrackUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIds: List<String> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && error == null && tracks.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
