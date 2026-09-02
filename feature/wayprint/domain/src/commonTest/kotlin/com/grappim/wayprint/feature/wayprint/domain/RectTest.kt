package com.grappim.wayprint.feature.wayprint.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RectTest {

    private val base = Rect(minX = 0.0, minY = 0.0, maxX = 10.0, maxY = 10.0)

    @Test
    fun `disjoint rects don't overlap`() {
        val other = Rect(minX = 20.0, minY = 20.0, maxX = 30.0, maxY = 30.0)

        assertFalse(base.overlaps(other))
    }

    @Test
    fun `rects that only touch an edge don't overlap`() {
        val other = Rect(minX = 10.0, minY = 0.0, maxX = 20.0, maxY = 10.0)

        assertFalse(base.overlaps(other))
    }

    @Test
    fun `a fully contained rect overlaps`() {
        val other = Rect(minX = 2.0, minY = 2.0, maxX = 8.0, maxY = 8.0)

        assertTrue(base.overlaps(other))
    }

    @Test
    fun `a partially overlapping rect overlaps`() {
        val other = Rect(minX = 5.0, minY = 5.0, maxX = 15.0, maxY = 15.0)

        assertTrue(base.overlaps(other))
    }
}
