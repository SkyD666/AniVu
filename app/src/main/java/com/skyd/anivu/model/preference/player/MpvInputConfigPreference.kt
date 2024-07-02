package com.skyd.anivu.model.preference.player

import com.skyd.anivu.config.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

object MpvInputConfigPreference {
    private var value: String? = null

    fun put(scope: CoroutineScope, value: String) {
        this.value = value
        scope.launch(Dispatchers.IO) {
            File(Const.MPV_CONFIG_DIR, "input.conf")
                .apply { if (!exists()) createNewFile() }
                .writeText(value)
        }
    }

    fun getValue(): String = value ?: runBlocking(Dispatchers.IO) {
        value = File(Const.MPV_CONFIG_DIR, "input.conf")
            .apply { if (!exists()) createNewFile() }
            .readText()
        value.orEmpty()
    }
}
