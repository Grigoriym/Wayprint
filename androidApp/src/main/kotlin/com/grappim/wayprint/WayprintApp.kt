package com.grappim.wayprint

import android.app.Application
import com.grappim.wayprint.composeapp.di.KoinApp
import org.koin.android.ext.koin.androidContext
import org.koin.plugin.module.dsl.startKoin

class WayprintApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin<KoinApp> {
            androidContext(this@WayprintApp)
            printLogger()
        }
    }
}
