package com.grappim.wayprint.composeapp.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.grappim.wayprint.feature.wayprint.ui.edit.WayprintEditRoute
import com.grappim.wayprint.feature.wayprint.ui.list.RecentsRoute
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Every route in the app, listed once. `rememberNavBackStack` `require`s a configuration whose
 * `serializersModule` is not the default one and throws on the *first* composition if given
 * `SavedStateConfiguration.DEFAULT`; a route *missing* from here is the quiet failure — the app
 * runs and only back-stack restore after process death breaks.
 */
internal val navKeySerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(RecentsRoute::class)
        subclass(WayprintEditRoute::class)
    }
}

val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = navKeySerializersModule
}
