package com.skyd.anivu.ui.fragment.more

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.color.MaterialColors
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentMoreBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.screenIsLand
import com.skyd.anivu.model.bean.MoreBean
import com.skyd.anivu.ui.adapter.decoration.AniVuItemDecoration
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.More1Proxy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreFragment : BaseFragment<FragmentMoreBinding>() {
    override val transitionProvider = nullTransitionProvider

    override fun FragmentMoreBinding.initView() {
        val adapter = VarietyAdapter(mutableListOf()).apply {
            dataList = getMoreBeanList()
            addProxy(More1Proxy(onClick = {
                val data = dataList[it]
                if (data is MoreBean) {
                    findMainNavController().navigate(data.navigateId)
                }
            }))
        }

        rvMoreFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }
        rvMoreFragment.addItemDecoration(AniVuItemDecoration())
        rvMoreFragment.adapter = adapter
    }

    override fun FragmentMoreBinding.setWindowInsets() {
        val isLand = requireContext().screenIsLand
        ablMoreFragment.addInsetsByPadding(top = true, left = !isLand, right = true)
        rvMoreFragment.addInsetsByPadding(left = !isLand, right = true)
    }

    private fun getMoreBeanList(): MutableList<MoreBean> {
        return mutableListOf(
            MoreBean(
                title = getString(R.string.settings_fragment_name),
                icon = AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_settings_24
                )!!,
                iconTint = MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorOnPrimary
                ),
                navigateId = R.id.action_to_settings_fragment,
                background = AppCompatResources.getDrawable(
                    requireContext(), R.drawable.shape_curly_corner
                )!!,
                backgroundTint = MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorPrimary
                ),
            ),
            MoreBean(
                title = getString(R.string.about_fragment_name),
                icon = AppCompatResources.getDrawable(
                    requireContext(), R.drawable.ic_info_24
                )!!,
                iconTint = MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorOnSecondary
                ),
                navigateId = R.id.action_to_about_fragment,
                background = AppCompatResources.getDrawable(
                    requireContext(), R.drawable.shape_clover
                )!!,
                backgroundTint = MaterialColors.getColor(
                    requireView(),
                    com.google.android.material.R.attr.colorSecondary
                ),
            ),
        )
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMoreBinding.inflate(inflater, container, false)
}