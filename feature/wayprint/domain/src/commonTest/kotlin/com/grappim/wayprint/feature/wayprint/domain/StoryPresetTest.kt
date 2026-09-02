package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.dayPalette
import kotlin.test.Test
import kotlin.test.assertEquals

class StoryPresetTest {

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
