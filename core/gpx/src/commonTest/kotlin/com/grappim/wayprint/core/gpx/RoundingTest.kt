package com.grappim.wayprint.core.gpx

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Boundary cases for [roundHalfEven] where `value * 10^scale` is itself an exact double (so the
 * tie is real, not an artifact of decimal-string display) — the cases `BigDecimal.HALF_EVEN`
 * (what this function replaces, per M15.1) cares about.
 */
class RoundingTest {

    @Test
    fun `rounds exact half-integer ties to the nearest even integer`() {
        assertEquals(2.0, roundHalfEven(2.5, 0))
        assertEquals(4.0, roundHalfEven(3.5, 0))
        assertEquals(0.0, roundHalfEven(0.5, 0))
        assertEquals(-2.0, roundHalfEven(-2.5, 0))
        assertEquals(-4.0, roundHalfEven(-3.5, 0))
    }

    @Test
    fun `rounds exact one-decimal ties to the nearest even tenth`() {
        assertEquals(0.2, roundHalfEven(0.25, 1))
        assertEquals(0.8, roundHalfEven(0.75, 1))
        assertEquals(1.2, roundHalfEven(1.25, 1))
        assertEquals(2.8, roundHalfEven(2.75, 1))
        assertEquals(-1.2, roundHalfEven(-1.25, 1))
    }

    @Test
    fun `rounds non-tie values to the nearest value regardless of parity`() {
        assertEquals(1.3, roundHalfEven(1.26, 1))
        assertEquals(1.2, roundHalfEven(1.24, 1))
        assertEquals(3.0, roundHalfEven(2.6, 0))
    }
}
