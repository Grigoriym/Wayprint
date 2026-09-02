package com.grappim.wayprint.feature.wayprint.domain

private const val CHAR_WIDTH = 7.0
private const val TEXT_HEIGHT = 14.0

/**
 * One candidate placement for a label: an offset from its anchor point, plus the [TextAnchor]
 * describing how the offset position relates to the resulting bounding box.
 */
data class LabelCandidate(val dx: Double, val dy: Double, val anchor: TextAnchor)

/** A label waiting to be placed: its text, fixed anchor point, and ordered candidates to try. */
data class LabelRequest(
    val text: String,
    val anchorX: Double,
    val anchorY: Double,
    val candidates: List<LabelCandidate>
)

/**
 * Compass-style candidate offsets from a label's anchor point, tried in this order: right, left,
 * above, below — matching how the hand-tuned Elbe labels were each nudged in one clear direction.
 */
fun compassCandidates(offset: Double): List<LabelCandidate> = listOf(
    LabelCandidate(dx = offset, dy = 0.0, anchor = TextAnchor.START),
    LabelCandidate(dx = -offset, dy = 0.0, anchor = TextAnchor.END),
    LabelCandidate(dx = 0.0, dy = -offset, anchor = TextAnchor.MIDDLE),
    LabelCandidate(dx = 0.0, dy = offset, anchor = TextAnchor.MIDDLE)
)

private fun estimatedSize(text: String): Pair<Double, Double> = (text.length * CHAR_WIDTH) to TEXT_HEIGHT

private fun boundingBox(x: Double, y: Double, anchor: TextAnchor, width: Double, height: Double): Rect {
    val halfHeight = height / 2
    val (left, right) = when (anchor) {
        TextAnchor.START -> x to x + width
        TextAnchor.MIDDLE -> x - width / 2 to x + width / 2
        TextAnchor.END -> x - width to x
    }
    return Rect(minX = left, minY = y - halfHeight, maxX = right, maxY = y + halfHeight)
}

private fun Rect.within(canvasBounds: Rect): Boolean =
    minX >= canvasBounds.minX && minY >= canvasBounds.minY && maxX <= canvasBounds.maxX && maxY <= canvasBounds.maxY

/**
 * Tries [LabelRequest.candidates] in order and returns the first whose bounding box clears every
 * box in [placed] and stays within [canvasBounds]; falls back to the last candidate, overlap
 * accepted, if none clear.
 */
fun placeLabel(request: LabelRequest, placed: List<Rect>, canvasBounds: Rect): PlacedLabel {
    val (width, height) = estimatedSize(request.text)
    request.candidates.forEachIndexed { index, candidate ->
        val x = request.anchorX + candidate.dx
        val y = request.anchorY + candidate.dy
        val box = boundingBox(x, y, candidate.anchor, width, height)
        val clears = box.within(canvasBounds) && placed.none { it.overlaps(box) }
        if (clears || index == request.candidates.lastIndex) {
            return PlacedLabel(text = request.text, x = x, y = y, anchor = candidate.anchor, boundingBox = box)
        }
    }
    error("LabelRequest.candidates must not be empty")
}

/** Places [requests] one at a time in priority order, each dodging every already-placed label. */
fun placeLabels(requests: List<LabelRequest>, canvasBounds: Rect): List<PlacedLabel> {
    val placed = mutableListOf<PlacedLabel>()
    for (request in requests) {
        placed += placeLabel(request, placed.map { it.boundingBox }, canvasBounds)
    }
    return placed
}

/** Repositions this label to ([x], [y]), recomputing its [PlacedLabel.boundingBox] in place. */
fun PlacedLabel.movedTo(x: Double, y: Double): PlacedLabel {
    val (width, height) = estimatedSize(text)
    return copy(x = x, y = y, boundingBox = boundingBox(x, y, anchor, width, height))
}
