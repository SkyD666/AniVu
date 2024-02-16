package com.skyd.anivu.ext

import android.os.Bundle

inline fun <reified T : Enum<T>> Bundle.getEnum(key: String, default: T) =
    getInt(key, -1).let { if (it >= 0) enumValues<T>()[it] else default }

fun <T : Enum<T>> Bundle.putEnum(key: String, value: T?) =
    putInt(key, value?.ordinal ?: -1)