package com.grappim.wayprint.core.storage

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
 * than a database, per CLAUDE.md's "simplicity first". Takes a [Path] directory rather than an
 * Android `Context` so it has no Android-specific dependency for a test to fake: a plain temp
 * directory exercises the real thing.
 *
 * A combined track (M11) is a sibling shape in the same id space, distinguished on disk by its
 * files rather than a stored type tag: a single track's directory holds [GPX_FILE_NAME], a
 * combined track's holds `track-0.gpx`, `track-1.gpx`, ... — chosen over generalizing [Track] to
 * `gpxBlobs: List<ByteArray>` per CLAUDE.md's "surgical changes" (no existing single-track call
 * site needs to change).
 */
class TracksStorage(private val directory: Path) {
    private fun trackDirectory(id: String) = Path(directory, id)

    fun save(id: String, gpxBytes: ByteArray, metadata: TrackMetadata) {
        val trackDirectory = trackDirectory(id)
        SystemFileSystem.createDirectories(trackDirectory)
        writeBytes(Path(trackDirectory, GPX_FILE_NAME), gpxBytes)
        writeText(Path(trackDirectory, METADATA_FILE_NAME), Json.encodeToString(metadata))
    }

    fun saveCombined(id: String, gpxBlobs: List<ByteArray>, metadata: CombinedTrackMetadata) {
        val trackDirectory = trackDirectory(id)
        SystemFileSystem.createDirectories(trackDirectory)
        gpxBlobs.forEachIndexed { index, gpxBytes ->
            writeBytes(Path(trackDirectory, combinedGpxFileName(index)), gpxBytes)
        }
        writeText(Path(trackDirectory, METADATA_FILE_NAME), Json.encodeToString(metadata))
    }

    fun load(id: String): Track? {
        val trackDirectory = trackDirectory(id)
        val gpxFile = Path(trackDirectory, GPX_FILE_NAME)
        val metadataFile = Path(trackDirectory, METADATA_FILE_NAME)
        if (!SystemFileSystem.exists(gpxFile) || !SystemFileSystem.exists(metadataFile)) return null
        val metadata = Json.decodeFromString<TrackMetadata>(readText(metadataFile))
        return Track(gpxBytes = readBytes(gpxFile), metadata = metadata)
    }

    fun loadCombined(id: String): CombinedTrack? {
        val trackDirectory = trackDirectory(id)
        val metadataFile = Path(trackDirectory, METADATA_FILE_NAME)
        val gpxFiles = combinedGpxFiles(trackDirectory)
        if (gpxFiles.isEmpty() || !SystemFileSystem.exists(metadataFile)) return null
        val metadata = Json.decodeFromString<CombinedTrackMetadata>(readText(metadataFile))
        return CombinedTrack(gpxBlobs = gpxFiles.map { readBytes(it) }, metadata = metadata)
    }

    fun list(): List<TrackListEntry> {
        if (!SystemFileSystem.exists(directory)) return emptyList()
        val trackDirectories = SystemFileSystem.list(directory)
            .filter { SystemFileSystem.metadataOrNull(it)?.isDirectory == true }
        return trackDirectories
            .mapNotNull { trackDirectory -> entryFor(trackDirectory) }
            .sortedByDescending { it.importedAtEpochMillis }
    }

    private fun entryFor(trackDirectory: Path): TrackListEntry? {
        val metadataFile = Path(trackDirectory, METADATA_FILE_NAME)
        if (!SystemFileSystem.exists(metadataFile)) return null
        val id = trackDirectory.name
        if (SystemFileSystem.exists(Path(trackDirectory, GPX_FILE_NAME))) {
            val metadata = Json.decodeFromString<TrackMetadata>(readText(metadataFile))
            return TrackListEntry.Single(id = id, metadata = metadata)
        }
        if (combinedGpxFiles(trackDirectory).isNotEmpty()) {
            val metadata = Json.decodeFromString<CombinedTrackMetadata>(readText(metadataFile))
            return TrackListEntry.Combined(id = id, metadata = metadata)
        }
        return null
    }

    private fun combinedGpxFiles(trackDirectory: Path): List<Path> = generateSequence(0) { it + 1 }
        .map { Path(trackDirectory, combinedGpxFileName(it)) }
        .takeWhile { SystemFileSystem.exists(it) }
        .toList()

    fun delete(id: String) {
        deleteRecursively(trackDirectory(id))
    }
}

private fun writeBytes(path: Path, bytes: ByteArray) {
    SystemFileSystem.sink(path).buffered().use { it.write(bytes) }
}

private fun writeText(path: Path, text: String) {
    SystemFileSystem.sink(path).buffered().use { it.writeString(text) }
}

private fun readBytes(path: Path): ByteArray = SystemFileSystem.source(path).buffered().use { it.readByteArray() }

private fun readText(path: Path): String = SystemFileSystem.source(path).buffered().use { it.readString() }

private fun deleteRecursively(path: Path) {
    if (!SystemFileSystem.exists(path)) return
    if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
        SystemFileSystem.list(path).forEach { deleteRecursively(it) }
    }
    SystemFileSystem.delete(path)
}
