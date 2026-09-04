package com.grappim.wayprint.feature.wayprint.domain

/**
 * A label placed at a resolved position, with its bounding box for future collision checks.
 * [id] identifies this label across add/remove/persist, independent of its position in a list.
 */
data class PlacedLabel(
    val id: String,
    val text: String,
    val x: Double,
    val y: Double,
    val anchor: TextAnchor,
    val boundingBox: Rect
)
