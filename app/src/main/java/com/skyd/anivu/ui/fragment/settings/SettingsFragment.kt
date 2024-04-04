package com.skyd.anivu.ui.fragment.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.findMainNavController
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.settings_fragment_name) }
    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        Preference(this).apply {
            key = "appearance"
            title = getString(R.string.appearance_fragment_name)
            summary = getString(R.string.appearance_fragment_description)
            setIcon(R.drawable.ic_palette_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_appearance_fragment)
                true
            }
            screen.addPreference(this)
        }

        Preference(this).apply {
            key = "rssConfig"
            title = getString(R.string.rss_config_fragment_name)
            summary = getString(R.string.rss_config_fragment_description)
            setIcon(R.drawable.ic_rss_feed_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_rss_config_fragment)
                true
            }
            screen.addPreference(this)
        }

        Preference(this).apply {
            key = "playerConfig"
            title = getString(R.string.player_config_fragment_name)
            summary = getString(R.string.player_config_fragment_description)
            setIcon(R.drawable.ic_smart_display_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_player_config_fragment)
                true
            }
            screen.addPreference(this)
        }

        Preference(this).apply {
            key = "data"
            title = getString(R.string.data_fragment_name)
            summary = getString(R.string.data_fragment_description)
            setIcon(R.drawable.ic_database_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_data_fragment)
                true
            }
            screen.addPreference(this)
        }

        Preference(this).apply {
            key = "transmission"
            title = getString(R.string.transmission_fragment_name)
            summary = getString(R.string.transmission_fragment_description)
            setIcon(R.drawable.ic_swap_vert_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_transmission_fragment)
                true
            }
            screen.addPreference(this)
        }
    }
}