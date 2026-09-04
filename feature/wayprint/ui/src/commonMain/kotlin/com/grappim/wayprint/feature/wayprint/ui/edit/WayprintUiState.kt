package com.grappim.wayprint.feature.wayprint.ui.edit

import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.movedTo

/**
 * Same flags-on-one-data-class shape as `../wallosmobile`'s `UiState`s (e.g.
 * `CurrenciesUiState`), not a sealed hierarchy — `Empty` is the all-defaults case (no
 * `isLoading`, no `layout`, no `error`).
 */
data class WayprintUiState(
    val isLoading: Boolean = false,
    val layout: WayprintLayout? = null,
    val error: String? = null,
    val colorSchemeIndex: Int = 0,
    val undoStack: List<EditSnapshot> = emptyList(),
    val dragStartSnapshot: EditSnapshot? = null
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
}

/** One undoable edit's pre-change state: both the layout and the color-scheme selection. */
data class EditSnapshot(val layout: WayprintLayout, val colorSchemeIndex: Int)

/** Remembers the current layout/scheme as the pre-drag snapshot a drag gesture may later undo. */
fun WayprintUiState.dragStarted(): WayprintUiState {
    val current = layout ?: return this
    return copy(dragStartSnapshot = EditSnapshot(current, colorSchemeIndex))
}

/** Moves the label at [index] to ([x], [y]) for live feedback while a drag is in progress. */
fun WayprintUiState.labelMoved(index: Int, x: Double, y: Double): WayprintUiState {
    val current = layout ?: return this
    val labels = current.labels.toMutableList().apply { this[index] = this[index].movedTo(x, y) }
    return copy(layout = current.copy(labels = labels))
}

/** Ends a drag gesture, pushing the pre-drag snapshot captured by [dragStarted] onto the undo stack. */
fun WayprintUiState.dragEnded(): WayprintUiState {
    val preDrag = dragStartSnapshot ?: return this
    return copy(undoStack = undoStack + preDrag, dragStartSnapshot = null)
}

/** Selects color scheme [index], pushing the pre-change snapshot onto the undo stack. */
fun WayprintUiState.colorSchemeSelected(index: Int): WayprintUiState {
    val current = layout ?: return this
    return copy(undoStack = undoStack + EditSnapshot(current, colorSchemeIndex), colorSchemeIndex = index)
}

/** Pops the most recent undo-stack entry, restoring it as the current layout/scheme. */
fun WayprintUiState.undone(): WayprintUiState {
    val previous = undoStack.lastOrNull() ?: return this
    return copy(
        layout = previous.layout,
        colorSchemeIndex = previous.colorSchemeIndex,
        undoStack = undoStack.dropLast(1)
    )
}
