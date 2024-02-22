package com.skyd.anivu.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentSettingsBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.model.bean.settings.SettingsBaseBean
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.settings.SettingsBaseProxy
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(SettingsBaseProxy(adapter = this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.dataList = getSettingsList()
    }

    override fun FragmentSettingsBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }

        rvSettingsFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }
        rvSettingsFragment.adapter = adapter
    }

    override fun FragmentSettingsBinding.setWindowInsets() {
        ablSettingsFragment.addInsetsByPadding(top = true, left = true, right = true)
        // Fix: https://github.com/material-components/material-components-android/issues/1310
        ViewCompat.setOnApplyWindowInsetsListener(ctlSettingsFragment, null)
        rvSettingsFragment.addInsetsByPadding(bottom = true, left = true, right = true)
    }

    private fun getSettingsList(): List<Any> = mutableListOf(
        SettingsBaseBean(
            title = getString(R.string.data_fragment_name),
            description = getString(R.string.data_fragment_description),
            icon = AppCompatResources.getDrawable(
                requireContext(), R.drawable.ic_database_24
            )!!,
            action = { findMainNavController().navigate(R.id.action_to_data_fragment) }
        ),
    )

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSettingsBinding.inflate(inflater, container, false)
}