package com.skyd.anivu.ui.fragment.more

import android.view.LayoutInflater
import android.view.ViewGroup
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentMoreBinding
import com.skyd.anivu.ext.addInsetsByPadding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreFragment : BaseFragment<FragmentMoreBinding>() {
    override fun FragmentMoreBinding.setWindowInsets() {
        ablMoreFragment.addInsetsByPadding(top = true, left = true, right = true)
        rvMoreFragment.addInsetsByPadding(bottom = true, left = true, right = true)
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMoreBinding.inflate(inflater, container, false)
}