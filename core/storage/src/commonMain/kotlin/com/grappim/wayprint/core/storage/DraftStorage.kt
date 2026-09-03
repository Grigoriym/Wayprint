package com.grappim.wayprint.core.storage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val GPX_FILE_NAME = "wayprint-draft.gpx"
private const val METADATA_FILE_NAME = "wayprint-draft.json"

/** A restored draft: the raw GPX bytes it was built from, plus its [DraftMetadata]. */
class Draft(val gpxBytes: ByteArray, val metadata: DraftMetadata)

/**
 * Persists the single in-progress draft (this app edits one route at a time) as two plain files
 * in [directory] — raw GPX bytes plus a small JSON sidecar for [DraftMetadata] — rather than a
 * database, per CLAUDE.md's "simplicity first" for a single draft. Takes a [File] directory
 * rather than an Android `Context` so it has no Android-specific dependency for a test to fake:
 * a plain temp directory exercises the real thing.
 */
class DraftStorage(private val directory: File) {
    private val gpxFile = File(directory, GPX_FILE_NAME)
    private val metadataFile = File(directory, METADATA_FILE_NAME)

    fun save(gpxBytes: ByteArray, metadata: DraftMetadata) {
        gpxFile.writeBytes(gpxBytes)
        metadataFile.writeText(Json.encodeToString(metadata))
    }

    fun load(): Draft? {
        if (!gpxFile.exists() || !metadataFile.exists()) return null
        val metadata = Json.decodeFromString<DraftMetadata>(metadataFile.readText())
        return Draft(gpxBytes = gpxFile.readBytes(), metadata = metadata)
    }

    fun clear() {
        gpxFile.delete()
        metadataFile.delete()
    }
}
