package com.grappim.wayprint.core.gpx

import kotlin.math.floor
import kotlin.math.pow

// Portable replacement for `BigDecimal(value).setScale(scale, RoundingMode.HALF_EVEN)`, which
// isn't available on Kotlin/Native. Matches Python's `round(x, ndigits)` at the tie cases that
// actually occur for IEEE 754 doubles: an exact tie only exists when `value * 10^scale` is itself
// exactly representable (e.g. 1.25 at scale=1, 2.5 at scale=0) — multiplying by a power of ten
// that keeps the result within a double's 52-bit mantissa is exact per IEEE 754, so this matches
// BigDecimal's exact-value semantics for every tie this codebase's inputs can produce.
internal fun roundHalfEven(value: Double, scale: Int): Double {
    val factor = 10.0.pow(scale)
    val scaled = value * factor
    val flr = floor(scaled)
    val diff = scaled - flr
    val rounded = when {
        diff > 0.5 -> flr + 1.0
        diff < 0.5 -> flr
        else -> if (flr % 2.0 == 0.0) flr else flr + 1.0
    }
    return rounded / factor
}
