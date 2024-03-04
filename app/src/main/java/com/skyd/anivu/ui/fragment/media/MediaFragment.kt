package com.skyd.anivu.ui.fragment.media

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
import com.skyd.anivu.databinding.FragmentMediaBinding
import com.skyd.anivu.ext.addFabBottomPaddingHook
import com.skyd.anivu.ext.addInsetsByMargin
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.ParentDirBean
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.activity.PlayActivity.Companion.VIDEO_URI_KEY
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.Media1Proxy
import com.skyd.anivu.ui.adapter.variety.proxy.ParentDir1Proxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MediaFragment : BaseFragment<FragmentMediaBinding>() {
    companion object {
        const val PATH_KEY = "path"
        const val HAS_PARENT_DIR_KEY = "hasParentDir"
    }

    private val viewModel by viewModels<MediaViewModel>()
    private val path by lazy { arguments?.getString(PATH_KEY) ?: Const.VIDEO_DIR.path }
    private val hasParentDir by lazy { arguments?.getBoolean(HAS_PARENT_DIR_KEY) ?: false }
    private val intents = Channel<MediaIntent>()

    private var waitingDialog: AlertDialog? = null
    private val adapter = VarietyAdapter(mutableListOf()).apply {
        addProxy(
            Media1Proxy(
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
                onRemove = {
                    intents.trySend(MediaIntent.Delete(it.file))
                },
                coroutineScope = lifecycleScope,
            )
        )
        addProxy(ParentDir1Proxy(
            onClick = { findNavController().popBackStackWithLifecycle() }
        ))
    }
    private val parentDirBean = ParentDirBean()

    private fun updateState(mediaState: MediaState) {
        if (mediaState.loadingDialog) {
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
        when (val videoListState = mediaState.mediaListState.apply {
            binding.srlMediaFragment.isRefreshing = loading
        }) {
            is MediaListState.Failed -> {
                adapter.dataList = emptyList<Any>().addHeader()
            }

            MediaListState.Init -> {
                adapter.dataList = emptyList<Any>().addHeader()
            }

            is MediaListState.Success -> {
                adapter.dataList = videoListState.list.addHeader()
            }
        }
    }

    private fun showEvent(mediaEvent: MediaEvent) {
        when (mediaEvent) {
            is MediaEvent.DeleteUriResultEvent.Failed -> {
                showSnackbar(text = mediaEvent.msg)
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

    override fun FragmentMediaBinding.initView() {
        if (hasParentDir) {
            topAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24)
            topAppBar.setNavigationOnClickListener {
                findNavController().popBackStackWithLifecycle()
            }
        } else {
            topAppBar.navigationIcon = null
        }

        fabMediaFragment.setOnClickListener {
            findMainNavController().navigate(R.id.action_to_download_fragment)
        }

        srlMediaFragment.setOnRefreshListener {
            intents.trySend(MediaIntent.Refresh(path))
        }

        rvMediaFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        rvMediaFragment.addItemDecoration(divider)
        rvMediaFragment.adapter = adapter
    }

    override fun FragmentMediaBinding.setWindowInsets() {
        ablMediaFragment.addInsetsByPadding(top = true, left = true, right = true)
        fabMediaFragment.addInsetsByMargin(left = true, right = true, bottom = hasParentDir)
        rvMediaFragment.addInsetsByPadding(
            left = true,
            right = true,
            hook = ::addFabBottomPaddingHook,
        )
    }

    private fun List<Any>.addHeader() = if (hasParentDir) {
        toMutableList().apply { add(0, parentDirBean) }
    } else this

    override fun onResume() {
        super.onResume()

        if (!path.isNullOrBlank()) {
            intents.trySend(MediaIntent.Refresh(path))
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMediaBinding.inflate(inflater, container, false)
}
