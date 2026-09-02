package com.grappim.wayprint.feature.wayprint.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LabelPlacementTest {

    private val canvasBounds = Rect(minX = 0.0, minY = 0.0, maxX = 1000.0, maxY = 1000.0)

    @Test
    fun `labels far apart each place at their preferred candidate`() {
        val requests = listOf(
            LabelRequest(text = "AAAA", anchorX = 100.0, anchorY = 100.0, candidates = compassCandidates(10.0)),
            LabelRequest(text = "AAAA", anchorX = 800.0, anchorY = 800.0, candidates = compassCandidates(10.0))
        )

        val placed = placeLabels(requests, canvasBounds)

        assertEquals(TextAnchor.START, placed[0].anchor)
        assertEquals(110.0, placed[0].x)
        assertEquals(100.0, placed[0].y)
        assertEquals(TextAnchor.START, placed[1].anchor)
        assertEquals(810.0, placed[1].x)
        assertEquals(800.0, placed[1].y)
        assertFalse(placed[0].boundingBox.overlaps(placed[1].boundingBox))
    }

    @Test
    fun `lower-priority label falls back when its preferred candidate collides`() {
        val requests = listOf(
            LabelRequest(text = "AAAA", anchorX = 500.0, anchorY = 500.0, candidates = compassCandidates(10.0)),
            LabelRequest(text = "AAAA", anchorX = 505.0, anchorY = 500.0, candidates = compassCandidates(10.0))
        )

        val placed = placeLabels(requests, canvasBounds)

        // Higher-priority label gets its first (right) candidate.
        assertEquals(TextAnchor.START, placed[0].anchor)
        assertEquals(510.0, placed[0].x)

        // Lower-priority label's right candidate would overlap placed[0], so it falls back to
        // its second (left) candidate instead.
        assertEquals(TextAnchor.END, placed[1].anchor)
        assertEquals(495.0, placed[1].x)
        assertFalse(placed[0].boundingBox.overlaps(placed[1].boundingBox))
    }

    @Test
    fun `falls back to the last candidate rather than throwing when none clear`() {
        val tinyCanvas = Rect(minX = 0.0, minY = 0.0, maxX = 1.0, maxY = 1.0)
        val request = LabelRequest(text = "A", anchorX = 0.0, anchorY = 0.0, candidates = compassCandidates(5.0))

        val placed = placeLabel(request, placed = emptyList(), canvasBounds = tinyCanvas)

        val lastCandidate = request.candidates.last()
        assertEquals(lastCandidate.anchor, placed.anchor)
        assertEquals(lastCandidate.dx, placed.x)
        assertEquals(lastCandidate.dy, placed.y)
    }

    @Test
    fun `movedTo repositions a label and recomputes its bounding box around the new position`() {
        val request =
            LabelRequest(text = "AAAA", anchorX = 100.0, anchorY = 100.0, candidates = compassCandidates(10.0))
        val placed = placeLabel(request, placed = emptyList(), canvasBounds = canvasBounds)

        val moved = placed.movedTo(x = 300.0, y = 400.0)

        assertEquals(300.0, moved.x)
        assertEquals(400.0, moved.y)
        assertEquals(placed.anchor, moved.anchor)
        assertEquals(placed.text, moved.text)
        val expectedWidth = placed.boundingBox.maxX - placed.boundingBox.minX
        val expectedHeight = placed.boundingBox.maxY - placed.boundingBox.minY
        assertEquals(expectedWidth, moved.boundingBox.maxX - moved.boundingBox.minX)
        assertEquals(expectedHeight, moved.boundingBox.maxY - moved.boundingBox.minY)
    }
}
