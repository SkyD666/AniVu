package com.skyd.anivu.model.preference.data

import android.content.Context
import android.os.Environment
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.anivu.base.BasePreference
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FilePickerLocationPreference : BasePreference<String> {
    private const val FILE_PICKER_LOCATION = "filePickerLocation"

    override val default: String = Environment.getExternalStorageDirectory().absolutePath

    val key = stringPreferencesKey(FILE_PICKER_LOCATION)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}
