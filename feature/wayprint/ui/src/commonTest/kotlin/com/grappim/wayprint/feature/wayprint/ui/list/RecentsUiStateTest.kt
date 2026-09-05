package com.grappim.wayprint.feature.wayprint.ui.list

import kotlin.test.Test
import kotlin.test.assertEquals

class RecentsUiStateTest {

    private val state = RecentsUiState(selectedIds = listOf("a", "b", "c"))

    @Test
    fun `moveSelected with offset -1 swaps the id one place earlier`() {
        val result = state.moveSelected("b", offset = -1)

        assertEquals(listOf("b", "a", "c"), result.selectedIds)
    }

    @Test
    fun `moveSelected with offset +1 swaps the id one place later`() {
        val result = state.moveSelected("b", offset = 1)

        assertEquals(listOf("a", "c", "b"), result.selectedIds)
    }

    @Test
    fun `moveSelected clamps at the front of the list instead of wrapping`() {
        val result = state.moveSelected("a", offset = -1)

        assertEquals(listOf("a", "b", "c"), result.selectedIds)
    }

    @Test
    fun `moveSelected clamps at the end of the list instead of wrapping`() {
        val result = state.moveSelected("c", offset = 1)

        assertEquals(listOf("a", "b", "c"), result.selectedIds)
    }

    @Test
    fun `moveSelected is a no-op for an id that isn't selected`() {
        val result = state.moveSelected("z", offset = -1)

        assertEquals(listOf("a", "b", "c"), result.selectedIds)
    }

    @Test
    fun `moveSelected by more than one position moves past intermediate ids`() {
        val result = state.moveSelected("a", offset = 2)

        assertEquals(listOf("b", "c", "a"), result.selectedIds)
    }
}
