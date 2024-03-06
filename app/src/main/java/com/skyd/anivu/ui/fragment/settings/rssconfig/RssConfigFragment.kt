package com.skyd.anivu.ui.fragment.settings.rssconfig

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.rss.ParseLinkTagAsEnclosurePreference
import com.skyd.anivu.model.preference.rss.RssSyncBatteryNotLowConstraintPreference
import com.skyd.anivu.model.preference.rss.RssSyncChargingConstraintPreference
import com.skyd.anivu.model.preference.rss.RssSyncFrequencyPreference
import com.skyd.anivu.model.preference.rss.RssSyncWifiConstraintPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RssConfigFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.rss_config_fragment_name) }
    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        val rssSyncCategory = PreferenceCategory(this).apply {
            key = "rssSyncCategory"
            title = getString(R.string.rss_config_fragment_sync_category)
            screen.addPreference(this)
        }
        DropDownPreference(this).apply {
            key = "rssSyncFrequency"
            title = getString(R.string.rss_config_fragment_sync_frequency)
            setIcon(R.drawable.ic_timer_24)
            value = context.dataStore.getOrDefault(RssSyncFrequencyPreference).toString()
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            entries = RssSyncFrequencyPreference.frequencies.map {
                RssSyncFrequencyPreference.toDisplayName(context, it)
            }.toTypedArray()
            entryValues =
                RssSyncFrequencyPreference.frequencies.map { it.toString() }.toTypedArray()
            setOnPreferenceChangeListener { _, newValue ->
                RssSyncFrequencyPreference.put(
                    requireContext(), lifecycleScope, (newValue as String).toLong(),
                )
                true
            }
            rssSyncCategory.addPreference(this)
        }
        SwitchPreferenceCompat(this).apply {
            key = "rssSyncWifiConstraint"
            title = getString(R.string.rss_config_fragment_sync_wifi_constraint)
            setIcon(R.drawable.ic_wifi_24)
            isChecked = requireContext().dataStore.getOrDefault(RssSyncWifiConstraintPreference)
            setOnPreferenceChangeListener { _, newValue ->
                RssSyncWifiConstraintPreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            rssSyncCategory.addPreference(this)
        }
        SwitchPreferenceCompat(this).apply {
            key = "rssSyncChargingConstraint"
            title = getString(R.string.rss_config_fragment_sync_charging_constraint)
            setIcon(R.drawable.ic_battery_charging_full_24)
            isChecked = requireContext().dataStore.getOrDefault(RssSyncChargingConstraintPreference)
            setOnPreferenceChangeListener { _, newValue ->
                RssSyncChargingConstraintPreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            rssSyncCategory.addPreference(this)
        }
        SwitchPreferenceCompat(this).apply {
            key = "rssSyncBatteryNotLowConstraint"
            title = getString(R.string.rss_config_fragment_sync_battery_not_low_constraint)
            setIcon(R.drawable.ic_battery_full_alt_24)
            isChecked =
                requireContext().dataStore.getOrDefault(RssSyncBatteryNotLowConstraintPreference)
            setOnPreferenceChangeListener { _, newValue ->
                RssSyncBatteryNotLowConstraintPreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            rssSyncCategory.addPreference(this)
        }

        val rssParseCategory = PreferenceCategory(this).apply {
            key = "rssParseCategory"
            title = getString(R.string.rss_config_fragment_parse_category)
            screen.addPreference(this)
        }

        SwitchPreferenceCompat(this).apply {
            key = "parseLinkTagAsEnclosure"
            title = getString(R.string.rss_config_fragment_parse_link_tag_as_enclosure)
            summary =
                getString(R.string.rss_config_fragment_parse_link_tag_as_enclosure_description)
            setIcon(R.drawable.ic_link_24)
            isChecked = requireContext().dataStore.getOrDefault(ParseLinkTagAsEnclosurePreference)
            setOnPreferenceChangeListener { _, newValue ->
                ParseLinkTagAsEnclosurePreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = newValue as Boolean,
                )
                true
            }
            rssParseCategory.addPreference(this)
        }
    }
}