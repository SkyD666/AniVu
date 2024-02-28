package com.skyd.anivu.ui.fragment.settings.rssconfig

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentRssConfigBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.model.bean.settings.SettingsSwitchBean
import com.skyd.anivu.model.preference.rss.ParseLinkTagAsEnclosurePreference
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.settings.SettingsBaseProxy
import com.skyd.anivu.ui.adapter.variety.proxy.settings.SettingsSwitchProxy
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RssConfigFragment : BaseFragment<FragmentRssConfigBinding>() {
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(SettingsBaseProxy(adapter = this))
        addProxy(SettingsSwitchProxy(adapter = this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.dataList = getSettingsList()
    }

    override fun FragmentRssConfigBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }

        rvRssConfigFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }
        rvRssConfigFragment.adapter = adapter
    }

    override fun FragmentRssConfigBinding.setWindowInsets() {
        ablRssConfigFragment.addInsetsByPadding(top = true, left = true, right = true)
        // Fix: https://github.com/material-components/material-components-android/issues/1310
        ViewCompat.setOnApplyWindowInsetsListener(ctlRssConfigFragment, null)
        rvRssConfigFragment.addInsetsByPadding(bottom = true, left = true, right = true)
    }

    private fun getSettingsList(): List<Any> = mutableListOf(
        SettingsSwitchBean(
            title = getString(R.string.rss_config_fragment_parse_link_tag_as_enclosure),
            description = getString(R.string.rss_config_fragment_parse_link_tag_as_enclosure_description),
            icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_link_24)!!,
            isChecked = { requireContext().dataStore.getOrDefault(ParseLinkTagAsEnclosurePreference) },
            onCheckedChanged = {
                ParseLinkTagAsEnclosurePreference.put(
                    context = requireContext(),
                    scope = lifecycleScope,
                    value = it,
                )
            },
        ),
    )

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRssConfigBinding.inflate(inflater, container, false)
}