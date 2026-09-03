package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.feature.wayprint.domain.TextAnchor
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelTouchTargetTest {

    @Test
    fun `text larger than minSize in both dimensions is not padded`() {
        val rect = labelTouchRect(
            x = 100.0,
            y = 200.0,
            anchor = TextAnchor.START,
            textWidth = 80.0,
            textHeight = 60.0,
            minSize = 48.0
        )

        assertEquals(100.0, rect.minX)
        assertEquals(180.0, rect.maxX)
        assertEquals(170.0, rect.minY)
        assertEquals(230.0, rect.maxY)
    }

    @Test
    fun `text smaller than minSize is padded up to a square minSize box`() {
        val rect = labelTouchRect(
            x = 100.0,
            y = 200.0,
            anchor = TextAnchor.START,
            textWidth = 20.0,
            textHeight = 14.0,
            minSize = 48.0
        )

        assertEquals(48.0, rect.maxX - rect.minX)
        assertEquals(48.0, rect.maxY - rect.minY)
        // centered on the text's own center (x + textWidth / 2 = 110), not the anchor point
        assertEquals(110.0, (rect.minX + rect.maxX) / 2)
        assertEquals(200.0, (rect.minY + rect.maxY) / 2)
    }

    @Test
    fun `anchor MIDDLE centers the text on x before padding`() {
        val rect = labelTouchRect(
            x = 100.0,
            y = 200.0,
            anchor = TextAnchor.MIDDLE,
            textWidth = 80.0,
            textHeight = 30.0,
            minSize = 48.0
        )

        assertEquals(60.0, rect.minX)
        assertEquals(140.0, rect.maxX)
    }

    @Test
    fun `anchor END extends the text to the left of x before padding`() {
        val rect = labelTouchRect(
            x = 100.0,
            y = 200.0,
            anchor = TextAnchor.END,
            textWidth = 80.0,
            textHeight = 30.0,
            minSize = 48.0
        )

        assertEquals(20.0, rect.minX)
        assertEquals(100.0, rect.maxX)
    }
}
