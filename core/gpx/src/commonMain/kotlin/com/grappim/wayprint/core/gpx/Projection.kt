package com.grappim.wayprint.core.gpx

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.cos

const val DEFAULT_BOX_WIDTH = 860.0
const val DEFAULT_BOX_HEIGHT = 980.0

/**
 * Equirectangular projection (x = lon*cos(meanLat), y = -lat) scaled uniformly (same factor on
 * both axes, so the true shape of the route isn't distorted) to fit a box. Ported from
 * `fit_projection()`/`to_svg()` in `gpx_route_art.py`.
 */
class Projection internal constructor(
    val meanLat: Double,
    val scale: Double,
    private val cosLat: Double,
    private val minX: Double,
    private val minY: Double
) {
    fun toSvg(lat: Double, lon: Double): Pair<Double, Double> {
        val x = lon * cosLat
        val y = -lat
        return roundToOneDecimal((x - minX) * scale) to roundToOneDecimal((y - minY) * scale)
    }
}

fun fitProjection(
    points: List<TrackPoint>,
    boxW: Double = DEFAULT_BOX_WIDTH,
    boxH: Double = DEFAULT_BOX_HEIGHT
): Projection {
    val meanLat = points.sumOf { it.lat } / points.size
    val cosLat = cos(meanLat * PI / 180)

    val xs = points.map { it.lon * cosLat }
    val ys = points.map { -it.lat }
    val minX = xs.min()
    val minY = ys.min()
    val scale = minOf(boxW / (xs.max() - minX), boxH / (ys.max() - minY))

    return Projection(meanLat, scale, cosLat, minX, minY)
}

// Matches Python's round(x, 1), which rounds half-to-even on the decimal value nearest to x's
// exact binary representation.
private fun roundToOneDecimal(value: Double): Double = BigDecimal(value).setScale(1, RoundingMode.HALF_EVEN).toDouble()
