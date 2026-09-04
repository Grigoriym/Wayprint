package com.grappim.wayprint.core.gpx

import kotlinx.io.Source

const val DEFAULT_RDP_EPSILON = 0.0006

/**
 * Wires parsing, RDP simplification, and projection into one pipeline, matching `build()`'s
 * parse -> rdp -> fit_projection -> to_svg call sequence in `gpx_route_art.py` for a single track.
 */
fun buildRouteArt(
    input: Source,
    epsilon: Double = DEFAULT_RDP_EPSILON,
    boxW: Double = DEFAULT_BOX_WIDTH,
    boxH: Double = DEFAULT_BOX_HEIGHT
): List<Pair<Double, Double>> {
    val simplified = rdp(parseTrack(input), epsilon)
    val projection = fitProjection(simplified, boxW, boxH)
    return simplified.map { projection.toSvg(it.lat, it.lon) }
}
