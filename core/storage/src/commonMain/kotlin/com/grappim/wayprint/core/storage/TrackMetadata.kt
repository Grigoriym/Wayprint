package com.grappim.wayprint.core.storage

import kotlinx.serialization.Serializable

/**
 * One persisted label: its identity, text, canvas-space position, and anchor. [anchor] is stored
 * as a plain string (matching `TextAnchor.name`) since `core:storage` has no dependency on
 * `feature:wayprint:domain`, the same pattern [TrackMetadata.colorSchemeIndex] already uses.
 */
@Serializable
data class SavedLabel(val id: String, val text: String, val x: Double, val y: Double, val anchor: String)

/**
 * Everything about a track beyond its raw GPX bytes: labels, styling, and list-row info.
 * [storyPresetIndex] defaults to `0` (the story template) so metadata persisted before this field
 * existed still loads unchanged, matching [colorSchemeIndex]'s "index into a fixed list" shape.
 */
@Serializable
data class TrackMetadata(
    val labels: List<SavedLabel>,
    val colorSchemeIndex: Int,
    val displayName: String,
    val importedAtEpochMillis: Long,
    val distanceKm: Double,
    val storyPresetIndex: Int = 0
)

/**
 * Everything about a combined track beyond its raw GPX bytes. Mirrors [TrackMetadata] minus
 * [TrackMetadata.colorSchemeIndex]: a combined image's per-track line colors come from
 * `dayPalette(n)` at layout-build time (one hue per track), not a user-selected scheme.
 * [storyPresetIndex] defaults to `0` for the same pre-existing-metadata reason as [TrackMetadata].
 */
@Serializable
data class CombinedTrackMetadata(
    val labels: List<SavedLabel>,
    val displayName: String,
    val importedAtEpochMillis: Long,
    val distanceKm: Double,
    val storyPresetIndex: Int = 0
)

/** One row for [TracksStorage.list] — either kind of track, cheap to list, no GPX bytes. */
sealed interface TrackListEntry {
    val id: String
    val displayName: String
    val importedAtEpochMillis: Long
    val distanceKm: Double

    data class Single(override val id: String, val metadata: TrackMetadata) : TrackListEntry {
        override val displayName get() = metadata.displayName
        override val importedAtEpochMillis get() = metadata.importedAtEpochMillis
        override val distanceKm get() = metadata.distanceKm
    }

    data class Combined(override val id: String, val metadata: CombinedTrackMetadata) : TrackListEntry {
        override val displayName get() = metadata.displayName
        override val importedAtEpochMillis get() = metadata.importedAtEpochMillis
        override val distanceKm get() = metadata.distanceKm
    }
}
