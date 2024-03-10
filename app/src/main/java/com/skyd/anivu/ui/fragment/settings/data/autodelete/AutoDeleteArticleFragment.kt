package com.skyd.anivu.ui.fragment.settings.data.autodelete

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import com.skyd.anivu.R
import com.skyd.anivu.base.BasePreferenceFragmentCompat
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.autodelete.AutoDeleteArticleBeforePreference
import com.skyd.anivu.model.preference.autodelete.AutoDeleteArticleFrequencyPreference
import com.skyd.anivu.model.preference.autodelete.UseAutoDeletePreference
import com.skyd.anivu.ui.component.preference.BannerSwitchPreference
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AutoDeleteArticleFragment : BasePreferenceFragmentCompat() {
    override val title by lazy { resources.getString(R.string.auto_delete_article_fragment_name) }
    private lateinit var bannerSwitchPreference: BannerSwitchPreference
    private lateinit var deleteArticleFrequencyPreference: DropDownPreference
    private lateinit var deleteArticleBeforePreference: DropDownPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        deleteArticleFrequencyPreference.dependency = bannerSwitchPreference.key
        deleteArticleBeforePreference.dependency = bannerSwitchPreference.key
    }

    override fun Context.onAddPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
        screen: PreferenceScreen
    ) {
        bannerSwitchPreference = BannerSwitchPreference(this).apply {
            key = "useAutoDeleteArticle"
            title = getString(R.string.auto_delete_article_fragment_use)
            isChecked = requireContext().dataStore.getOrDefault(UseAutoDeletePreference)
            setIcon(R.drawable.ic_auto_delete_24)
            setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                setIcon(R.drawable.ic_auto_delete_24)
                UseAutoDeletePreference.put(requireContext(), lifecycleScope, value)
                true
            }
            screen.addPreference(this)
        }
        deleteArticleFrequencyPreference = DropDownPreference(this).apply {
            key = "autoDeleteArticleFrequency"
            title = getString(R.string.auto_delete_article_fragment_delete_frequency)
            setIcon(R.drawable.ic_timer_24)
            value = context.dataStore.getOrDefault(AutoDeleteArticleFrequencyPreference).toString()
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            entries = AutoDeleteArticleFrequencyPreference.frequencies.map {
                AutoDeleteArticleFrequencyPreference.toDisplayName(context, it)
            }.toTypedArray()
            entryValues = AutoDeleteArticleFrequencyPreference.frequencies
                .map { it.toString() }
                .toTypedArray()
            setOnPreferenceChangeListener { _, newValue ->
                AutoDeleteArticleFrequencyPreference.put(
                    requireContext(), lifecycleScope, (newValue as String).toLong(),
                )
                true
            }
            screen.addPreference(this)
        }
        deleteArticleBeforePreference = DropDownPreference(this).apply {
            key = "autoDeleteArticleBefore"
            title = getString(R.string.auto_delete_article_fragment_delete_before)
            setIcon(R.drawable.ic_calendar_clock_24)
            value = context.dataStore.getOrDefault(AutoDeleteArticleBeforePreference).toString()
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            entries = AutoDeleteArticleBeforePreference.frequencies.map {
                AutoDeleteArticleBeforePreference.toDisplayName(context, it)
            }.toTypedArray()
            entryValues = AutoDeleteArticleBeforePreference.frequencies
                .map { it.toString() }
                .toTypedArray()
            setOnPreferenceChangeListener { _, newValue ->
                AutoDeleteArticleBeforePreference.put(
                    requireContext(), lifecycleScope, (newValue as String).toLong(),
                )
                true
            }
            screen.addPreference(this)
        }
    }
}