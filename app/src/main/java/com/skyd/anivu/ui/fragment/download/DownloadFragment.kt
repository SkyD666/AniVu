package com.skyd.anivu.ui.fragment.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentDownloadBinding
import com.skyd.anivu.ext.addFabBottomPaddingHook
import com.skyd.anivu.ext.addInsetsByMargin
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import com.skyd.anivu.model.worker.download.doIfMagnetOrTorrentLink
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.Download1Proxy
import com.skyd.anivu.ui.component.dialog.InputDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class DownloadFragment : BaseFragment<FragmentDownloadBinding>() {
    private val feedViewModel by viewModels<DownloadViewModel>()
    private val intents = Channel<DownloadIntent>()
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(
            Download1Proxy(
                adapter = this,
                onPause = {
                    DownloadTorrentWorker.pause(
                        context = requireContext(),
                        requestId = it.downloadRequestId,
                        link = it.link,
                    )
                },
                onResume = { video ->
                    DownloadTorrentWorker.startWorker(
                        context = requireContext(),
                        torrentLink = video.link,
                        requestId = video.downloadRequestId,
                    )
                },
                onCancel = { video ->
                    DownloadTorrentWorker.cancel(
                        context = requireContext(),
                        requestId = video.downloadRequestId,
                        link = video.link,
                        downloadingDirName = video.downloadingDirName,
                    )
                },
            )
        )
    }

    private fun updateState(downloadState: DownloadState) {
        when (val downloadListState = downloadState.downloadListState) {
            is DownloadListState.Failed -> {
                adapter.dataList = emptyList()
            }

            DownloadListState.Init -> {
                adapter.dataList = emptyList()
            }

            DownloadListState.Loading -> {
                adapter.dataList = emptyList()
            }

            is DownloadListState.Success -> {
                adapter.dataList = downloadListState.downloadInfoBeanList
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intents
            .consumeAsFlow()
            .startWith(DownloadIntent.Init)
            .onEach(feedViewModel::processIntent)
            .launchIn(lifecycleScope)

        feedViewModel.viewState.collectIn(this) { updateState(it) }
    }

    override fun FragmentDownloadBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }

        fabDownloadFragment.setOnClickListener {
            InputDialogBuilder(requireContext())
                .setHint(getString(R.string.download_fragment_add_download_hint))
                .setPositiveButton(getString(R.string.download)) { _, _, text ->
                    doIfMagnetOrTorrentLink(
                        link = text,
                        onMagnet = { intents.trySend(DownloadIntent.AddDownload(text)) },
                        onTorrent = { intents.trySend(DownloadIntent.AddDownload(text)) },
                        onUnsupported = { showSnackbar(getString(R.string.download_fragment_unsupported_link)) },
                    )
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                .setTitle(R.string.download)
                .setIcon(R.drawable.ic_download_2_24)
                .show()
        }

        rvDownloadFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        rvDownloadFragment.addItemDecoration(divider)
        rvDownloadFragment.adapter = adapter
    }

    override fun FragmentDownloadBinding.setWindowInsets() {
        ablDownloadFragment.addInsetsByPadding(top = true, left = true, right = true)
        fabDownloadFragment.addInsetsByMargin(bottom = true, right = true)
        rvDownloadFragment.addInsetsByPadding(
            bottom = true, left = true, right = true, hook = ::addFabBottomPaddingHook,
        )
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentDownloadBinding.inflate(inflater, container, false)
}