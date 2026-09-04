package com.grappim.wayprint.core.gpx

import kotlinx.io.Source
import kotlinx.io.readString

private const val TAG_TRK = "trk"
private const val TAG_TRKPT = "trkpt"
private const val TAG_ELE = "ele"

/**
 * Returns the track points of the first `<trk>` in a GPX file, ported from `parse_track()` in
 * `gpx_route_art.py`. A `<trkpt>` with no `<ele>` child defaults its elevation to `0.0`, matching
 * the Python reference.
 *
 * Hand-rolled scan rather than a DOM/XML library: GPX's needed subset here is narrow (`<trk>`,
 * `<trkpt lat lon>`, `<ele>`, matched by local name, namespace-blind), and it sidesteps XXE by
 * construction — comments/CDATA/PIs/DOCTYPEs are skipped verbatim, never interpreted, so there's
 * no entity-resolver workaround to carry over from the old `DocumentBuilderFactory` parser.
 */
fun parseTrack(input: Source): List<TrackPoint> {
    val xml = input.readString()
    val scan = TrackScan()
    var pos = 0

    while (!scan.trkDone) {
        val tag = nextTag(xml, pos) ?: break
        pos = tag.contentEnd
        if (tag.isEndTag) scan.closeTag(tag) else scan.openTag(tag, xml)
    }

    return scan.points
}

/** Depth-tracked scan state for [parseTrack] — a plain integer depth counter is enough to match
 * an end tag back to the start tag that opened it, with no name stack needed (see M15.2's Note). */
private class TrackScan {
    val points = mutableListOf<TrackPoint>()
    var depth = 0
    var trkDepth = -1
    var trkDone = false
    var trkptDepth = -1
    private var lat = 0.0
    private var lon = 0.0
    private var ele = 0.0
    private var eleCaptured = false

    fun closeTag(tag: Tag) {
        if (tag.localName == TAG_TRK && depth == trkDepth) {
            trkDone = true
        } else if (tag.localName == TAG_TRKPT && depth == trkptDepth) {
            finishTrkpt()
        }
        depth--
    }

    fun openTag(tag: Tag, xml: String) {
        val elementDepth = depth + 1
        captureAttributes(tag, elementDepth, xml)
        when {
            tag.isSelfClosing && tag.localName == TAG_TRKPT && trkptDepth == elementDepth -> finishTrkpt()
            tag.isSelfClosing && tag.localName == TAG_TRK && trkDepth == elementDepth -> trkDone = true
            !tag.isSelfClosing -> depth = elementDepth
        }
    }

    private fun captureAttributes(tag: Tag, elementDepth: Int, xml: String) {
        when {
            tag.localName == TAG_TRK && trkDepth == -1 -> trkDepth = elementDepth

            trkDepth != -1 && tag.localName == TAG_TRKPT && trkptDepth == -1 -> startTrkpt(tag, elementDepth)

            trkptDepth != -1 && !eleCaptured && tag.localName == TAG_ELE && !tag.isSelfClosing ->
                captureEle(tag, xml)
        }
    }

    private fun startTrkpt(tag: Tag, elementDepth: Int) {
        trkptDepth = elementDepth
        lat = tag.attribute("lat")?.toDouble() ?: 0.0
        lon = tag.attribute("lon")?.toDouble() ?: 0.0
        ele = 0.0
        eleCaptured = false
    }

    private fun captureEle(tag: Tag, xml: String) {
        val textEnd = xml.indexOf('<', tag.contentEnd).let { if (it >= 0) it else xml.length }
        ele = decodeXmlEntities(xml.substring(tag.contentEnd, textEnd)).trim().toDoubleOrNull() ?: 0.0
        eleCaptured = true
    }

    private fun finishTrkpt() {
        points += TrackPoint(lat, lon, ele)
        trkptDepth = -1
    }
}

private class Tag(
    val isEndTag: Boolean,
    val isSelfClosing: Boolean,
    val localName: String,
    private val attributesRaw: String,
    val contentEnd: Int
) {
    fun attribute(name: String): String? {
        val match = Regex("""$name\s*=\s*(?:"([^"]*)"|'([^']*)')""").find(attributesRaw) ?: return null
        return match.groupValues[1].ifEmpty { match.groupValues[2] }
    }
}

/** Finds the next real tag from [from], skipping comments/CDATA/PIs/DOCTYPEs verbatim. */
private fun nextTag(xml: String, from: Int): Tag? {
    var start = xml.indexOf('<', from)
    while (start != -1) {
        val skipTo = skipNonTag(xml, start)
        if (skipTo == null) break
        if (skipTo == start) return readTag(xml, start)
        start = xml.indexOf('<', skipTo)
    }
    return null
}

/** Returns the index past a comment/CDATA/PI/DOCTYPE at [start], or [start] itself if it's a real tag. */
private fun skipNonTag(xml: String, start: Int): Int? = when {
    xml.startsWith("<!--", start) -> xml.indexOf("-->", start).let { if (it == -1) null else it + "-->".length }
    xml.startsWith("<![CDATA[", start) -> xml.indexOf("]]>", start).let { if (it == -1) null else it + "]]>".length }
    xml.startsWith("<?", start) -> xml.indexOf("?>", start).let { if (it == -1) null else it + "?>".length }
    xml.startsWith("<!", start) -> xml.indexOf('>', start).let { if (it == -1) null else it + 1 }
    else -> start
}

private fun readTag(xml: String, start: Int): Tag? {
    val isEndTag = xml.startsWith("</", start)
    val nameStart = start + if (isEndTag) 2 else 1
    val end = findTagEnd(xml, start) ?: return null
    val isSelfClosing = !isEndTag && xml[end - 1] == '/'
    val bodyEnd = if (isSelfClosing) end - 1 else end
    val body = xml.substring(nameStart, bodyEnd)
    val nameEnd = body.indexOfFirst { it.isWhitespace() }.let { if (it == -1) body.length else it }
    val qualifiedName = body.substring(0, nameEnd)
    val localName = qualifiedName.substringAfterLast(':')
    val attributesRaw = body.substring(nameEnd)
    return Tag(isEndTag, isSelfClosing, localName, attributesRaw, contentEnd = end + 1)
}

/** Finds the `>` that closes the tag opened at [start], skipping any inside quoted attribute values. */
private fun findTagEnd(xml: String, start: Int): Int? {
    var quote: Char? = null
    for (i in start + 1 until xml.length) {
        val c = xml[i]
        when {
            quote != null -> if (c == quote) quote = null
            c == '"' || c == '\'' -> quote = c
            c == '>' -> return i
        }
    }
    return null
}

private fun decodeXmlEntities(text: String): String = text
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&apos;", "'")
    .replace("&quot;", "\"")
    .replace("&amp;", "&")
