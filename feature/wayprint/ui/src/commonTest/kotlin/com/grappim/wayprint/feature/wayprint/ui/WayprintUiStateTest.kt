package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.feature.wayprint.domain.PlacedLabel
import com.grappim.wayprint.feature.wayprint.domain.Rect
import com.grappim.wayprint.feature.wayprint.domain.TextAnchor
import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WayprintUiStateTest {

    private val label = PlacedLabel(
        text = "Start",
        x = 100.0,
        y = 100.0,
        anchor = TextAnchor.START,
        boundingBox = Rect(minX = 100.0, minY = 93.0, maxX = 135.0, maxY = 107.0)
    )
    private val layout = WayprintLayout(path = emptyList(), totalDistanceKm = 1.0, labels = listOf(label))

    @Test
    fun `dragStarted remembers the current layout and scheme as the pre-drag snapshot`() {
        val state = WayprintUiState(layout = layout, colorSchemeIndex = 2).dragStarted()

        assertEquals(EditSnapshot(layout, colorSchemeIndex = 2), state.dragStartSnapshot)
    }

    @Test
    fun `labelMoved updates the dragged label's position live`() {
        val state = WayprintUiState(layout = layout).labelMoved(index = 0, x = 200.0, y = 300.0)

        assertEquals(200.0, state.layout?.labels?.get(0)?.x)
        assertEquals(300.0, state.layout?.labels?.get(0)?.y)
    }

    @Test
    fun `dragEnded pushes the pre-drag snapshot onto the undo stack and clears it`() {
        val dragged = WayprintUiState(layout = layout).dragStarted().labelMoved(index = 0, x = 200.0, y = 300.0)

        val state = dragged.dragEnded()

        assertEquals(listOf(EditSnapshot(layout, colorSchemeIndex = 0)), state.undoStack)
        assertTrue(state.canUndo)
        assertNull(state.dragStartSnapshot)
    }

    @Test
    fun `dragEnded without a pending drag is a no-op`() {
        val state = WayprintUiState(layout = layout).dragEnded()

        assertFalse(state.canUndo)
        assertNull(state.dragStartSnapshot)
    }

    @Test
    fun `undone restores the last pushed layout and pops the stack`() {
        val dragged = WayprintUiState(layout = layout).dragStarted().labelMoved(index = 0, x = 200.0, y = 300.0)
        val afterDrag = dragged.dragEnded()

        val state = afterDrag.undone()

        assertEquals(layout, state.layout)
        assertFalse(state.canUndo)
    }

    @Test
    fun `undone with an empty stack is a no-op`() {
        val state = WayprintUiState(layout = layout).undone()

        assertEquals(layout, state.layout)
        assertFalse(state.canUndo)
    }

    @Test
    fun `colorSchemeSelected pushes the pre-change snapshot and updates the index`() {
        val state = WayprintUiState(layout = layout, colorSchemeIndex = 0).colorSchemeSelected(3)

        assertEquals(3, state.colorSchemeIndex)
        assertEquals(listOf(EditSnapshot(layout, colorSchemeIndex = 0)), state.undoStack)
    }

    @Test
    fun `undone after a scheme change restores the previous scheme`() {
        val afterSelect = WayprintUiState(layout = layout, colorSchemeIndex = 0).colorSchemeSelected(3)

        val state = afterSelect.undone()

        assertEquals(0, state.colorSchemeIndex)
        assertFalse(state.canUndo)
    }
}
