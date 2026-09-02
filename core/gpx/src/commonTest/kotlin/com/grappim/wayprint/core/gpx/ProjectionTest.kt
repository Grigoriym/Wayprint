package com.grappim.wayprint.core.gpx

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Expected values captured by running `fit_projection()`/`to_svg()` from `gpx_route_art.py`
 * against the reference's own `TOWNS` coordinates, per CLAUDE.md's numeric-parity requirement.
 */
class ProjectionTest {

    private val towns = listOf(
        "Dessau" to TrackPoint(51.8395, 12.2489, 0.0),
        "Wittenberg" to TrackPoint(51.8663, 12.6455, 0.0),
        "Torgau" to TrackPoint(51.5595, 13.0058, 0.0),
        "Riesa" to TrackPoint(51.3072, 13.2939, 0.0),
        "Meissen" to TrackPoint(51.1623, 13.4736, 0.0),
        "Dresden" to TrackPoint(51.0504, 13.7373, 0.0)
    )

    @Test
    fun `projects the TOWNS coordinates matching the Python reference`() {
        val projection = fitProjection(towns.map { it.second }, 860.0, 980.0)

        assertEquals(51.464200000000005, projection.meanLat)
        assertEquals(927.4452816101941, projection.scale)

        val expected = mapOf(
            "Dessau" to (0.0 to 24.9),
            "Wittenberg" to (229.2 to 0.0),
            "Torgau" to (437.3 to 284.5),
            "Riesa" to (603.8 to 518.5),
            "Meissen" to (707.6 to 652.9),
            "Dresden" to (860.0 to 756.7)
        )

        for ((name, point) in towns) {
            assertEquals(expected.getValue(name), projection.toSvg(point.lat, point.lon), "town=$name")
        }
    }
}
