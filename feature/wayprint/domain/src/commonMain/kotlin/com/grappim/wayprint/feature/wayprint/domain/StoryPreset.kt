package com.grappim.wayprint.feature.wayprint.domain

/**
 * The single hardcoded MVP style preset: a fixed story canvas, the route-art box centered within
 * it (same units core:gpx's `fitProjection`/`toSvg` project into, y grows downward), and the
 * colors M4's renderer paints with. Own palette (forest green matching uikit's M2 theme seed
 * hue), not the Elbe reference's paper/ochre one.
 */
data class StoryPreset(
    val canvasWidth: Double,
    val canvasHeight: Double,
    val routeBoxWidth: Double,
    val routeBoxHeight: Double,
    val marginX: Double,
    val marginY: Double,
    val backgroundColor: String,
    val lineColor: String,
    val textColor: String
)

val DEFAULT_STORY_PRESET = StoryPreset(
    canvasWidth = 1080.0,
    canvasHeight = 1920.0,
    routeBoxWidth = 860.0,
    routeBoxHeight = 980.0,
    marginX = 110.0,
    marginY = 470.0,
    backgroundColor = "#F7F4EC",
    lineColor = "#2E6B4F",
    textColor = "#1B2E22"
)
