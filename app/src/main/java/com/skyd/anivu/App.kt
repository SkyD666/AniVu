package com.skyd.anivu

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import com.skyd.anivu.model.worker.rsssync.listenerRssSyncFrequency
import com.skyd.anivu.util.CrashHandler
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this

        CrashHandler.init(this)

        DynamicColors.applyToActivitiesIfAvailable(this)

        listenerRssSyncFrequency(this)
    }
}

lateinit var appContext: Context