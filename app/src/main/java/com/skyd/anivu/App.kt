package com.skyd.anivu

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}

lateinit var appContext: Context