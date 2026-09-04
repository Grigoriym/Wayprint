package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.dayPalette
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryPresetTest {

    @Test
    fun `STORY_PRESETS has story at index 0 and square at index 1`() {
        assertEquals(2, STORY_PRESETS.size)
        assertEquals(DEFAULT_STORY_PRESET, STORY_PRESETS[0])
        assertEquals(SQUARE_STORY_PRESET, STORY_PRESETS[1])
    }

    @Test
    fun `SQUARE_STORY_PRESET shares DEFAULT_STORY_PRESET's canvas width`() {
        assertEquals(DEFAULT_STORY_PRESET.canvasWidth, SQUARE_STORY_PRESET.canvasWidth)
        assertEquals(SQUARE_STORY_PRESET.canvasWidth, SQUARE_STORY_PRESET.canvasHeight)
    }

    @Test
    fun `pipeline places exactly 3 non-overlapping labels within the square canvas`() {
        val fixture =
            requireNotNull(object {}.javaClass.getResourceAsStream("/fixtures/04 Riesa - Meissen.gpx"))

        val layout = fixture.use { buildWayprintLayout(it.asSource().buffered(), preset = SQUARE_STORY_PRESET) }

        assertEquals(3, layout.labels.size)

        val canvasBounds = Rect(
            minX = 0.0,
            minY = 0.0,
            maxX = SQUARE_STORY_PRESET.canvasWidth,
            maxY = SQUARE_STORY_PRESET.canvasHeight
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
    fun `PRESET_COLOR_SCHEMES has 5 entries with fixed background and text colors`() {
        assertEquals(5, PRESET_COLOR_SCHEMES.size)
        assertEquals(1, PRESET_COLOR_SCHEMES.map { it.backgroundColor }.distinct().size)
        assertEquals(1, PRESET_COLOR_SCHEMES.map { it.textColor }.distinct().size)
    }

    @Test
    fun `PRESET_COLOR_SCHEMES line colors match dayPalette(5)`() {
        val expectedLineColors = dayPalette(5)

        assertEquals(expectedLineColors, PRESET_COLOR_SCHEMES.map { it.lineColor })
    }
}
