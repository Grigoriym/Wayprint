package com.grappim.wayprint.core.gpx

import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOLERANCE_KM = 1e-9

/**
 * Expected values captured by running `haversine_km()` from `gpx_route_art.py` against the
 * reference's own `TOWNS` coordinate pairs, per CLAUDE.md's numeric-parity requirement.
 */
class HaversineTest {

    @Test
    fun `distance between Dessau and Wittenberg matches the Python reference`() {
        val dessau = TrackPoint(51.8395, 12.2489, 0.0)
        val wittenberg = TrackPoint(51.8663, 12.6455, 0.0)

        assertEquals(27.402269533092426, haversineKm(dessau, wittenberg), TOLERANCE_KM)
    }

    @Test
    fun `distance between Riesa and Meissen matches the Python reference`() {
        val riesa = TrackPoint(51.3072, 13.2939, 0.0)
        val meissen = TrackPoint(51.1623, 13.4736, 0.0)

        assertEquals(20.399299957417778, haversineKm(riesa, meissen), TOLERANCE_KM)
    }

    @Test
    fun `distance between Torgau and Dresden matches the Python reference`() {
        val torgau = TrackPoint(51.5595, 13.0058, 0.0)
        val dresden = TrackPoint(51.0504, 13.7373, 0.0)

        assertEquals(76.09440163359226, haversineKm(torgau, dresden), TOLERANCE_KM)
    }
}
