package com.grappim.wayprint.feature.wayprint.ui.edit

import com.grappim.wayprint.feature.wayprint.domain.CombinedWayprintLayout
import com.grappim.wayprint.feature.wayprint.domain.PlacedLabel
import com.grappim.wayprint.feature.wayprint.domain.WayprintLayout

/**
 * Unifies [WayprintLayout] (single track) and [CombinedWayprintLayout] (M11) so
 * [WayprintUiState]/[WayprintViewModel] can edit labels the same way for both — the only place
 * the two diverge is drawing, where [WayprintScreen][com.grappim.wayprint.feature.wayprint.ui.edit.WayprintScreen]
 * dispatches on this type to the matching canvas.
 */
sealed interface EditableWayprintLayout {
    val labels: List<PlacedLabel>
    val totalDistanceKm: Double
    fun withLabels(labels: List<PlacedLabel>): EditableWayprintLayout

    data class Single(val layout: WayprintLayout) : EditableWayprintLayout {
        override val labels get() = layout.labels
        override val totalDistanceKm get() = layout.totalDistanceKm
        override fun withLabels(labels: List<PlacedLabel>) = Single(layout.copy(labels = labels))
    }

    data class Combined(val layout: CombinedWayprintLayout) : EditableWayprintLayout {
        override val labels get() = layout.labels
        override val totalDistanceKm get() = layout.totalDistanceKm
        override fun withLabels(labels: List<PlacedLabel>) = Combined(layout.copy(labels = labels))
    }
}
