package com.skyd.anivu.model.preference.autodelete

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

object AutoDeleteArticleFrequencyPreference : BasePreference<Long> {
    private const val AUTO_DELETE_ARTICLE_FREQUENCY = "autoDeleteArticleFrequency"

    val EVERY_1_DAY = 1.days.inWholeMilliseconds
    val EVERY_2_DAY = 2.days.inWholeMilliseconds
    val EVERY_3_DAY = 3.days.inWholeMilliseconds
    val EVERY_5_DAY = 5.days.inWholeMilliseconds
    val EVERY_7_DAY = 7.days.inWholeMilliseconds
    val EVERY_10_DAY = 10.days.inWholeMilliseconds

    val frequencies = listOf(
        EVERY_1_DAY,
        EVERY_2_DAY,
        EVERY_3_DAY,
        EVERY_5_DAY,
        EVERY_7_DAY,
        EVERY_10_DAY,
    )

    override val default = EVERY_5_DAY

    val key = longPreferencesKey(AUTO_DELETE_ARTICLE_FREQUENCY)

    fun put(context: Context, scope: CoroutineScope, value: Long) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): Long = preferences[key] ?: default

    fun toDisplayName(
        context: Context,
        value: Long = context.dataStore.getOrDefault(this),
    ): String = when (value) {
        EVERY_1_DAY, EVERY_2_DAY, EVERY_3_DAY,
        EVERY_5_DAY, EVERY_7_DAY, EVERY_10_DAY -> context.resources.getQuantityString(
            R.plurals.frequency_day,
            value.milliseconds.inWholeDays.toInt(),
            value.milliseconds.inWholeDays.toInt(),
        )

        else -> context.getString(R.string.frequency_manual)
    }
}
