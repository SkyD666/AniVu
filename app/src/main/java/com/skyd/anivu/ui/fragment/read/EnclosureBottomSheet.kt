package com.skyd.anivu.ui.fragment.read

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseBottomSheetDialogFragment
import com.skyd.anivu.databinding.BottomSheetEnclosureBinding
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.bean.LinkEnclosureBean
import com.skyd.anivu.model.worker.download.DownloadTorrentWorker
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.Enclosure1Proxy
import com.skyd.anivu.ui.adapter.variety.proxy.LinkEnclosure1Proxy

class EnclosureBottomSheet : BaseBottomSheetDialogFragment<BottomSheetEnclosureBinding>() {
    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private val registerPostNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        if (!resultMap.containsValue(false)) {
            waitingDownloadList.removeIf {
                val url = when (it) {
                    is EnclosureBean -> it.url
                    is LinkEnclosureBean -> it.link
                    else -> return@removeIf true
                }

                DownloadTorrentWorker.startWorker(
                    context = requireContext(),
                    torrentLink = url
                )
                true
            }
        }
    }

    private val waitingDownloadList = mutableListOf<Any>()

    private val onDownload: (Any) -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            waitingDownloadList += it
            registerPostNotificationPermission.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
                    )
                } else arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
            )
        } else {
            val url = when (it) {
                is EnclosureBean -> it.url
                is LinkEnclosureBean -> it.link
                else -> null
            }
            if (!url.isNullOrBlank()) {
                DownloadTorrentWorker.startWorker(
                    context = requireContext(), torrentLink = url
                )
            }
        }
    }

    private val adapter = VarietyAdapter(
        mutableListOf(
            Enclosure1Proxy(onDownload = onDownload, onPlay = {
                startActivity(
                    Intent(requireContext(), PlayActivity::class.java).apply {
                        putExtra(PlayActivity.VIDEO_URI_KEY, it.url.toUri())
                    }
                )
            }),
            LinkEnclosure1Proxy(onDownload = onDownload),
        )
    )

    fun updateData(dataList: List<Any>) {
        adapter.dataList = dataList
    }

    override fun BottomSheetEnclosureBinding.initView() {
        rvBottomSheetEnclosure.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        divider.setDividerInsetStartResource(requireContext(), R.dimen.divider_inset_start)
        divider.setDividerInsetEndResource(requireContext(), R.dimen.divider_inset_end)
        rvBottomSheetEnclosure.addItemDecoration(divider)
        rvBottomSheetEnclosure.adapter = adapter
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        BottomSheetEnclosureBinding.inflate(inflater, container, false)
}