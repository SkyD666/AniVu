package com.skyd.anivu.ui.component

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.skyd.anivu.appContext

private var uiThreadHandler: Handler = Handler(Looper.getMainLooper())

fun CharSequence.showToast(duration: Int = Toast.LENGTH_SHORT) {
    uiThreadHandler.post {
        val toast = Toast.makeText(appContext, this, duration)
        toast.duration = duration
        toast.show()
    }
}
