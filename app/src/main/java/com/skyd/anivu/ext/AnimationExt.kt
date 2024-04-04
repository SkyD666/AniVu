package com.skyd.anivu.ext

import android.view.animation.Animation

val Animation.isRunning: Boolean
    get() = hasStarted() && !hasEnded()