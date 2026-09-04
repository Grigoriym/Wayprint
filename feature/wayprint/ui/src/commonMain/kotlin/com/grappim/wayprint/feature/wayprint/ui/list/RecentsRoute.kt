package com.grappim.wayprint.feature.wayprint.ui.list

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The app's start destination (M9.5's `startKey`) — the tracks list. No payload.
 *
 * Registered in the shell's `navKeySerializersModule` — a route missing from there survives every
 * gate and only breaks back-stack restore after process death.
 */
@Serializable
data object RecentsRoute : NavKey
