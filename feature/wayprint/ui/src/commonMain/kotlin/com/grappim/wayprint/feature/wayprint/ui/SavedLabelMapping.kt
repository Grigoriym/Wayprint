package com.grappim.wayprint.feature.wayprint.ui

import com.grappim.wayprint.core.storage.SavedLabel
import com.grappim.wayprint.feature.wayprint.domain.PlacedLabel
import com.grappim.wayprint.feature.wayprint.domain.TextAnchor
import com.grappim.wayprint.feature.wayprint.domain.placeNewLabel

/**
 * Bridges [PlacedLabel] (`feature:wayprint:domain`) and [SavedLabel] (`core:storage`) — neither
 * module depends on the other (M10.2's shared context), so this conversion lives here instead.
 */
fun PlacedLabel.toSavedLabel(): SavedLabel = SavedLabel(id = id, text = text, x = x, y = y, anchor = anchor.name)

/** Rebuilds a [PlacedLabel] from a persisted [SavedLabel], recomputing its bounding box. */
fun SavedLabel.toPlacedLabel(): PlacedLabel =
    placeNewLabel(id = id, text = text, x = x, y = y, anchor = TextAnchor.valueOf(anchor))
