package com.skyd.anivu.ui.fragment.settings.playerconfig

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PlayerConfigFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.player_config_fragment_name) }
    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        val playerBehaviorCategory = PreferenceCategory(this).apply {
            key = "playerBehaviorCategory"
            title = getString(R.string.player_config_fragment_behavior_category)
            screen.addPreference(this)
        }

        DropDownPreference(this).apply {
            key = "playerDoubleTap"
            title = getString(R.string.player_config_fragment_double_tap)
            summary = getString(R.string.player_config_fragment_double_tap_description)
            setIcon(R.drawable.ic_touch_app_24)
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            entries = PlayerDoubleTapPreference.values.map {
                PlayerDoubleTapPreference.toDisplayName(context, it)
            }.toTypedArray()
            entryValues = PlayerDoubleTapPreference.values
            setOnPreferenceChangeListener { _, newValue ->
                PlayerDoubleTapPreference.put(
                    requireContext(), lifecycleScope, newValue as String,
                )
                true
            }
            playerBehaviorCategory.addPreference(this)
        }
    }
}