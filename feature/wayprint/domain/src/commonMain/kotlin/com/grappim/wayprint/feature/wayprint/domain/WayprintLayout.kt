package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.DEFAULT_RDP_EPSILON
import java.io.InputStream
import java.util.Locale

private const val LABEL_OFFSET = 24.0

/**
 * The route's projected path plus its 3 placed labels (Start, Finish, distance), in canvas
 * space — the shape M4 renders directly.
 */
data class WayprintLayout(
    val path: List<Pair<Double, Double>>,
    val totalDistanceKm: Double,
    val labels: List<PlacedLabel>
)

/**
 * Wires [buildWayprintRoute] and M3.2's greedy placer into one pipeline: the route's path is
 * translated from route-box-local space into [preset]'s canvas space (offset by
 * [StoryPreset.marginX]/[StoryPreset.marginY]), Start/Finish label anchors come from its first/
 * last points and the distance label's anchor from its bounding-box center, then all 3 are placed
 * in that fixed priority order against the full canvas bounds.
 */
fun buildWayprintLayout(
    input: InputStream,
    preset: StoryPreset = DEFAULT_STORY_PRESET,
    epsilon: Double = DEFAULT_RDP_EPSILON
): WayprintLayout {
    val route = buildWayprintRoute(input, preset, epsilon)
    val path = route.path.map { (x, y) -> (x + preset.marginX) to (y + preset.marginY) }

    val xs = path.map { it.first }
    val ys = path.map { it.second }
    val (startX, startY) = path.first()
    val (finishX, finishY) = path.last()
    val bboxCenterX = (xs.min() + xs.max()) / 2
    val bboxCenterY = (ys.min() + ys.max()) / 2

    val canvasBounds = Rect(minX = 0.0, minY = 0.0, maxX = preset.canvasWidth, maxY = preset.canvasHeight)
    val requests = listOf(
        LabelRequest(text = "Start", anchorX = startX, anchorY = startY, candidates = compassCandidates(LABEL_OFFSET)),
        LabelRequest(
            text = "Finish",
            anchorX = finishX,
            anchorY = finishY,
            candidates = compassCandidates(LABEL_OFFSET)
        ),
        LabelRequest(
            text = String.format(Locale.ROOT, "%.1f km", route.totalDistanceKm),
            anchorX = bboxCenterX,
            anchorY = bboxCenterY,
            candidates = compassCandidates(LABEL_OFFSET)
        )
    )

    return WayprintLayout(
        path = path,
        totalDistanceKm = route.totalDistanceKm,
        labels = placeLabels(requests, canvasBounds)
    )
}
