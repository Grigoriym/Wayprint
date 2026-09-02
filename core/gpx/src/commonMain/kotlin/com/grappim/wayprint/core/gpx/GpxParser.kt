package com.grappim.wayprint.core.gpx

import org.w3c.dom.Element
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

private const val GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1"

/**
 * Returns the track points of the first `<trk>` in a GPX file, ported from `parse_track()` in
 * `gpx_route_art.py`. A `<trkpt>` with no `<ele>` child defaults its elevation to `0.0`, matching
 * the Python reference.
 */
fun parseTrack(input: InputStream): List<TrackPoint> {
    val document = newDocumentBuilder().parse(input)
    val trk = document.getElementsByTagNameNS(GPX_NAMESPACE, "trk").item(0) as? Element
        ?: return emptyList()

    val trkpts = trk.getElementsByTagNameNS(GPX_NAMESPACE, "trkpt")
    return buildList {
        for (i in 0 until trkpts.length) {
            val trkpt = trkpts.item(i) as Element
            val lat = trkpt.getAttribute("lat").toDouble()
            val lon = trkpt.getAttribute("lon").toDouble()
            val eleElements = trkpt.getElementsByTagNameNS(GPX_NAMESPACE, "ele")
            val ele = if (eleElements.length > 0) eleElements.item(0).textContent.toDouble() else 0.0
            add(TrackPoint(lat, lon, ele))
        }
    }
}

// GPX files come from a file picker / share-intent (untrusted input, M5), so entity/DTD
// resolution is disabled here to close off XXE rather than leaving it open until that step.
private fun newDocumentBuilder() = DocumentBuilderFactory.newInstance().apply {
    isNamespaceAware = true
    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    setFeature("http://xml.org/sax/features/external-general-entities", false)
    setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    isXIncludeAware = false
    isExpandEntityReferences = false
}.newDocumentBuilder()
