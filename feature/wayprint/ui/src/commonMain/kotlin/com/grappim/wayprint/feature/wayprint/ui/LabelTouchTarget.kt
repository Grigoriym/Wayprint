package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.feature.wayprint.domain.Rect
import com.grappim.wayprint.feature.wayprint.domain.TextAnchor

/**
 * A touch-friendly hit box for a label at ([x], [y]), built from its actual rendered
 * [textWidth]/[textHeight] — not `PlacedLabel.boundingBox`'s placement-estimate size, which is
 * only sized well enough for collision-avoidance spacing, not for a finger — padded up to at
 * least [minSize] square and centered on the label's visible glyph rather than its placement
 * anchor, so a label smaller than [minSize] gets a comfortably larger tap target without moving
 * where it was placed.
 */
fun labelTouchRect(
    x: Double,
    y: Double,
    anchor: TextAnchor,
    textWidth: Double,
    textHeight: Double,
    minSize: Double
): Rect {
    val (left, right) = when (anchor) {
        TextAnchor.START -> x to x + textWidth
        TextAnchor.MIDDLE -> x - textWidth / 2 to x + textWidth / 2
        TextAnchor.END -> x - textWidth to x
    }
    val centerX = (left + right) / 2
    val halfWidth = maxOf(textWidth, minSize) / 2
    val halfHeight = maxOf(textHeight, minSize) / 2
    return Rect(minX = centerX - halfWidth, minY = y - halfHeight, maxX = centerX + halfWidth, maxY = y + halfHeight)
}
