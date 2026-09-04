package com.grappim.wayprint.core.storage

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TracksStorageTest {

    private val storage = TracksStorage(Files.createTempDirectory("tracks-storage-test").toFile())

    private fun labels() = listOf(
        SavedLabel(id = "start", text = "Start", x = 1.5, y = 2.5, anchor = "START"),
        SavedLabel(id = "finish", text = "Finish", x = 3.0, y = 4.0, anchor = "END")
    )

    private fun metadata(
        colorSchemeIndex: Int = 0,
        displayName: String = "Track",
        importedAtEpochMillis: Long = 0L,
        distanceKm: Double = 0.0
    ) = TrackMetadata(
        labels = labels(),
        colorSchemeIndex = colorSchemeIndex,
        displayName = displayName,
        importedAtEpochMillis = importedAtEpochMillis,
        distanceKm = distanceKm
    )

    private fun combinedMetadata(
        displayName: String = "Combined track",
        importedAtEpochMillis: Long = 0L,
        distanceKm: Double = 0.0
    ) = CombinedTrackMetadata(
        labels = labels(),
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
    fun `saveCombined then loadCombined round-trips every gpx blob in order and metadata exactly`() {
        val gpxBlobs = listOf("<gpx><trk>a</trk></gpx>".toByteArray(), "<gpx><trk>b</trk></gpx>".toByteArray())
        val trackMetadata = combinedMetadata(displayName = "Two days", distanceKm = 30.0)

        storage.saveCombined("combined-1", gpxBlobs, trackMetadata)
        val restored = requireNotNull(storage.loadCombined("combined-1"))

        assertEquals(gpxBlobs.size, restored.gpxBlobs.size)
        gpxBlobs.forEachIndexed { index, blob -> assertTrue(blob.contentEquals(restored.gpxBlobs[index])) }
        assertEquals(trackMetadata, restored.metadata)
    }

    @Test
    fun `loadCombined returns null for a single track id and load returns null for a combined track id`() {
        storage.save("single", "solo".toByteArray(), metadata())
        storage.saveCombined("combo", listOf("a".toByteArray(), "b".toByteArray()), combinedMetadata())

        assertNull(storage.loadCombined("single"))
        assertNull(storage.load("combo"))
    }

    @Test
    fun `list returns every saved track's summary newest first`() {
        storage.save("older", "a".toByteArray(), metadata(importedAtEpochMillis = 1L, displayName = "Older"))
        storage.save("newer", "b".toByteArray(), metadata(importedAtEpochMillis = 2L, displayName = "Newer"))

        val summaries = storage.list()

        assertEquals(listOf("newer", "older"), summaries.map { it.id })
        assertEquals(listOf("Newer", "Older"), summaries.map { it.displayName })
    }

    @Test
    fun `list surfaces single and combined tracks together newest first`() {
        storage.save("single", "a".toByteArray(), metadata(importedAtEpochMillis = 1L, displayName = "Single"))
        storage.saveCombined(
            "combined",
            listOf("a".toByteArray(), "b".toByteArray()),
            combinedMetadata(importedAtEpochMillis = 2L, displayName = "Combined")
        )

        val entries = storage.list()

        assertEquals(listOf("combined", "single"), entries.map { it.id })
        assertIs<TrackListEntry.Combined>(entries[0])
        assertIs<TrackListEntry.Single>(entries[1])
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
