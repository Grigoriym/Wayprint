package com.grappim.wayprint.core.storage

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DraftStorageTest {

    private val storage = DraftStorage(Files.createTempDirectory("draft-storage-test").toFile())

    @Test
    fun `load returns null when no draft was ever saved`() {
        assertNull(storage.load())
    }

    @Test
    fun `save then load round-trips the gpx bytes and metadata exactly`() {
        val gpxBytes = "<gpx><trk/></gpx>".toByteArray()
        val metadata = DraftMetadata(
            labelPositions = listOf(LabelPosition(1.5, 2.5), LabelPosition(3.0, 4.0)),
            colorSchemeIndex = 2
        )

        storage.save(gpxBytes, metadata)
        val restored = requireNotNull(storage.load())

        assertTrue(gpxBytes.contentEquals(restored.gpxBytes))
        assertEquals(metadata, restored.metadata)
    }

    @Test
    fun `a second save overwrites the first draft rather than appending`() {
        storage.save("first".toByteArray(), DraftMetadata(emptyList(), colorSchemeIndex = 0))
        storage.save("second".toByteArray(), DraftMetadata(emptyList(), colorSchemeIndex = 1))

        val restored = requireNotNull(storage.load())

        assertTrue("second".toByteArray().contentEquals(restored.gpxBytes))
        assertEquals(1, restored.metadata.colorSchemeIndex)
    }

    @Test
    fun `save then clear then load returns null`() {
        storage.save("draft".toByteArray(), DraftMetadata(emptyList(), colorSchemeIndex = 0))

        storage.clear()

        assertNull(storage.load())
    }

    @Test
    fun `clear on an empty directory does not throw`() {
        storage.clear()
    }
}
