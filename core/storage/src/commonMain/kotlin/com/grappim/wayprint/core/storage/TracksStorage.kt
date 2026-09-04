package com.grappim.wayprint.core.storage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val GPX_FILE_NAME = "track.gpx"
private const val METADATA_FILE_NAME = "metadata.json"

/** A restored track: the raw GPX bytes it was built from, plus its [TrackMetadata]. */
class Track(val gpxBytes: ByteArray, val metadata: TrackMetadata)

/** A restored combined track: each source track's raw GPX bytes, in combine order, plus [CombinedTrackMetadata]. */
class CombinedTrack(val gpxBlobs: List<ByteArray>, val metadata: CombinedTrackMetadata)

private fun combinedGpxFileName(index: Int) = "track-$index.gpx"

/**
 * Persists every imported track (one per id, no cap) under [directory] — one subdirectory per
 * track id, each holding raw GPX bytes plus a small JSON sidecar for [TrackMetadata] — rather
 * than a database, per CLAUDE.md's "simplicity first". Takes a [File] directory rather than an
 * Android `Context` so it has no Android-specific dependency for a test to fake: a plain temp
 * directory exercises the real thing.
 *
 * A combined track (M11) is a sibling shape in the same id space, distinguished on disk by its
 * files rather than a stored type tag: a single track's directory holds [GPX_FILE_NAME], a
 * combined track's holds `track-0.gpx`, `track-1.gpx`, ... — chosen over generalizing [Track] to
 * `gpxBlobs: List<ByteArray>` per CLAUDE.md's "surgical changes" (no existing single-track call
 * site needs to change).
 */
class TracksStorage(private val directory: File) {
    private fun trackDirectory(id: String) = File(directory, id)

    fun save(id: String, gpxBytes: ByteArray, metadata: TrackMetadata) {
        val trackDirectory = trackDirectory(id)
        trackDirectory.mkdirs()
        File(trackDirectory, GPX_FILE_NAME).writeBytes(gpxBytes)
        File(trackDirectory, METADATA_FILE_NAME).writeText(Json.encodeToString(metadata))
    }

    fun saveCombined(id: String, gpxBlobs: List<ByteArray>, metadata: CombinedTrackMetadata) {
        val trackDirectory = trackDirectory(id)
        trackDirectory.mkdirs()
        gpxBlobs.forEachIndexed { index, gpxBytes ->
            File(trackDirectory, combinedGpxFileName(index)).writeBytes(gpxBytes)
        }
        File(trackDirectory, METADATA_FILE_NAME).writeText(Json.encodeToString(metadata))
    }

    fun load(id: String): Track? {
        val trackDirectory = trackDirectory(id)
        val gpxFile = File(trackDirectory, GPX_FILE_NAME)
        val metadataFile = File(trackDirectory, METADATA_FILE_NAME)
        if (!gpxFile.exists() || !metadataFile.exists()) return null
        val metadata = Json.decodeFromString<TrackMetadata>(metadataFile.readText())
        return Track(gpxBytes = gpxFile.readBytes(), metadata = metadata)
    }

    fun loadCombined(id: String): CombinedTrack? {
        val trackDirectory = trackDirectory(id)
        val metadataFile = File(trackDirectory, METADATA_FILE_NAME)
        val gpxFiles = combinedGpxFiles(trackDirectory)
        if (gpxFiles.isEmpty() || !metadataFile.exists()) return null
        val metadata = Json.decodeFromString<CombinedTrackMetadata>(metadataFile.readText())
        return CombinedTrack(gpxBlobs = gpxFiles.map { it.readBytes() }, metadata = metadata)
    }

    fun list(): List<TrackListEntry> {
        val trackDirectories = directory.listFiles()?.filter { it.isDirectory } ?: emptyList()
        return trackDirectories
            .mapNotNull { trackDirectory -> entryFor(trackDirectory) }
            .sortedByDescending { it.importedAtEpochMillis }
    }

    private fun entryFor(trackDirectory: File): TrackListEntry? {
        val metadataFile = File(trackDirectory, METADATA_FILE_NAME)
        if (!metadataFile.exists()) return null
        val id = trackDirectory.name
        if (File(trackDirectory, GPX_FILE_NAME).exists()) {
            val metadata = Json.decodeFromString<TrackMetadata>(metadataFile.readText())
            return TrackListEntry.Single(id = id, metadata = metadata)
        }
        if (combinedGpxFiles(trackDirectory).isNotEmpty()) {
            val metadata = Json.decodeFromString<CombinedTrackMetadata>(metadataFile.readText())
            return TrackListEntry.Combined(id = id, metadata = metadata)
        }
        return null
    }

    private fun combinedGpxFiles(trackDirectory: File): List<File> = generateSequence(0) { it + 1 }
        .map { File(trackDirectory, combinedGpxFileName(it)) }
        .takeWhile { it.exists() }
        .toList()

    fun delete(id: String) {
        trackDirectory(id).deleteRecursively()
    }
}
