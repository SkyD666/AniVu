package com.skyd.anivu.ui.fragment.video

import com.skyd.anivu.base.mvi.MviIntent
import java.io.File

sealed interface VideoIntent : MviIntent {
    data class Init(val path: String) : VideoIntent
    data class Refresh(val path: String) : VideoIntent
    data class Delete(val file: File) : VideoIntent
}