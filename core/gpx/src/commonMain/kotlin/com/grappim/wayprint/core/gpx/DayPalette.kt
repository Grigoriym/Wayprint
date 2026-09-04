package com.grappim.wayprint.core.gpx

import kotlin.math.floor

private val DEFAULT_HUES = listOf(33, 106, 186, 253, 322)
private const val DEFAULT_SATURATION = 0.48
private const val DEFAULT_LIGHTNESS = 0.63

private const val ONE_THIRD = 1.0 / 3.0
private const val ONE_SIXTH = 1.0 / 6.0
private const val TWO_THIRD = 2.0 / 3.0
private const val RGB_MAX = 255.0

/**
 * `n` muted, evenly-spaced hues (anchored near the site's ochre ~33 deg and river-teal ~186 deg)
 * as hex colors, ported from `day_palette()` in `gpx_route_art.py`. Its HLS->RGB conversion has
 * no Kotlin stdlib equivalent, so `colorsys.hls_to_rgb`'s formula is ported here directly — note
 * the argument order is hue/lightness/saturation, not hue/saturation/lightness.
 *
 * Two call sites use this generic `n`: `StoryPreset.PRESET_COLOR_SCHEMES` (`dayPalette(5)`, one
 * fixed palette for the single-track color picker) and, per M11, a combined image's per-track
 * line color (`dayPalette(tracks.size)`, one hue per track) — no change needed here for the
 * latter, `n` already means "however many colors this caller needs."
 */
fun dayPalette(
    n: Int,
    hues: List<Int> = DEFAULT_HUES,
    s: Double = DEFAULT_SATURATION,
    l: Double = DEFAULT_LIGHTNESS
): List<String> = (0 until n).map { i ->
    val h = hues[i % hues.size]
    val (r, g, b) = hlsToRgb(h / 360.0, l, s)
    "#%02x%02x%02x".format(
        roundHalfEvenToInt(r * RGB_MAX),
        roundHalfEvenToInt(g * RGB_MAX),
        roundHalfEvenToInt(b * RGB_MAX)
    )
}

private fun hlsToRgb(h: Double, l: Double, s: Double): Triple<Double, Double, Double> {
    if (s == 0.0) return Triple(l, l, l)
    val m2 = if (l <= 0.5) l * (1.0 + s) else l + s - l * s
    val m1 = 2.0 * l - m2
    return Triple(
        hueToChannel(m1, m2, h + ONE_THIRD),
        hueToChannel(m1, m2, h),
        hueToChannel(m1, m2, h - ONE_THIRD)
    )
}

// Matches Python's `hue % 1.0`, whose result always lands in [0, 1) regardless of sign.
private fun hueToChannel(m1: Double, m2: Double, hue: Double): Double {
    val h = hue - floor(hue)
    return when {
        h < ONE_SIXTH -> m1 + (m2 - m1) * h * 6.0
        h < 0.5 -> m2
        h < TWO_THIRD -> m1 + (m2 - m1) * (TWO_THIRD - h) * 6.0
        else -> m1
    }
}

// Matches Python's round(x) (no ndigits), which rounds half-to-even to the nearest integer.
private fun roundHalfEvenToInt(value: Double): Int = roundHalfEven(value, 0).toInt()
