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

/**
 * [selectedIds] is ordered by selection, not display order — `RecentsViewModel.combineSelected`
 * combines in that order. Selection order alone is an unreliable proxy for a multi-day trip's
 * real chronology (the Recents list itself sorts newest-imported-first), so [moveSelected] lets
 * the user fix that order up before combining, via a reorder step between selecting tracks and
 * confirming the combine.
 */
data class RecentsUiState(
    val tracks: List<RecentTrackUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIds: List<String> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && error == null && tracks.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()

    /** Moves [id] by [offset] positions within [selectedIds]; clamped to the list's bounds, a no-op if [id] isn't selected. */
    fun moveSelected(id: String, offset: Int): RecentsUiState {
        val index = selectedIds.indexOf(id)
        if (index == -1) return this
        val newIndex = (index + offset).coerceIn(selectedIds.indices)
        if (newIndex == index) return this
        val reordered = selectedIds.toMutableList().apply {
            removeAt(index)
            add(newIndex, id)
        }
        return copy(selectedIds = reordered)
    }
}
