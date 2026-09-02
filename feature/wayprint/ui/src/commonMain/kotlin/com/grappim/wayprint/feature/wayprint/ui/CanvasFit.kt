package com.grappim.wayprint.feature.wayprint.ui

/**
 * Uniform scale factor plus centering offset that letterboxes a fixed [canvasWidth]x[canvasHeight]
 * space into an [availableWidth]x[availableHeight] container — matching SVG's default
 * `xMidYMid meet` viewBox behavior (see the Elbe reference's `<svg viewBox=...>`).
 */
data class CanvasFit(val scale: Double, val offsetX: Double, val offsetY: Double)

fun fitScale(canvasWidth: Double, canvasHeight: Double, availableWidth: Double, availableHeight: Double): CanvasFit {
    val scale = minOf(availableWidth / canvasWidth, availableHeight / canvasHeight)
    val offsetX = (availableWidth - canvasWidth * scale) / 2
    val offsetY = (availableHeight - canvasHeight * scale) / 2
    return CanvasFit(scale = scale, offsetX = offsetX, offsetY = offsetY)
}
