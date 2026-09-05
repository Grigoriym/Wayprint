package com.grappim.wayprint.feature.wayprint.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WayprintRouteTest {

    @Test
    fun `total distance on the fixture matches the Python reference's sum of haversine_km over raw points`() {
        val route = buildWayprintRoute(riesaMeissenFixtureSource())

        // Captured by running parse_track() then summing haversine_km() over consecutive raw
        // points from gpx_route_art.py on the same fixture file.
        assertEquals(26.15576342355938, route.totalDistanceKm, 1e-9)
    }

    @Test
    fun `combined route sums per-track distance and assigns one dayPalette color per track`() {
        val route = buildCombinedWayprintRoute(
            listOf(riesaMeissenFixtureSource(), riesaMeissenFixtureSource())
        )

        assertEquals(2, route.tracks.size)
        assertEquals(2 * 26.15576342355938, route.totalDistanceKm, 1e-9)
        assertTrue(
            route.tracks[0].color != route.tracks[1].color,
            "each track must get its own dayPalette color"
        )
    }
}
