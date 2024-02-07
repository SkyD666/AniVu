package com.skyd.anivu.ui.fragment.video

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.config.Const
import com.skyd.anivu.databinding.FragmentVideoBinding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.ParentDirBean
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.activity.PlayActivity.Companion.VIDEO_URI_KEY
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.ParentDir1Proxy
import com.skyd.anivu.ui.adapter.variety.proxy.Video1Proxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class VideoFragment : BaseFragment<FragmentVideoBinding>() {
    companion object {
        const val PATH_KEY = "path"
        const val HAS_PARENT_DIR_KEY = "hasParentDir"
    }

    private val viewModel by viewModels<VideoViewModel>()
    private val path by lazy { arguments?.getString(PATH_KEY) ?: Const.VIDEO_DIR.path }
    private val hasParentDir by lazy { arguments?.getBoolean(HAS_PARENT_DIR_KEY) ?: false }
    private val intents = Channel<VideoIntent>()

    private var waitingDialog: AlertDialog? = null
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(
            Video1Proxy(
                adapter = this,
                onPlay = {
                    startActivity(
                        Intent(requireContext(), PlayActivity::class.java).apply {
                            putExtra(VIDEO_URI_KEY, it.file.toUri(requireContext()))
                        }
                    )
                },
                onOpenDir = {
                    findMainNavController().navigate(
                        R.id.action_to_video_fragment,
                        Bundle().apply {
                            putString(PATH_KEY, "${path}/${it.name}")
                            putBoolean(HAS_PARENT_DIR_KEY, true)
                        }
                    )
                },
            )
        )
        addProxy(ParentDir1Proxy(
            onClick = { findNavController().popBackStackWithLifecycle() }
        ))
    }
    private val parentDirBean = ParentDirBean()

    private fun updateState(videoState: VideoState) {
        if (videoState.loadingDialog) {
            if (waitingDialog == null || !waitingDialog!!.isShowing) {
                waitingDialog = MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_info_24)
                    .setTitle(R.string.info)
                    .setCancelable(false)
                    .setMessage(R.string.waiting)
                    .show()
            } else if (waitingDialog!!.isShowing) {
                waitingDialog?.show()
            }
        } else {
            waitingDialog?.dismiss()
            waitingDialog = null
        }
        when (val videoListState = videoState.videoListState) {
            is VideoListState.Failed -> {
                binding.srlVideoFragment.isRefreshing = false
                adapter.dataList = emptyList<Any>().addHeader()
            }

            VideoListState.Init -> {
                binding.srlVideoFragment.isRefreshing = true
                adapter.dataList = emptyList<Any>().addHeader()
            }

            VideoListState.Loading -> {
                binding.srlVideoFragment.isRefreshing = true
            }

            is VideoListState.Success -> {
                binding.srlVideoFragment.isRefreshing = false
                adapter.dataList = videoListState.videoList.addHeader()
            }
        }
    }

    private fun showEvent(videoEvent: VideoEvent) {
        when (videoEvent) {
            is VideoEvent.DeleteUriResultEvent.Failed -> {
                showSnackbar(text = videoEvent.msg)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkArgument(
            messageRes = R.string.article_fragment_feed_url_illegal,
            action = { !path.isNullOrBlank() },
            onSuccess = {
                intents
                    .consumeAsFlow()
                    .onEach(viewModel::processIntent)
                    .launchIn(lifecycleScope)

                viewModel.viewState.collectIn(this) { updateState(it) }
                viewModel.singleEvent.collectIn(this) { showEvent(it) }
            }
        )
    }

    override fun FragmentVideoBinding.initView() {
        if (hasParentDir) {
            topAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24)
            topAppBar.setNavigationOnClickListener {
                findNavController().popBackStackWithLifecycle()
            }
        } else {
            topAppBar.navigationIcon = null
        }

        fabVideoFragment.setOnClickListener {
            findMainNavController().navigate(R.id.action_to_download_fragment)
        }

        srlVideoFragment.setOnRefreshListener {
            intents.trySend(VideoIntent.Refresh(path))
        }

        rvVideoFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        rvVideoFragment.addItemDecoration(divider)
        rvVideoFragment.adapter = adapter
    }

    private fun List<Any>.addHeader() = if (hasParentDir) this + parentDirBean else this

    override fun onResume() {
        super.onResume()

        if (!path.isNullOrBlank()) {
            intents.trySend(VideoIntent.Refresh(path))
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentVideoBinding.inflate(inflater, container, false)
}
