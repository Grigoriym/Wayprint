package com.grappim.wayprint.core.storage

import kotlinx.serialization.Serializable

/**
 * One persisted label: its identity, text, canvas-space position, and anchor. [anchor] is stored
 * as a plain string (matching `TextAnchor.name`) since `core:storage` has no dependency on
 * `feature:wayprint:domain`, the same pattern [TrackMetadata.colorSchemeIndex] already uses.
 */
@Serializable
data class SavedLabel(val id: String, val text: String, val x: Double, val y: Double, val anchor: String)

/** Everything about a track beyond its raw GPX bytes: labels, styling, and list-row info. */
@Serializable
data class TrackMetadata(
    val labels: List<SavedLabel>,
    val colorSchemeIndex: Int,
    val displayName: String,
    val importedAtEpochMillis: Long,
    val distanceKm: Double
)

/** A track's id plus its [TrackMetadata] — cheap to list, no GPX bytes. */
data class TrackSummary(val id: String, val metadata: TrackMetadata)
