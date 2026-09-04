package com.grappim.wayprint.core.gpx

import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteArtTest {

    @Test
    fun `builds route art for the fixture matching the Python reference build()`() {
        val fixture =
            requireNotNull(object {}.javaClass.getResourceAsStream("/fixtures/04 Riesa - Meissen.gpx"))

        val svgPoints = fixture.use { buildRouteArt(it.asSource().buffered()) }

        // Captured by running parse_track() -> rdp(epsilon=0.0006) -> fit_projection(860, 980)
        // -> to_svg() from gpx_route_art.py on the same fixture file.
        val expected = listOf(
            0.0 to 16.3, 159.2 to 0.0, 164.4 to 3.4, 162.1 to 10.2, 166.5 to 15.7,
            221.5 to 45.5, 215.5 to 56.8, 286.0 to 97.4, 296.4 to 83.8, 307.1 to 87.8,
            328.6 to 128.7, 352.6 to 149.7, 335.1 to 162.1, 328.4 to 174.8, 332.7 to 185.0,
            348.9 to 215.4, 370.2 to 230.1, 381.7 to 249.2, 390.1 to 249.7, 388.3 to 284.9,
            381.8 to 286.3, 374.9 to 301.3, 381.3 to 304.9, 387.7 to 326.8, 389.1 to 399.0,
            403.8 to 432.9, 433.0 to 464.0, 440.0 to 477.5, 443.6 to 498.9, 439.4 to 510.4,
            403.7 to 541.9, 411.0 to 552.0, 388.4 to 586.7, 394.1 to 597.8, 400.1 to 598.6,
            402.9 to 697.8, 405.1 to 716.8, 414.3 to 729.1, 413.5 to 737.2, 447.5 to 752.7,
            467.3 to 776.7, 486.6 to 790.7, 534.3 to 806.2, 565.0 to 824.3, 571.2 to 818.4,
            590.4 to 831.7, 614.3 to 856.1, 643.0 to 897.3, 686.4 to 920.9, 698.3 to 960.0,
            710.3 to 980.0
        )

        assertEquals(expected, svgPoints)
    }
}
