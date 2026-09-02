package com.grappim.wayprint.core.gpx

import kotlin.math.abs
import kotlin.math.hypot

/**
 * Ramer-Douglas-Peucker simplification, ported from `rdp()` in `gpx_route_art.py`. Measures
 * perpendicular distance in raw lat/lon degrees rather than via [haversineKm], matching the
 * Python reference as written rather than "fixing" it (see `docs/CHECKLIST.md` M1.2). Any extra
 * fields (e.g. elevation) ride along unchanged on the points that survive.
 */
fun rdp(points: List<TrackPoint>, epsilon: Double): List<TrackPoint> {
    if (points.size < 3) return points

    var dMax = 0.0
    var index = 0
    for (i in 1 until points.size - 1) {
        val d = perpendicularDistance(points[i], points.first(), points.last())
        if (d > dMax) {
            dMax = d
            index = i
        }
    }

    return if (dMax > epsilon) {
        val left = rdp(points.subList(0, index + 1), epsilon)
        val right = rdp(points.subList(index, points.size), epsilon)
        left.dropLast(1) + right
    } else {
        listOf(points.first(), points.last())
    }
}

private fun perpendicularDistance(point: TrackPoint, start: TrackPoint, end: TrackPoint): Double {
    val x = point.lat
    val y = point.lon
    val x1 = start.lat
    val y1 = start.lon
    val x2 = end.lat
    val y2 = end.lon
    if (x1 == x2 && y1 == y2) {
        return hypot(x - x1, y - y1)
    }
    val numerator = abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1)
    val denominator = hypot(y2 - y1, x2 - x1)
    return numerator / denominator
}
