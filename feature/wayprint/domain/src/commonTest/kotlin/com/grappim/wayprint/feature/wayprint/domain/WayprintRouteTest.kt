package com.grappim.wayprint.feature.wayprint.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class WayprintRouteTest {

    @Test
    fun `total distance on the fixture matches the Python reference's sum of haversine_km over raw points`() {
        val fixture =
            requireNotNull(object {}.javaClass.getResourceAsStream("/fixtures/04 Riesa - Meissen.gpx"))

        val route = fixture.use { buildWayprintRoute(it) }

        // Captured by running parse_track() then summing haversine_km() over consecutive raw
        // points from gpx_route_art.py on the same fixture file.
        assertEquals(26.15576342355938, route.totalDistanceKm, 1e-9)
    }
}
