package com.grappim.wayprint.core.storage

import kotlinx.serialization.Serializable

/** One label's overridden position, in the same canvas-space units the layout it came from used. */
@Serializable
data class LabelPosition(val x: Double, val y: Double)

/** Everything about a track beyond its raw GPX bytes: label positions, styling, and list-row info. */
@Serializable
data class TrackMetadata(
    val labelPositions: List<LabelPosition>,
    val colorSchemeIndex: Int,
    val displayName: String,
    val importedAtEpochMillis: Long,
    val distanceKm: Double
)

/** A track's id plus its [TrackMetadata] — cheap to list, no GPX bytes. */
data class TrackSummary(val id: String, val metadata: TrackMetadata)
