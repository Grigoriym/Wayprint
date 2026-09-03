package com.grappim.wayprint.core.storage

import kotlinx.serialization.Serializable

/** One label's overridden position, in the same canvas-space units the layout it came from used. */
@Serializable
data class LabelPosition(val x: Double, val y: Double)

/** Everything about a draft beyond its raw GPX bytes: current label positions and color scheme. */
@Serializable
data class DraftMetadata(val labelPositions: List<LabelPosition>, val colorSchemeIndex: Int)
