package com.grappim.wayprint.core.storage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val GPX_FILE_NAME = "track.gpx"
private const val METADATA_FILE_NAME = "metadata.json"

/** A restored track: the raw GPX bytes it was built from, plus its [TrackMetadata]. */
class Track(val gpxBytes: ByteArray, val metadata: TrackMetadata)

/**
 * Persists every imported track (one per id, no cap) under [directory] — one subdirectory per
 * track id, each holding raw GPX bytes plus a small JSON sidecar for [TrackMetadata] — rather
 * than a database, per CLAUDE.md's "simplicity first". Takes a [File] directory rather than an
 * Android `Context` so it has no Android-specific dependency for a test to fake: a plain temp
 * directory exercises the real thing.
 */
class TracksStorage(private val directory: File) {
    private fun trackDirectory(id: String) = File(directory, id)

    fun save(id: String, gpxBytes: ByteArray, metadata: TrackMetadata) {
        val trackDirectory = trackDirectory(id)
        trackDirectory.mkdirs()
        File(trackDirectory, GPX_FILE_NAME).writeBytes(gpxBytes)
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

    fun list(): List<TrackSummary> {
        val trackDirectories = directory.listFiles()?.filter { it.isDirectory } ?: emptyList()
        return trackDirectories
            .mapNotNull { trackDirectory ->
                val metadataFile = File(trackDirectory, METADATA_FILE_NAME)
                if (!metadataFile.exists()) return@mapNotNull null
                val metadata = Json.decodeFromString<TrackMetadata>(metadataFile.readText())
                TrackSummary(id = trackDirectory.name, metadata = metadata)
            }
            .sortedByDescending { it.metadata.importedAtEpochMillis }
    }

    fun delete(id: String) {
        trackDirectory(id).deleteRecursively()
    }
}
