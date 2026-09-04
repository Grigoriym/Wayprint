package com.grappim.wayprint.feature.wayprint.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultLabelRequestsTest {

    private val path = listOf(0.0 to 0.0, 10.0 to 10.0)

    private fun distanceLabelText(totalDistanceKm: Double) =
        defaultLabelRequests(path, totalDistanceKm).single { it.id == "distance" }.text

    // Expected values captured from `String.format(Locale.ROOT, "%.1f km", ...)` on the JVM —
    // the function this replaces (M15.3) — including the negative-sign-on-a-zero-magnitude edge
    // case Java's formatter produces (sign comes from the original value, not the rounded one).
    @Test
    fun `formats the common case to one decimal`() {
        assertEquals("26.2 km", distanceLabelText(26.15576342355938))
    }

    @Test
    fun `formats zero`() {
        assertEquals("0.0 km", distanceLabelText(0.0))
    }

    @Test
    fun `formats a negative distance, rounding the magnitude half-up away from zero`() {
        assertEquals("-2.4 km", distanceLabelText(-2.35))
        assertEquals("-2.5 km", distanceLabelText(-2.45))
    }

    @Test
    fun `keeps the negative sign when a small negative value rounds to zero magnitude`() {
        assertEquals("-0.0 km", distanceLabelText(-0.04))
    }

    @Test
    fun `formats a large distance`() {
        assertEquals("1234.6 km", distanceLabelText(1234.567))
    }
}
