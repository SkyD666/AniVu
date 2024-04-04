package com.skyd.anivu.ui.fragment.settings.appearance

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.appearance.ThemePreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AppearanceFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.appearance_fragment_name) }
    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        val themeCategory = PreferenceCategory(this).apply {
            key = "themeCategory"
            title = getString(R.string.appearance_fragment_theme_category)
            screen.addPreference(this)
        }

        DropDownPreference(this).apply {
            key = "theme"
            title = getString(R.string.appearance_fragment_theme)
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            value = context.dataStore.getOrDefault(ThemePreference)
            entries = ThemePreference.values.map {
                ThemePreference.toDisplayName(context, it)
            }.toTypedArray()
            entryValues = ThemePreference.values
            setOnPreferenceChangeListener { _, newValue ->
                ThemePreference.put(
                    requireContext(), lifecycleScope, newValue as String,
                ) {
                    requireActivity().recreate()
                }
                true
            }
            themeCategory.addPreference(this)
        }
    }
}