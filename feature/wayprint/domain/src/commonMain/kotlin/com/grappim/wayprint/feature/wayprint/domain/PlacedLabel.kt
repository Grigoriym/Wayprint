package com.grappim.wayprint.feature.wayprint.domain

/** A label placed at a resolved position, with its bounding box for future collision checks. */
data class PlacedLabel(val text: String, val x: Double, val y: Double, val anchor: TextAnchor, val boundingBox: Rect)
