package com.skyd.anivu.model.preference.appearance.media

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MediaFileFilterPreference : BasePreference<String> {
    private const val MEDIA_FILE_FILTER = "mediaFileFilter"

    const val VIDEO_REGEX = ".*\\.(mp4|avi|mkv|mov|flv|wmv|webm|mpg|mpeg|3gp|rmvb|ts|mov|m3u8)\$"
    const val AUDIO_REGEX = ".*\\.(mp3|wav|flac|aac|ogg|m4a|wma|opus|alac|aiff|aif)\$"
    const val MEDIA_REGEX = "($VIDEO_REGEX)|($AUDIO_REGEX)"
    const val ALL_REGEX = ".*"

    val values = arrayOf(ALL_REGEX, MEDIA_REGEX, VIDEO_REGEX, AUDIO_REGEX)

    override val default = ALL_REGEX

    val key = stringPreferencesKey(MEDIA_FILE_FILTER)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default

    fun toDisplayName(
        context: Context,
        value: String = context.dataStore.getOrDefault(this),
    ): String = when (value) {
        VIDEO_REGEX -> context.getString(R.string.media_display_filter_video)
        AUDIO_REGEX -> context.getString(R.string.media_display_filter_audio)
        MEDIA_REGEX -> context.getString(R.string.media_display_filter_media)
        ALL_REGEX -> context.getString(R.string.media_display_filter_all)
        else -> value
    }
}
