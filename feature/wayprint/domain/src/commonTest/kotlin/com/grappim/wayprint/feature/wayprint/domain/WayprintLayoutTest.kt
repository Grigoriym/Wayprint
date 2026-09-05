package com.grappim.wayprint.feature.wayprint.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WayprintLayoutTest {

    @Test
    fun `pipeline places exactly 3 non-overlapping labels within the canvas`() {
        val layout = buildWayprintLayout(riesaMeissenFixtureSource())

        assertEquals(3, layout.labels.size)

        val canvasBounds = Rect(
            minX = 0.0,
            minY = 0.0,
            maxX = DEFAULT_STORY_PRESET.canvasWidth,
            maxY = DEFAULT_STORY_PRESET.canvasHeight
        )
        for (label in layout.labels) {
            val box = label.boundingBox
            assertTrue(
                box.minX >= canvasBounds.minX && box.minY >= canvasBounds.minY &&
                    box.maxX <= canvasBounds.maxX && box.maxY <= canvasBounds.maxY,
                "${label.text}'s bounding box $box must stay within the canvas $canvasBounds"
            )
        }

        for (i in layout.labels.indices) {
            for (j in i + 1 until layout.labels.size) {
                assertFalse(
                    layout.labels[i].boundingBox.overlaps(layout.labels[j].boundingBox),
                    "${layout.labels[i].text} and ${layout.labels[j].text} must not overlap"
                )
            }
        }
    }

    @Test
    fun `combined pipeline places exactly the 2 default Start-Finish labels no distance label`() {
        val layout = buildCombinedWayprintLayout(
            listOf(riesaMeissenFixtureSource(), riesaMeissenFixtureSource())
        )

        assertEquals(2, layout.tracks.size)
        assertEquals(2, layout.labels.size)
        assertEquals(setOf("start", "finish"), layout.labels.map { it.id }.toSet())

        val canvasBounds = Rect(
            minX = 0.0,
            minY = 0.0,
            maxX = DEFAULT_STORY_PRESET.canvasWidth,
            maxY = DEFAULT_STORY_PRESET.canvasHeight
        )
        for (label in layout.labels) {
            val box = label.boundingBox
            assertTrue(
                box.minX >= canvasBounds.minX && box.minY >= canvasBounds.minY &&
                    box.maxX <= canvasBounds.maxX && box.maxY <= canvasBounds.maxY,
                "${label.text}'s bounding box $box must stay within the canvas $canvasBounds"
            )
        }
        assertFalse(
            layout.labels[0].boundingBox.overlaps(layout.labels[1].boundingBox),
            "Start and Finish must not overlap"
        )
    }
}
