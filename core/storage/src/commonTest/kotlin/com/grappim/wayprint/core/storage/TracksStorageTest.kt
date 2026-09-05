package com.grappim.wayprint.core.storage

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.writeString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TracksStorageTest {

    private val directory = Path(SystemTemporaryDirectory, "tracks-storage-test-${Random.nextInt()}").also {
        SystemFileSystem.createDirectories(it)
    }
    private val storage = TracksStorage(directory)

    private fun labels() = listOf(
        SavedLabel(id = "start", text = "Start", x = 1.5, y = 2.5, anchor = "START"),
        SavedLabel(id = "finish", text = "Finish", x = 3.0, y = 4.0, anchor = "END")
    )

    private fun metadata(
        colorSchemeIndex: Int = 0,
        displayName: String = "Track",
        importedAtEpochMillis: Long = 0L,
        distanceKm: Double = 0.0,
        storyPresetIndex: Int = 0
    ) = TrackMetadata(
        labels = labels(),
        colorSchemeIndex = colorSchemeIndex,
        displayName = displayName,
        importedAtEpochMillis = importedAtEpochMillis,
        distanceKm = distanceKm,
        storyPresetIndex = storyPresetIndex
    )

    private fun combinedMetadata(
        displayName: String = "Combined track",
        importedAtEpochMillis: Long = 0L,
        distanceKm: Double = 0.0,
        storyPresetIndex: Int = 0,
        trackNames: List<String> = displayName.split(" + ")
    ) = CombinedTrackMetadata(
        labels = labels(),
        displayName = displayName,
        importedAtEpochMillis = importedAtEpochMillis,
        distanceKm = distanceKm,
        storyPresetIndex = storyPresetIndex,
        trackNames = trackNames
    )

    private fun writeFile(path: Path, text: String) {
        SystemFileSystem.sink(path).buffered().use { it.writeString(text) }
    }

    @Test
    fun `load returns null for an id that was never saved`() {
        assertNull(storage.load("missing"))
    }

    @Test
    fun `save then load round-trips the gpx bytes and metadata exactly`() {
        val gpxBytes = "<gpx><trk/></gpx>".encodeToByteArray()
        val trackMetadata = metadata(colorSchemeIndex = 2, displayName = "Elbe route", distanceKm = 12.5)

        storage.save("track-1", gpxBytes, trackMetadata)
        val restored = requireNotNull(storage.load("track-1"))

        assertTrue(gpxBytes.contentEquals(restored.gpxBytes))
        assertEquals(trackMetadata, restored.metadata)
    }

    @Test
    fun `saveCombined then loadCombined round-trips every gpx blob in order and metadata exactly`() {
        val gpxBlobs = listOf("<gpx><trk>a</trk></gpx>".encodeToByteArray(), "<gpx><trk>b</trk></gpx>".encodeToByteArray())
        val trackMetadata = combinedMetadata(
            displayName = "Two days",
            distanceKm = 30.0,
            trackNames = listOf("morning.gpx", "evening.gpx")
        )

        storage.saveCombined("combined-1", gpxBlobs, trackMetadata)
        val restored = requireNotNull(storage.loadCombined("combined-1"))

        assertEquals(gpxBlobs.size, restored.gpxBlobs.size)
        gpxBlobs.forEachIndexed { index, blob -> assertTrue(blob.contentEquals(restored.gpxBlobs[index])) }
        assertEquals(trackMetadata, restored.metadata)
    }

    @Test
    fun `loadCombined returns null for a single track id and load returns null for a combined track id`() {
        storage.save("single", "solo".encodeToByteArray(), metadata())
        storage.saveCombined("combo", listOf("a".encodeToByteArray(), "b".encodeToByteArray()), combinedMetadata())

        assertNull(storage.loadCombined("single"))
        assertNull(storage.load("combo"))
    }

    @Test
    fun `list returns every saved track's summary newest first`() {
        storage.save("older", "a".encodeToByteArray(), metadata(importedAtEpochMillis = 1L, displayName = "Older"))
        storage.save("newer", "b".encodeToByteArray(), metadata(importedAtEpochMillis = 2L, displayName = "Newer"))

        val summaries = storage.list()

        assertEquals(listOf("newer", "older"), summaries.map { it.id })
        assertEquals(listOf("Newer", "Older"), summaries.map { it.displayName })
    }

    @Test
    fun `list surfaces single and combined tracks together newest first`() {
        storage.save("single", "a".encodeToByteArray(), metadata(importedAtEpochMillis = 1L, displayName = "Single"))
        storage.saveCombined(
            "combined",
            listOf("a".encodeToByteArray(), "b".encodeToByteArray()),
            combinedMetadata(importedAtEpochMillis = 2L, displayName = "Combined")
        )

        val entries = storage.list()

        assertEquals(listOf("combined", "single"), entries.map { it.id })
        assertIs<TrackListEntry.Combined>(entries[0])
        assertIs<TrackListEntry.Single>(entries[1])
    }

    @Test
    fun `delete removes only the targeted track`() {
        storage.save("keep", "keep".encodeToByteArray(), metadata())
        storage.save("gone", "gone".encodeToByteArray(), metadata())

        storage.delete("gone")

        assertNull(storage.load("gone"))
        assertTrue("keep".encodeToByteArray().contentEquals(requireNotNull(storage.load("keep")).gpxBytes))
    }

    @Test
    fun `delete on an unknown id does not throw`() {
        storage.delete("missing")
    }

    @Test
    fun `save then load round-trips a non-default storyPresetIndex`() {
        val trackMetadata = metadata(storyPresetIndex = 1)

        storage.save("track-1", "gpx".encodeToByteArray(), trackMetadata)

        assertEquals(1, requireNotNull(storage.load("track-1")).metadata.storyPresetIndex)
    }

    @Test
    fun `load defaults storyPresetIndex to 0 for metadata json saved before that field existed`() {
        val trackDirectory = Path(directory, "legacy-single").also { SystemFileSystem.createDirectories(it) }
        writeFile(Path(trackDirectory, "track.gpx"), "<gpx/>")
        writeFile(
            Path(trackDirectory, "metadata.json"),
            """{"labels":[],"colorSchemeIndex":0,"displayName":"Legacy","importedAtEpochMillis":0,"distanceKm":0.0}"""
        )

        val restored = requireNotNull(storage.load("legacy-single"))

        assertEquals(0, restored.metadata.storyPresetIndex)
    }

    @Test
    fun `loadCombined defaults storyPresetIndex to 0 for metadata json saved before that field existed`() {
        val trackDirectory = Path(directory, "legacy-combined").also { SystemFileSystem.createDirectories(it) }
        writeFile(Path(trackDirectory, "track-0.gpx"), "<gpx/>")
        writeFile(
            Path(trackDirectory, "metadata.json"),
            """{"labels":[],"displayName":"Legacy combined","importedAtEpochMillis":0,"distanceKm":0.0}"""
        )

        val restored = requireNotNull(storage.loadCombined("legacy-combined"))

        assertEquals(0, restored.metadata.storyPresetIndex)
    }

    @Test
    fun `loadCombined defaults trackNames to displayName split on the join separator when that field is absent`() {
        val trackDirectory = Path(directory, "legacy-combined-names").also { SystemFileSystem.createDirectories(it) }
        writeFile(Path(trackDirectory, "track-0.gpx"), "<gpx/>")
        writeFile(
            Path(trackDirectory, "metadata.json"),
            """{"labels":[],"displayName":"morning.gpx + evening.gpx","importedAtEpochMillis":0,"distanceKm":0.0}"""
        )

        val restored = requireNotNull(storage.loadCombined("legacy-combined-names"))

        assertEquals(listOf("morning.gpx", "evening.gpx"), restored.metadata.trackNames)
    }
}
