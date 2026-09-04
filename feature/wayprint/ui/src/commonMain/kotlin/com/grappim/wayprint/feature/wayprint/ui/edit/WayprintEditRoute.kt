package com.grappim.wayprint.feature.wayprint.ui.edit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Pushed onto the stack with the id of an already-saved track (M9's "every import auto-saves"
 * decision — there is no "new, unsaved" edit state). [WayprintViewModel] loads that one track
 * from `TracksStorage` and saves edits back under the same id.
 *
 * Registered in the shell's `navKeySerializersModule` — a route missing from there survives every
 * gate and only breaks back-stack restore after process death.
 */
@Serializable
data class WayprintEditRoute(val trackId: String) : NavKey
