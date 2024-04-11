package com.skyd.anivu.ui.fragment.license

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentLicenseBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.dp
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.model.bean.LicenseBean
import com.skyd.anivu.ui.adapter.decoration.AniVuItemDecoration
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.License1Proxy
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LicenseFragment : BaseFragment<FragmentLicenseBinding>() {
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(License1Proxy(adapter = this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter.dataList = licenseList
    }

    override fun FragmentLicenseBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }

        rvLicenseFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }
        rvLicenseFragment.addItemDecoration(AniVuItemDecoration())
        rvLicenseFragment.adapter = adapter
    }

    override fun FragmentLicenseBinding.setWindowInsets() {
        ablLicenseFragment.addInsetsByPadding(top = true, left = true, right = true)
        // Fix: https://github.com/material-components/material-components-android/issues/1310
        ViewCompat.setOnApplyWindowInsetsListener(ctlLicenseFragment, null)
        rvLicenseFragment.addInsetsByPadding(bottom = true, left = true, right = true)
    }

    private val licenseList = mutableListOf(
        LicenseBean(
            name = "Android Open Source Project",
            license = "Apache-2.0",
            link = "https://source.android.com/",
        ),
        LicenseBean(
            name = "Material Components for Android",
            license = "Apache-2.0",
            link = "https://github.com/material-components/material-components-android",
        ),
        LicenseBean(
            name = "Hilt",
            license = "Apache-2.0",
            link = "https://github.com/googlecodelabs/android-hilt",
        ),
        LicenseBean(
            name = "OkHttp",
            license = "Apache-2.0",
            link = "https://github.com/square/okhttp",
        ),
        LicenseBean(
            name = "Coil",
            license = "Apache-2.0",
            link = "https://github.com/coil-kt/coil",
        ),
        LicenseBean(
            name = "kotlinx.coroutines",
            license = "Apache-2.0",
            link = "https://github.com/Kotlin/kotlinx.coroutines",
        ),
        LicenseBean(
            name = "kotlinx.serialization",
            license = "Apache-2.0",
            link = "https://github.com/Kotlin/kotlinx.serialization",
        ),
        LicenseBean(
            name = "Retrofit",
            license = "Apache-2.0",
            link = "https://github.com/square/retrofit",
        ),
        LicenseBean(
            name = "Kotlin Serialization Converter",
            license = "Apache-2.0",
            link = "https://github.com/JakeWharton/retrofit2-kotlinx-serialization-converter",
        ),
        LicenseBean(
            name = "ROME",
            license = "Apache-2.0",
            link = "https://github.com/rometools/rome",
        ),
        LicenseBean(
            name = "Read You",
            license = "GPL-3.0",
            link = "https://github.com/Ashinch/ReadYou",
        ),
        LicenseBean(
            name = "Readability4J",
            license = "Apache-2.0",
            link = "https://github.com/dankito/Readability4J",
        ),
        LicenseBean(
            name = "libtorrent4j",
            license = "MIT",
            link = "https://github.com/aldenml/libtorrent4j",
        ),
    ).sortedBy { it.name }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentLicenseBinding.inflate(inflater, container, false)
}