package com.grappim.wayprint.core.gpx

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0088

/**
 * Great-circle distance in km between two points, ported from `haversine_km()` in
 * `gpx_route_art.py`.
 */
fun haversineKm(a: TrackPoint, b: TrackPoint): Double {
    val p1 = a.lat.toRadians()
    val p2 = b.lat.toRadians()
    val dPhi = (b.lat - a.lat).toRadians()
    val dLambda = (b.lon - a.lon).toRadians()
    val sinDPhi = sin(dPhi / 2)
    val sinDLambda = sin(dLambda / 2)
    val x = sinDPhi * sinDPhi + cos(p1) * cos(p2) * sinDLambda * sinDLambda
    return 2 * EARTH_RADIUS_KM * asin(sqrt(x))
}

private fun Double.toRadians(): Double = this * PI / 180
