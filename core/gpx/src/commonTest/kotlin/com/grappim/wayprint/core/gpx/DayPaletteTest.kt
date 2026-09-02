package com.grappim.wayprint.core.gpx

import kotlin.test.Test
import kotlin.test.assertEquals

class DayPaletteTest {

    @Test
    fun `dayPalette of 5 matches the Python reference`() {
        val expected = listOf("#cea573", "#88ce73", "#73c5ce", "#8773ce", "#ce73ad")

        assertEquals(expected, dayPalette(5))
    }
}
