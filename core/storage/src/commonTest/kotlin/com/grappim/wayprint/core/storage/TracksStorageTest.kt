package com.grappim.wayprint.core.storage

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TracksStorageTest {

    private val storage = TracksStorage(Files.createTempDirectory("tracks-storage-test").toFile())

    private fun metadata(
        colorSchemeIndex: Int = 0,
        displayName: String = "Track",
        importedAtEpochMillis: Long = 0L,
        distanceKm: Double = 0.0
    ) = TrackMetadata(
        labels = listOf(
            SavedLabel(id = "start", text = "Start", x = 1.5, y = 2.5, anchor = "START"),
            SavedLabel(id = "finish", text = "Finish", x = 3.0, y = 4.0, anchor = "END")
        ),
        colorSchemeIndex = colorSchemeIndex,
        displayName = displayName,
        importedAtEpochMillis = importedAtEpochMillis,
        distanceKm = distanceKm
    )

    @Test
    fun `load returns null for an id that was never saved`() {
        assertNull(storage.load("missing"))
    }

    @Test
    fun `save then load round-trips the gpx bytes and metadata exactly`() {
        val gpxBytes = "<gpx><trk/></gpx>".toByteArray()
        val trackMetadata = metadata(colorSchemeIndex = 2, displayName = "Elbe route", distanceKm = 12.5)

        storage.save("track-1", gpxBytes, trackMetadata)
        val restored = requireNotNull(storage.load("track-1"))

        assertTrue(gpxBytes.contentEquals(restored.gpxBytes))
        assertEquals(trackMetadata, restored.metadata)
    }

    @Test
    fun `list returns every saved track's summary newest first`() {
        storage.save("older", "a".toByteArray(), metadata(importedAtEpochMillis = 1L, displayName = "Older"))
        storage.save("newer", "b".toByteArray(), metadata(importedAtEpochMillis = 2L, displayName = "Newer"))

        val summaries = storage.list()

        assertEquals(listOf("newer", "older"), summaries.map { it.id })
        assertEquals(listOf("Newer", "Older"), summaries.map { it.metadata.displayName })
    }

    @Test
    fun `delete removes only the targeted track`() {
        storage.save("keep", "keep".toByteArray(), metadata())
        storage.save("gone", "gone".toByteArray(), metadata())

        storage.delete("gone")

        assertNull(storage.load("gone"))
        assertTrue("keep".toByteArray().contentEquals(requireNotNull(storage.load("keep")).gpxBytes))
    }

    @Test
    fun `delete on an unknown id does not throw`() {
        storage.delete("missing")
    }
}
