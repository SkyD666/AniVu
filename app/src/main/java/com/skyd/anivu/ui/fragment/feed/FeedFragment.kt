package com.skyd.anivu.ui.fragment.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentFeedBinding
import com.skyd.anivu.ext.addFabBottomPaddingHook
import com.skyd.anivu.ext.addInsetsByMargin
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.findMainNavController
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.paging.PagingAniSpanSize
import com.skyd.anivu.ui.adapter.variety.paging.PagingVarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.Feed1Proxy
import com.skyd.anivu.ui.component.dialog.InputDialogBuilder
import com.skyd.anivu.ui.fragment.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class FeedFragment : BaseFragment<FragmentFeedBinding>() {
    override val transitionProvider: () -> Unit = nullTransitionProvider

    private val feedViewModel by viewModels<FeedViewModel>()
    private val intents = Channel<FeedIntent>()

    private var waitingDialog: AlertDialog? = null
    private val adapter = PagingVarietyAdapter(
        mutableListOf(
            Feed1Proxy(
                onRemove = { intents.trySend(FeedIntent.RemoveFeed(it.url)) },
                onEdit = {
                    InputDialogBuilder(requireContext())
                        .setInitInputText(it.url)
                        .setHint(getString(R.string.feed_fragment_add_rss_hint))
                        .setPositiveButton { _, _, text ->
                            if (text.isNotBlank()) {
                                intents.trySend(
                                    FeedIntent.EditFeed(oldUrl = it.url, newUrl = text)
                                )
                            }
                        }
                        .setIcon(
                            AppCompatResources.getDrawable(
                                requireActivity(),
                                R.drawable.ic_rss_feed_24
                            )
                        )
                        .setTitle(R.string.edit)
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show()
                },
            )
        )
    )

    private fun updateState(feedState: FeedState) {
        if (feedState.loadingDialog) {
            if (waitingDialog == null || !waitingDialog!!.isShowing) {
                waitingDialog = MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_info_24)
                    .setTitle(R.string.waiting)
                    .setCancelable(false)
                    .setView(R.layout.layout_waiting_dialog)
                    .show()
                    .apply { findViewById<TextView>(R.id.tv_message)?.gone() }
            } else if (waitingDialog!!.isShowing) {
                waitingDialog?.show()
            }
        } else {
            waitingDialog?.dismiss()
            waitingDialog = null
        }
        when (val feedListState = feedState.feedListState) {
            is FeedListState.Failed -> {
                adapter.clear()
            }

            FeedListState.Init -> {
                adapter.clear()
            }

            FeedListState.Loading -> {
                adapter.clear()
            }

            is FeedListState.Success -> {
                adapter.submitDataAsync(feedListState.feedPagingData)
            }
        }
    }

    private fun showEvent(feedEvent: FeedEvent) {
        when (feedEvent) {
            is FeedEvent.InitFeetListResultEvent.Failed -> {
                showSnackbar(text = feedEvent.msg)
            }

            is FeedEvent.RemoveFeedResultEvent.Failed -> {
                showSnackbar(text = feedEvent.msg)
            }

            is FeedEvent.AddFeedResultEvent.Failed -> {
                showSnackbar(text = feedEvent.msg)
            }

            is FeedEvent.EditFeedResultEvent.Failed -> {
                showSnackbar(text = feedEvent.msg)
            }

            FeedEvent.EditFeedResultEvent.Success,
            FeedEvent.RemoveFeedResultEvent.Success,
            FeedEvent.AddFeedResultEvent.Success -> Unit
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intents
            .consumeAsFlow()
            .startWith(FeedIntent.Init)
            .onEach(feedViewModel::processIntent)
            .launchIn(lifecycleScope)

        feedViewModel.viewState.collectIn(this) { updateState(it) }
        feedViewModel.singleEvent.collectIn(this) { showEvent(it) }
    }

    override fun FragmentFeedBinding.initView() {
        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_to_search_fragment -> {
                    findMainNavController().navigate(
                        resId = R.id.action_to_search_fragment,
                        args = Bundle().apply {
                            putSerializable(
                                SearchFragment.SEARCH_DOMAIN_KEY,
                                SearchFragment.SearchDomain.Feed,
                            )
                        }
                    )
                    true
                }

                else -> false
            }
        }

        rvFeedFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = PagingAniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        rvFeedFragment.addItemDecoration(divider)
        rvFeedFragment.adapter = adapter

        fabFeedFragment.setOnClickListener {
            InputDialogBuilder(requireContext())
                .setHint(getString(R.string.feed_fragment_add_rss_hint))
                .setPositiveButton { _, _, text ->
                    if (text.isNotBlank()) {
                        intents.trySend(FeedIntent.AddFeed(text))
                    }
                }
                .setIcon(
                    AppCompatResources.getDrawable(
                        requireActivity(),
                        R.drawable.ic_rss_feed_24
                    )
                )
                .setTitle(R.string.add)
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }
    }

    override fun FragmentFeedBinding.setWindowInsets() {
        ablFeedFragment.addInsetsByPadding(top = true, left = true, right = true)
        fabFeedFragment.addInsetsByMargin(left = true, right = true)
        rvFeedFragment.addInsetsByPadding(
            left = true,
            right = true,
            hook = ::addFabBottomPaddingHook,
        )
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFeedBinding.inflate(inflater, container, false)
}