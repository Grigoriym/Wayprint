package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.DEFAULT_RDP_EPSILON
import com.grappim.wayprint.core.gpx.fitProjection
import com.grappim.wayprint.core.gpx.haversineKm
import com.grappim.wayprint.core.gpx.parseTrack
import com.grappim.wayprint.core.gpx.rdp
import java.io.InputStream

/** The route's projected, simplified path ready to draw, plus its total ridden distance. */
data class WayprintRoute(val path: List<Pair<Double, Double>>, val totalDistanceKm: Double)

/**
 * Parses [input] and builds a [WayprintRoute]: total distance summed with `haversineKm` over the
 * raw (pre-`rdp`) points — `rdp` declutters the drawn line, not the reported distance — plus the
 * same projected, simplified path core:gpx's `buildRouteArt` produces for the same input.
 *
 * Doesn't call `buildRouteArt` itself: it consumes [input] once internally, and the raw points
 * are needed here too for distance, so this replicates its parse -> rdp -> project sequence
 * directly via core:gpx's own exposed functions.
 */
fun buildWayprintRoute(
    input: InputStream,
    preset: StoryPreset = DEFAULT_STORY_PRESET,
    epsilon: Double = DEFAULT_RDP_EPSILON
): WayprintRoute {
    val rawPoints = parseTrack(input)
    val totalDistanceKm = rawPoints.zipWithNext(::haversineKm).sum()
    val simplified = rdp(rawPoints, epsilon)
    val projection = fitProjection(simplified, preset.routeBoxWidth, preset.routeBoxHeight)
    val path = simplified.map { projection.toSvg(it.lat, it.lon) }
    return WayprintRoute(path = path, totalDistanceKm = totalDistanceKm)
}
