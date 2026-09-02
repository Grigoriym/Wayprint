package com.grappim.wayprint.feature.wayprint.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.grappim.wayprint.feature.wayprint.domain.StoryPreset
import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout

private const val ROUTE_LINE_WIDTH = 6f

/**
 * Draws [preset]'s background full-bleed and [layout]'s route path as one stroked polyline,
 * letterboxed into the composable's actual size via [fitScale]. No markers/labels yet (M4.2).
 */
@Composable
fun WayprintCanvas(layout: WayprintLayout, preset: StoryPreset, modifier: Modifier = Modifier) {
    val backgroundColor = parseHexColor(preset.backgroundColor)
    val lineColor = parseHexColor(preset.lineColor)

    Canvas(modifier = modifier) {
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
        }
    }
}

private fun parseHexColor(hex: String): Color {
    val rgb = hex.removePrefix("#").toLong(radix = 16)
    return Color(color = 0xFF000000 or rgb)
}
