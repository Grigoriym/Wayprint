package com.grappim.wayprint.feature.wayprint.domain

import com.grappim.wayprint.core.gpx.dayPalette

/**
 * The single hardcoded MVP canvas shape: a fixed story canvas and the route-art box centered
 * within it (same units core:gpx's `fitProjection`/`toSvg` project into, y grows downward).
 * Colors live separately in [ColorScheme] so multiple schemes can apply to the same shape.
 */
data class StoryPreset(
    val canvasWidth: Double,
    val canvasHeight: Double,
    val routeBoxWidth: Double,
    val routeBoxHeight: Double,
    val marginX: Double,
    val marginY: Double
)

val DEFAULT_STORY_PRESET = StoryPreset(
    canvasWidth = 1080.0,
    canvasHeight = 1920.0,
    routeBoxWidth = 860.0,
    routeBoxHeight = 980.0,
    marginX = 110.0,
    marginY = 470.0
)

/** The 3 colors M4's renderer paints with, grouped so a scheme can be swapped as one unit. */
data class ColorScheme(val backgroundColor: String, val lineColor: String, val textColor: String)

private const val PRESET_BACKGROUND_COLOR = "#F7F4EC"
private const val PRESET_TEXT_COLOR = "#1B2E22"

/**
 * The 5 selectable color schemes: `core:gpx`'s `dayPalette(5)` supplies each [ColorScheme.lineColor],
 * background/text stay fixed across every scheme.
 */
val PRESET_COLOR_SCHEMES: List<ColorScheme> = dayPalette(5).map { hue ->
    ColorScheme(backgroundColor = PRESET_BACKGROUND_COLOR, lineColor = hue, textColor = PRESET_TEXT_COLOR)
}
