package com.skyd.anivu.ext

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2

fun snapshotStateOffsetSaver() = Saver<MutableState<Offset>, Long>(
    save = { state -> packFloats(state.value.x, state.value.y) },
    restore = { value -> mutableStateOf(Offset(unpackFloat1(value), unpackFloat2(value))) }
)