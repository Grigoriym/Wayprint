package com.grappim.wayprint.core.gpx

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlin.test.Test
import kotlin.test.assertEquals

private const val REFERENCE_EPSILON = 0.0006

class RdpTest {

    @Test
    fun `simplifies a synthetic zigzag matching a hand-verified expectation`() {
        // A straight line from (0,0) to (10,0) with two intermediate points: (5,1) deviates 1
        // degree from the line, (7,3) deviates 3 degrees. At epsilon=2, (7,3) survives (3 > 2)
        // but (5,1) does not (its distance from the (0,0)-(7,3) sub-line is ~1.05 < 2). Expected
        // output hand-computed and cross-checked against `rdp()` run on the same input.
        val points = listOf(
            TrackPoint(0.0, 0.0, 10.0),
            TrackPoint(5.0, 1.0, 20.0),
            TrackPoint(7.0, 3.0, 30.0),
            TrackPoint(10.0, 0.0, 40.0)
        )

        val simplified = rdp(points, epsilon = 2.0)

        assertEquals(
            listOf(
                TrackPoint(0.0, 0.0, 10.0),
                TrackPoint(7.0, 3.0, 30.0),
                TrackPoint(10.0, 0.0, 40.0)
            ),
            simplified
        )
    }

    @Test
    fun `simplifies the fixture matching the Python reference point count`() {
        val fixture =
            requireNotNull(object {}.javaClass.getResourceAsStream("/fixtures/04 Riesa - Meissen.gpx"))
        val points = fixture.use { parseTrack(it.asSource().buffered()) }

        val simplified = rdp(points, REFERENCE_EPSILON)

        assertEquals(51, simplified.size)
        assertEquals(points.first(), simplified.first())
        assertEquals(points.last(), simplified.last())
    }
}
