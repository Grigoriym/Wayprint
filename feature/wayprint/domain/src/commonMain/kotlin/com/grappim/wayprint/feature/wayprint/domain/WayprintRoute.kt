package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.DEFAULT_RDP_EPSILON
import com.grappim.wayprint.core.gpx.dayPalette
import com.grappim.wayprint.core.gpx.fitProjection
import com.grappim.wayprint.core.gpx.haversineKm
import com.grappim.wayprint.core.gpx.parseTrack
import com.grappim.wayprint.core.gpx.rdp
import kotlinx.io.Source

/** The route's projected, simplified path ready to draw, plus its total ridden distance. */
data class WayprintRoute(val path: List<Pair<Double, Double>>, val totalDistanceKm: Double)

/** One combined-image track's projected path, tagged with its `dayPalette` line color. */
data class ColoredPath(val path: List<Pair<Double, Double>>, val color: String)

/** N tracks' projected paths sharing one projection, plus their summed total distance. */
data class CombinedWayprintRoute(val tracks: List<ColoredPath>, val totalDistanceKm: Double)

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
    input: Source,
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

/**
 * Same pipeline as [buildWayprintRoute], for N [inputs] sharing one projection: `fitProjection`
 * fit over every track's simplified points concatenated (M11.1 confirmed the flat-list signature
 * already yields one shared bounding box/scale), then each track projected individually through
 * that instance and tagged with its `dayPalette(inputs.size)` color.
 */
fun buildCombinedWayprintRoute(
    inputs: List<Source>,
    preset: StoryPreset = DEFAULT_STORY_PRESET,
    epsilon: Double = DEFAULT_RDP_EPSILON
): CombinedWayprintRoute {
    val rawPointsPerTrack = inputs.map { parseTrack(it) }
    val totalDistanceKm = rawPointsPerTrack.sumOf { points -> points.zipWithNext(::haversineKm).sum() }
    val simplifiedPerTrack = rawPointsPerTrack.map { rdp(it, epsilon) }
    val projection = fitProjection(simplifiedPerTrack.flatten(), preset.routeBoxWidth, preset.routeBoxHeight)
    val colors = dayPalette(inputs.size)
    val tracks = simplifiedPerTrack.mapIndexed { index, simplified ->
        ColoredPath(path = simplified.map { projection.toSvg(it.lat, it.lon) }, color = colors[index])
    }
    return CombinedWayprintRoute(tracks = tracks, totalDistanceKm = totalDistanceKm)
}
