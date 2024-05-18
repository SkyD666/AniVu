package com.skyd.anivu.ui.fragment.settings.playerconfig

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import com.skyd.anivu.model.preference.player.PlayerShow85sButtonPreference
import com.skyd.anivu.model.preference.player.PlayerShowScreenshotButtonPreference
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

        val playerAppearanceCategory = PreferenceCategory(this).apply {
            key = "playerAppearanceCategory"
            title = getString(R.string.player_config_fragment_appearance_category)
            screen.addPreference(this)
        }

        SwitchPreferenceCompat(this).apply {
            key = "playerShow85sButton"
            title = getString(R.string.player_config_fragment_show_85s_button)
            summary = getString(R.string.player_config_fragment_show_85s_button_description)
            setIcon(R.drawable.ic_fast_forward_24)
            isChecked = requireContext().dataStore.getOrDefault(PlayerShow85sButtonPreference)
            setOnPreferenceChangeListener { _, newValue ->
                PlayerShow85sButtonPreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            playerAppearanceCategory.addPreference(this)
        }

        SwitchPreferenceCompat(this).apply {
            key = "playerShowScreenshotButton"
            title = getString(R.string.player_config_fragment_show_screenshot_button)
            setIcon(R.drawable.ic_photo_camera_24)
            isChecked =
                requireContext().dataStore.getOrDefault(PlayerShowScreenshotButtonPreference)
            setOnPreferenceChangeListener { _, newValue ->
                PlayerShowScreenshotButtonPreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            playerAppearanceCategory.addPreference(this)
        }

        val playerAdvancedCategory = PreferenceCategory(this).apply {
            key = "playerAdvancedCategory"
            title = getString(R.string.player_config_fragment_advanced_category)
            screen.addPreference(this)
        }

        Preference(this).apply {
            key = "playerAdvanced"
            title = getString(R.string.player_config_advanced_screen_name)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_player_config_advanced_fragment)
                true
            }
            playerAdvancedCategory.addPreference(this)
        }
    }
}