package com.grappim.wayprint.feature.wayprint.domain

/**
 * Axis-aligned bounding box in the same SVG-space coordinates `core:gpx`'s `Projection.toSvg`
 * produces (y grows downward).
 */
data class Rect(val minX: Double, val minY: Double, val maxX: Double, val maxY: Double) {
    fun overlaps(other: Rect): Boolean =
        minX < other.maxX && maxX > other.minX && minY < other.maxY && maxY > other.minY

    fun contains(x: Double, y: Double): Boolean = x in minX..maxX && y in minY..maxY
}
