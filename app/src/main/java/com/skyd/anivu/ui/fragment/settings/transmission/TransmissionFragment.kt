package com.skyd.anivu.ui.fragment.settings.transmission

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.transmission.SeedingWhenCompletePreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TransmissionFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.transmission_fragment_name) }
    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        val transmissionBehaviorCategory = PreferenceCategory(this).apply {
            key = "transmissionBehaviorCategory"
            title = getString(R.string.transmission_fragment_transmission_behavior_category)
            screen.addPreference(this)
        }

        SwitchPreferenceCompat(this).apply {
            key = "seedingWhenComplete"
            title = getString(R.string.transmission_fragment_seeding_when_complete)
            summary = getString(R.string.transmission_fragment_seeding_when_complete_description)
            setIcon(R.drawable.ic_cloud_upload_24)
            isChecked = requireContext().dataStore.getOrDefault(SeedingWhenCompletePreference)
            setOnPreferenceChangeListener { _, newValue ->
                SeedingWhenCompletePreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            transmissionBehaviorCategory.addPreference(this)
        }
        val proxyCategory = PreferenceCategory(this).apply {
            key = "proxyCategory"
            title = getString(R.string.transmission_fragment_proxy_category)
            screen.addPreference(this)
        }
        Preference(this).apply {
            key = "proxy"
            title = getString(R.string.proxy_fragment_name)
            summary = getString(R.string.proxy_fragment_description)
            setIcon(R.drawable.ic_vpn_key_24)
            setOnPreferenceClickListener {
                findMainNavController().navigate(R.id.action_to_proxy_fragment)
                true
            }
            proxyCategory.addPreference(this)
        }
    }
}