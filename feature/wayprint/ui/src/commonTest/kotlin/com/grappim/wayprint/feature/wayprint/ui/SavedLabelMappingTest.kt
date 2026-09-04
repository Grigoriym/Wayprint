package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.core.storage.SavedLabel
import com.grappim.wayprint.feature.wayprint.domain.TextAnchor
import com.grappim.wayprint.feature.wayprint.domain.placeNewLabel
import kotlin.test.Test
import kotlin.test.assertEquals

class SavedLabelMappingTest {

    @Test
    fun `toSavedLabel carries id, text, position and anchor name`() {
        val label = placeNewLabel(id = "custom-1", text = "Camp", x = 250.0, y = 150.0, anchor = TextAnchor.END)

        val saved = label.toSavedLabel()

        assertEquals(SavedLabel(id = "custom-1", text = "Camp", x = 250.0, y = 150.0, anchor = "END"), saved)
    }

    @Test
    fun `toPlacedLabel round-trips a saved label back to a placed one`() {
        val saved = SavedLabel(id = "custom-1", text = "Camp", x = 250.0, y = 150.0, anchor = "END")

        val label = saved.toPlacedLabel()

        assertEquals("custom-1", label.id)
        assertEquals("Camp", label.text)
        assertEquals(250.0, label.x)
        assertEquals(150.0, label.y)
        assertEquals(TextAnchor.END, label.anchor)
    }
}
