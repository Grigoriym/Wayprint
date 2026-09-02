package com.grappim.wayprint.feature.wayprint.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class FitScaleTest {

    @Test
    fun `available space matching the canvas aspect ratio fits exactly`() {
        val fit = fitScale(
            canvasWidth = 1080.0,
            canvasHeight = 1920.0,
            availableWidth = 540.0,
            availableHeight = 960.0
        )

        assertEquals(0.5, fit.scale)
        assertEquals(0.0, fit.offsetX)
        assertEquals(0.0, fit.offsetY)
    }

    @Test
    fun `wider available space is letterboxed on the sides`() {
        val fit = fitScale(
            canvasWidth = 1080.0,
            canvasHeight = 1920.0,
            availableWidth = 2000.0,
            availableHeight = 1920.0
        )

        assertEquals(1.0, fit.scale)
        assertEquals(460.0, fit.offsetX)
        assertEquals(0.0, fit.offsetY)
    }

    @Test
    fun `taller available space is letterboxed on top and bottom`() {
        val fit = fitScale(
            canvasWidth = 1080.0,
            canvasHeight = 1920.0,
            availableWidth = 1080.0,
            availableHeight = 3000.0
        )

        assertEquals(1.0, fit.scale)
        assertEquals(0.0, fit.offsetX)
        assertEquals(540.0, fit.offsetY)
    }
}
