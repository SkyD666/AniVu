package com.skyd.anivu.ui.fragment.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.config.Const
import com.skyd.anivu.databinding.FragmentAboutBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.getAppVersionName
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.model.bean.OtherWorksBean
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.OtherWorks1Proxy
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>() {
    private val adapter = VarietyAdapter(
        mutableListOf()
    ).apply {
        addProxy(OtherWorks1Proxy(adapter = this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.dataList += mutableListOf(
            OtherWorksBean(
                name = getString(R.string.about_fragment_other_works_rays_name),
                icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_rays)!!,
                description = getString(R.string.about_fragment_other_works_rays_description),
                url = Const.RAYS_ANDROID_URL,
            ),
            OtherWorksBean(
                name = getString(R.string.about_fragment_other_works_raca_name),
                icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_raca)!!,
                description = getString(R.string.about_fragment_other_works_raca_description),
                url = Const.RACA_ANDROID_URL,
            ),
            OtherWorksBean(
                name = getString(R.string.about_fragment_other_works_night_screen_name),
                icon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_night_screen
                )!!,
                description = getString(R.string.about_fragment_other_works_night_screen_description),
                url = Const.NIGHT_SCREEN_URL,
            ),
        )
    }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun FragmentAboutBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }
        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_about_fragment_license -> {
                    findNavController().navigate(R.id.action_to_license_fragment)
                    true
                }

                else -> false
            }
        }

        val badgeDrawable = BadgeDrawable.create(requireContext())
        badgeDrawable.isVisible = true
        badgeDrawable.text = requireContext().getAppVersionName()
        BadgeUtils.attachBadgeDrawable(badgeDrawable, tvAboutFragmentAppName)

        btnAboutFragmentGithub.setOnClickListener {
            it.context.openBrowser(Const.GITHUB_REPO)
        }
        btnAboutFragmentTelegram.setOnClickListener {
            it.context.openBrowser(Const.TELEGRAM_GROUP)
        }
        btnAboutFragmentDiscord.setOnClickListener {
            it.context.openBrowser(Const.DISCORD_SERVER)
        }

        rvAboutFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }
        rvAboutFragment.adapter = adapter
    }

    override fun FragmentAboutBinding.setWindowInsets() {
        ablAboutFragment.addInsetsByPadding(top = true, left = true, right = true)
        // Fix: https://github.com/material-components/material-components-android/issues/1310
        ViewCompat.setOnApplyWindowInsetsListener(ctlAboutFragment, null)
        nsvAboutFragment.addInsetsByPadding(bottom = true, left = true, right = true)
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAboutBinding.inflate(inflater, container, false)
}