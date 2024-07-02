package com.skyd.anivu.model.preference.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object OpmlExportDirPreference : BasePreference<String> {
    private const val OPML_EXPORT_DIR = "opmlExportDir"

    override val default = ""

    val key = stringPreferencesKey(OPML_EXPORT_DIR)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
