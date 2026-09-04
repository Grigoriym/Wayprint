package com.grappim.wayprint.core.gpx

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Expected values captured by running `parse_track()` from `gpx_route_art.py` on the same
 * fixture file, per CLAUDE.md's numeric-parity requirement.
 */
class GpxParserTest {

    @Test
    fun `parses the fixture matching the Python reference`() {
        val fixture =
            requireNotNull(object {}.javaClass.getResourceAsStream("/fixtures/04 Riesa - Meissen.gpx"))

        val points = fixture.use { parseTrack(it.asSource().buffered()) }

        assertEquals(614, points.size)

        val first = points.first()
        assertEquals(51.305712, first.lat)
        assertEquals(13.307704, first.lon)
        assertEquals(97.0, first.ele)

        val last = points.last()
        assertEquals(51.161584, last.lat)
        assertEquals(13.47739, last.lon)
        assertEquals(105.0, last.ele)
    }
}
