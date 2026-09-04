package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.DEFAULT_RDP_EPSILON
import kotlinx.io.Source
import kotlin.math.abs
import kotlin.math.floor

private const val LABEL_OFFSET = 24.0

// Portable replacement for `String.format(Locale.ROOT, "%.1f km", ...)`, which relies on
// java.util.Locale/BigDecimal HALF_UP formatting unavailable on Kotlin/Native. Rounds the
// magnitude half-up (ties away from zero, matching Java's %f) and reattaches the original sign,
// since Java's formatter prints e.g. "-0.0 km" for a small negative value that rounds to zero.
private fun formatOneDecimalKm(totalDistanceKm: Double): String {
    val magnitudeTenths = abs(totalDistanceKm) * 10.0
    val flr = floor(magnitudeTenths)
    val roundedTenths = (if (magnitudeTenths - flr >= 0.5) flr + 1.0 else flr).toLong()
    val sign = if (totalDistanceKm < 0.0) "-" else ""
    return "$sign${roundedTenths / 10}.${roundedTenths % 10} km"
}

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
 * A combined image's layout (M11): N [ColoredPath]s sharing one canvas/projection instead of
 * [WayprintLayout]'s single path, plus placed labels. M4's renderer draws one line per track.
 */
data class CombinedWayprintLayout(
    val tracks: List<ColoredPath>,
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
            text = formatOneDecimalKm(totalDistanceKm),
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
    input: Source,
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

/**
 * The 2 default label requests for a combined image: global Start (the first track's first
 * point) / Finish (the last track's last point) only — no distance label, no per-track
 * Start/Finish. Per M11's shared context: per-track labels are then something the user adds via
 * M10.3's `addLabel`, not auto-generated.
 */
fun defaultCombinedLabelRequests(tracks: List<List<Pair<Double, Double>>>): List<LabelRequest> {
    val (startX, startY) = tracks.first().first()
    val (finishX, finishY) = tracks.last().last()

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
        )
    )
}

/** [buildWayprintLayout]'s N-track equivalent: [buildCombinedWayprintRoute] wired to [defaultCombinedLabelRequests]. */
fun buildCombinedWayprintLayout(
    inputs: List<Source>,
    preset: StoryPreset = DEFAULT_STORY_PRESET,
    epsilon: Double = DEFAULT_RDP_EPSILON
): CombinedWayprintLayout {
    val route = buildCombinedWayprintRoute(inputs, preset, epsilon)
    val tracks = route.tracks.map { track ->
        track.copy(path = track.path.map { (x, y) -> (x + preset.marginX) to (y + preset.marginY) })
    }
    val canvasBounds = Rect(minX = 0.0, minY = 0.0, maxX = preset.canvasWidth, maxY = preset.canvasHeight)

    return CombinedWayprintLayout(
        tracks = tracks,
        totalDistanceKm = route.totalDistanceKm,
        labels = placeLabels(defaultCombinedLabelRequests(tracks.map { it.path }), canvasBounds)
    )
}
