package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.DEFAULT_RDP_EPSILON
import java.io.InputStream
import java.util.Locale

private const val LABEL_OFFSET = 24.0

/**
 * The route's projected path plus its placed labels, in canvas space — the shape M4's renderer
 * draws directly. No longer assumes exactly Start/Finish/distance (M10): [labels] is whatever
 * arbitrary set the caller placed, defaults or otherwise.
 */
data class WayprintLayout(
    val path: List<Pair<Double, Double>>,
    val totalDistanceKm: Double,
    val labels: List<PlacedLabel>
)

/**
 * The 3 default label requests generated fresh on import — Start/Finish anchor to [path]'s
 * first/last points, the distance label anchors to its bounding-box center — with fixed ids so
 * they stay identifiable across saves. Only the *initial* contents of an editable label set
 * (M10), not a fixed set [buildWayprintLayout] owns: a caller may place any other list of
 * [LabelRequest]s against the same [placeLabels]/canvas-bounds pipeline instead.
 */
fun defaultLabelRequests(path: List<Pair<Double, Double>>, totalDistanceKm: Double): List<LabelRequest> {
    val xs = path.map { it.first }
    val ys = path.map { it.second }
    val (startX, startY) = path.first()
    val (finishX, finishY) = path.last()
    val bboxCenterX = (xs.min() + xs.max()) / 2
    val bboxCenterY = (ys.min() + ys.max()) / 2

    return listOf(
        LabelRequest(
            id = "start",
            text = "Start",
            anchorX = startX,
            anchorY = startY,
            candidates = compassCandidates(LABEL_OFFSET)
        ),
        LabelRequest(
            id = "finish",
            text = "Finish",
            anchorX = finishX,
            anchorY = finishY,
            candidates = compassCandidates(LABEL_OFFSET)
        ),
        LabelRequest(
            id = "distance",
            text = String.format(Locale.ROOT, "%.1f km", totalDistanceKm),
            anchorX = bboxCenterX,
            anchorY = bboxCenterY,
            candidates = compassCandidates(LABEL_OFFSET)
        )
    )
}

/**
 * Wires [buildWayprintRoute] and M3.2's greedy placer into one pipeline: the route's path is
 * translated from route-box-local space into [preset]'s canvas space (offset by
 * [StoryPreset.marginX]/[StoryPreset.marginY]), then [defaultLabelRequests] are placed against
 * the full canvas bounds.
 */
fun buildWayprintLayout(
    input: InputStream,
    preset: StoryPreset = DEFAULT_STORY_PRESET,
    epsilon: Double = DEFAULT_RDP_EPSILON
): WayprintLayout {
    val route = buildWayprintRoute(input, preset, epsilon)
    val path = route.path.map { (x, y) -> (x + preset.marginX) to (y + preset.marginY) }
    val canvasBounds = Rect(minX = 0.0, minY = 0.0, maxX = preset.canvasWidth, maxY = preset.canvasHeight)

    return WayprintLayout(
        path = path,
        totalDistanceKm = route.totalDistanceKm,
        labels = placeLabels(defaultLabelRequests(path, route.totalDistanceKm), canvasBounds)
    )
}
