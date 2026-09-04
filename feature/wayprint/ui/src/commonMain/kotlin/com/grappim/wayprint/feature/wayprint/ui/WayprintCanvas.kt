package com.grappim.wayprint.feature.wayprint.ui

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.grappim.wayprint.feature.wayprint.domain.ColorScheme
import com.grappim.wayprint.feature.wayprint.domain.CombinedWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.PRESET_COLOR_SCHEMES
import com.grappim.wayprint.feature.wayprint.domain.PlacedLabel
import com.grappim.wayprint.feature.wayprint.domain.StoryPreset
import com.grappim.wayprint.feature.wayprint.domain.TextAnchor
import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout

private const val ROUTE_LINE_WIDTH = 6f
private const val START_MARKER_RADIUS = 13f
private const val FINISH_MARKER_RADIUS = 14f
private const val MARKER_STROKE_WIDTH = 6f
private const val LABEL_TEXT_SIZE = 28f
private const val LABEL_HALO_WIDTH = 8f
private const val LABEL_BASELINE_OFFSET_FACTOR = 0.35f
private val MIN_LABEL_TOUCH_TARGET = 48.dp

/**
 * Letterboxes [layout]/[preset] into the composable's actual on-screen size via [fitScale], then
 * delegates the actual drawing to [drawWayprintStory]. A drag gesture is inverse-transformed
 * through the same [fitScale] to hit-test [WayprintLayout.labels] in canvas space: [onDragStart]
 * fires once a label is hit, [onDrag] fires on every move with the label's new canvas-space
 * position, [onDragEnd] once the gesture finishes. Drag position is tracked as running deltas
 * inside the gesture rather than read back off [layout], since a caller applying [onDrag] live
 * would otherwise restart the gesture on every recomposition.
 *
 * A separate tap/long-press gesture (M10.3) hit-tests the same way: [onLabelTap] fires on every
 * tap with the hit label's id (`null` for empty canvas), [onCanvasLongPress] fires only for a
 * long-press that missed every label, with the press's canvas-space position — an actual drag
 * consumes the position change before this detector's touch-slop check, so the two gestures don't
 * fire for the same touch.
 */
@Composable
fun WayprintCanvas(
    layout: WayprintLayout,
    preset: StoryPreset,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit = {},
    onDrag: (index: Int, x: Double, y: Double) -> Unit = { _, _, _ -> },
    onDragEnd: () -> Unit = {},
    onLabelTap: (id: String?) -> Unit = {},
    onCanvasLongPress: (x: Double, y: Double) -> Unit = { _, _ -> }
) {
    val currentLayout by rememberUpdatedState(layout)

    Canvas(
        modifier = modifier
            .wayprintDragGestures(preset, { currentLayout.labels }, onDragStart, onDrag, onDragEnd)
            .wayprintTapGestures(preset, { currentLayout.labels }, onLabelTap, onCanvasLongPress)
    ) {
        val fit = fitScale(
            canvasWidth = preset.canvasWidth,
            canvasHeight = preset.canvasHeight,
            availableWidth = size.width.toDouble(),
            availableHeight = size.height.toDouble()
        )

        withTransform({
            translate(left = fit.offsetX.toFloat(), top = fit.offsetY.toFloat())
            scale(scaleX = fit.scale.toFloat(), scaleY = fit.scale.toFloat(), pivot = Offset.Zero)
        }) {
            drawWayprintStory(layout, preset, colorScheme)
        }
    }
}

/**
 * [WayprintCanvas]'s N-track equivalent (M11.4): same fit/letterbox and label drag/tap gestures,
 * drawing [CombinedWayprintLayout]'s per-track colored paths instead of one line. No user-selectable
 * [ColorScheme] param — [colorScheme] only supplies the fixed background/text colors shared by every
 * scheme; each track's line color comes from [CombinedWayprintLayout]'s own `ColoredPath.color`.
 */
@Composable
fun CombinedWayprintCanvas(
    layout: CombinedWayprintLayout,
    preset: StoryPreset,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = PRESET_COLOR_SCHEMES.first(),
    onDragStart: () -> Unit = {},
    onDrag: (index: Int, x: Double, y: Double) -> Unit = { _, _, _ -> },
    onDragEnd: () -> Unit = {},
    onLabelTap: (id: String?) -> Unit = {},
    onCanvasLongPress: (x: Double, y: Double) -> Unit = { _, _ -> }
) {
    val currentLayout by rememberUpdatedState(layout)

    Canvas(
        modifier = modifier
            .wayprintDragGestures(preset, { currentLayout.labels }, onDragStart, onDrag, onDragEnd)
            .wayprintTapGestures(preset, { currentLayout.labels }, onLabelTap, onCanvasLongPress)
    ) {
        val fit = fitScale(
            canvasWidth = preset.canvasWidth,
            canvasHeight = preset.canvasHeight,
            availableWidth = size.width.toDouble(),
            availableHeight = size.height.toDouble()
        )

        withTransform({
            translate(left = fit.offsetX.toFloat(), top = fit.offsetY.toFloat())
            scale(scaleX = fit.scale.toFloat(), scaleY = fit.scale.toFloat(), pivot = Offset.Zero)
        }) {
            drawCombinedWayprintStory(layout, preset, colorScheme)
        }
    }
}

/**
 * The label drag gesture shared by [WayprintCanvas]/[CombinedWayprintCanvas]: hit-tests
 * [labels]() in canvas space (inverse-transformed through [preset]'s fit-scale), then reports the
 * dragged label's running canvas-space position via [onDrag].
 */
@Composable
private fun Modifier.wayprintDragGestures(
    preset: StoryPreset,
    labels: () -> List<PlacedLabel>,
    onDragStart: () -> Unit,
    onDrag: (index: Int, x: Double, y: Double) -> Unit,
    onDragEnd: () -> Unit
): Modifier = pointerInput(preset) {
    var draggedIndex = -1
    var x = 0.0
    var y = 0.0
    val minTouchTargetPx = MIN_LABEL_TOUCH_TARGET.toPx()

    fun currentFit() = fitScale(preset.canvasWidth, preset.canvasHeight, size.width.toDouble(), size.height.toDouble())

    detectDragGestures(
        onDragStart = { start ->
            val fit = currentFit()
            val canvasX = (start.x - fit.offsetX) / fit.scale
            val canvasY = (start.y - fit.offsetY) / fit.scale
            val minSize = minTouchTargetPx / fit.scale
            draggedIndex = hitTestLabelIndex(labels(), canvasX, canvasY, minSize)
            if (draggedIndex >= 0) {
                val label = labels()[draggedIndex]
                x = label.x
                y = label.y
                onDragStart()
            }
        },
        onDrag = { change, dragAmount ->
            if (draggedIndex >= 0) {
                change.consume()
                val fit = currentFit()
                x += dragAmount.x / fit.scale
                y += dragAmount.y / fit.scale
                onDrag(draggedIndex, x, y)
            }
        },
        onDragEnd = {
            if (draggedIndex >= 0) onDragEnd()
            draggedIndex = -1
        },
        onDragCancel = { draggedIndex = -1 }
    )
}

/**
 * The label tap/long-press gesture shared by [WayprintCanvas]/[CombinedWayprintCanvas] — see
 * [WayprintCanvas]'s doc for the tap-vs-drag/long-press-miss semantics.
 */
@Composable
private fun Modifier.wayprintTapGestures(
    preset: StoryPreset,
    labels: () -> List<PlacedLabel>,
    onLabelTap: (id: String?) -> Unit,
    onCanvasLongPress: (x: Double, y: Double) -> Unit
): Modifier = pointerInput(preset) {
    val minTouchTargetPx = MIN_LABEL_TOUCH_TARGET.toPx()

    fun currentFit() = fitScale(preset.canvasWidth, preset.canvasHeight, size.width.toDouble(), size.height.toDouble())

    detectTapGestures(
        onTap = { position ->
            val fit = currentFit()
            val canvasX = (position.x - fit.offsetX) / fit.scale
            val canvasY = (position.y - fit.offsetY) / fit.scale
            val minSize = minTouchTargetPx / fit.scale
            val index = hitTestLabelIndex(labels(), canvasX, canvasY, minSize)
            onLabelTap(labels().getOrNull(index)?.id)
        },
        onLongPress = { position ->
            val fit = currentFit()
            val canvasX = (position.x - fit.offsetX) / fit.scale
            val canvasY = (position.y - fit.offsetY) / fit.scale
            val minSize = minTouchTargetPx / fit.scale
            if (hitTestLabelIndex(labels(), canvasX, canvasY, minSize) < 0) {
                onCanvasLongPress(canvasX, canvasY)
            }
        }
    )
}

/**
 * Draws [colorScheme]'s background full-bleed, [layout]'s route path as one stroked polyline,
 * Start/Finish circle markers, and [layout]'s labels with a stroke halo for legibility over the
 * line (the Elbe reference's `paint-order: stroke` technique), in [preset]'s own canvas-space
 * coordinates (`0..canvasWidth, 0..canvasHeight`) — no fit-scale/letterbox transform of its own,
 * so it's reusable by both [WayprintCanvas]'s on-screen letterboxed transform and
 * [renderWayprintStoryBitmap]'s headless full-size render.
 */
fun DrawScope.drawWayprintStory(layout: WayprintLayout, preset: StoryPreset, colorScheme: ColorScheme) {
    val backgroundColor = parseHexColor(colorScheme.backgroundColor)
    val lineColor = parseHexColor(colorScheme.lineColor)
    val textColor = parseHexColor(colorScheme.textColor)

    drawRect(
        color = backgroundColor,
        size = Size(preset.canvasWidth.toFloat(), preset.canvasHeight.toFloat())
    )

    val routePath = Path().apply {
        layout.path.forEachIndexed { index, (x, y) ->
            if (index == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
        }
    }
    drawPath(
        path = routePath,
        color = lineColor,
        style = Stroke(width = ROUTE_LINE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    val (startX, startY) = layout.path.first()
    val startCenter = Offset(startX.toFloat(), startY.toFloat())
    drawCircle(color = backgroundColor, radius = START_MARKER_RADIUS, center = startCenter)
    drawCircle(
        color = lineColor,
        radius = START_MARKER_RADIUS,
        center = startCenter,
        style = Stroke(width = MARKER_STROKE_WIDTH)
    )

    val (finishX, finishY) = layout.path.last()
    drawCircle(
        color = lineColor,
        radius = FINISH_MARKER_RADIUS,
        center = Offset(finishX.toFloat(), finishY.toFloat())
    )

    drawWayprintLabels(layout.labels, backgroundColor, textColor)
}

/**
 * [drawWayprintStory]'s N-track equivalent (M11.4): same background/marker/label treatment, one
 * stroked polyline per [CombinedWayprintLayout.tracks] entry in its own `ColoredPath.color`; the
 * global Start/Finish markers are colored with the first/last track's color respectively, matching
 * [defaultCombinedLabelRequests][com.grappim.wayprint.feature.wayprint.domain.defaultCombinedLabelRequests]'s
 * global-Start/global-Finish semantics. [colorScheme]'s `lineColor` is unused — only its fixed
 * background/text colors apply.
 */
fun DrawScope.drawCombinedWayprintStory(layout: CombinedWayprintLayout, preset: StoryPreset, colorScheme: ColorScheme) {
    val backgroundColor = parseHexColor(colorScheme.backgroundColor)
    val textColor = parseHexColor(colorScheme.textColor)

    drawRect(
        color = backgroundColor,
        size = Size(preset.canvasWidth.toFloat(), preset.canvasHeight.toFloat())
    )

    layout.tracks.forEach { track ->
        val routePath = Path().apply {
            track.path.forEachIndexed { index, (x, y) ->
                if (index == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
            }
        }
        drawPath(
            path = routePath,
            color = parseHexColor(track.color),
            style = Stroke(width = ROUTE_LINE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    val firstTrack = layout.tracks.first()
    val (startX, startY) = firstTrack.path.first()
    val startColor = parseHexColor(firstTrack.color)
    val startCenter = Offset(startX.toFloat(), startY.toFloat())
    drawCircle(color = backgroundColor, radius = START_MARKER_RADIUS, center = startCenter)
    drawCircle(
        color = startColor,
        radius = START_MARKER_RADIUS,
        center = startCenter,
        style = Stroke(width = MARKER_STROKE_WIDTH)
    )

    val lastTrack = layout.tracks.last()
    val (finishX, finishY) = lastTrack.path.last()
    drawCircle(
        color = parseHexColor(lastTrack.color),
        radius = FINISH_MARKER_RADIUS,
        center = Offset(finishX.toFloat(), finishY.toFloat())
    )

    drawWayprintLabels(layout.labels, backgroundColor, textColor)
}

/** The halo-then-fill label text drawing shared by [drawWayprintStory]/[drawCombinedWayprintStory]. */
private fun DrawScope.drawWayprintLabels(labels: List<PlacedLabel>, backgroundColor: Color, textColor: Color) {
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = LABEL_HALO_WIDTH
        strokeJoin = Paint.Join.ROUND
        textSize = LABEL_TEXT_SIZE
        color = backgroundColor.toArgb()
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = LABEL_TEXT_SIZE
        color = textColor.toArgb()
    }
    labels.forEach { label ->
        val align = label.anchor.toPaintAlign()
        haloPaint.textAlign = align
        fillPaint.textAlign = align
        val x = label.x.toFloat()
        val baselineY = label.y.toFloat() + LABEL_TEXT_SIZE * LABEL_BASELINE_OFFSET_FACTOR
        drawContext.canvas.nativeCanvas.drawText(label.text, x, baselineY, haloPaint)
        drawContext.canvas.nativeCanvas.drawText(label.text, x, baselineY, fillPaint)
    }
}

/**
 * Headless render of [drawWayprintStory] at [preset]'s exact canvas size (1080×1920 for
 * [com.grappim.wayprint.feature.wayprint.domain.DEFAULT_STORY_PRESET]) — no fit-scale/letterbox,
 * since that's only needed for on-screen display ([WayprintCanvas]). For export.
 */
fun renderWayprintStoryBitmap(layout: WayprintLayout, preset: StoryPreset, colorScheme: ColorScheme): Bitmap {
    val width = preset.canvasWidth.toInt()
    val height = preset.canvasHeight.toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    CanvasDrawScope().draw(
        density = Density(density = 1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = androidx.compose.ui.graphics.Canvas(android.graphics.Canvas(bitmap)),
        size = Size(preset.canvasWidth.toFloat(), preset.canvasHeight.toFloat())
    ) {
        drawWayprintStory(layout, preset, colorScheme)
    }
    return bitmap
}

/** [renderWayprintStoryBitmap]'s N-track equivalent (M11.4), headless-rendering [drawCombinedWayprintStory]. */
fun renderCombinedWayprintStoryBitmap(
    layout: CombinedWayprintLayout,
    preset: StoryPreset,
    colorScheme: ColorScheme = PRESET_COLOR_SCHEMES.first()
): Bitmap {
    val width = preset.canvasWidth.toInt()
    val height = preset.canvasHeight.toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    CanvasDrawScope().draw(
        density = Density(density = 1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = androidx.compose.ui.graphics.Canvas(android.graphics.Canvas(bitmap)),
        size = Size(preset.canvasWidth.toFloat(), preset.canvasHeight.toFloat())
    ) {
        drawCombinedWayprintStory(layout, preset, colorScheme)
    }
    return bitmap
}

/**
 * The last (topmost-drawn) label in [labels] whose touch-friendly [labelTouchRect] contains
 * ([x], [y]) in canvas space, or -1 if none does. Shared by [WayprintCanvas]/[CombinedWayprintCanvas]'s
 * drag and tap/long-press gesture detectors so both hit-test the same way.
 */
private fun hitTestLabelIndex(labels: List<PlacedLabel>, x: Double, y: Double, minSize: Double): Int {
    val touchTestPaint = Paint().apply { textSize = LABEL_TEXT_SIZE }
    val metrics = touchTestPaint.fontMetrics
    return labels.indexOfLast { label ->
        val touchRect = labelTouchRect(
            x = label.x,
            y = label.y,
            anchor = label.anchor,
            textWidth = touchTestPaint.measureText(label.text).toDouble(),
            textHeight = (metrics.descent - metrics.ascent).toDouble(),
            minSize = minSize
        )
        touchRect.contains(x, y)
    }
}

private fun TextAnchor.toPaintAlign(): Paint.Align = when (this) {
    TextAnchor.START -> Paint.Align.LEFT
    TextAnchor.MIDDLE -> Paint.Align.CENTER
    TextAnchor.END -> Paint.Align.RIGHT
}

fun parseHexColor(hex: String): Color {
    val rgb = hex.removePrefix("#").toLong(radix = 16)
    return Color(color = 0xFF000000 or rgb)
}
