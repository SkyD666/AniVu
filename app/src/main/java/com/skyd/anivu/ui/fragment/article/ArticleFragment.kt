package com.skyd.anivu.ui.fragment.article

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
import com.skyd.anivu.databinding.FragmentArticleBinding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.Article1Proxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ArticleFragment : BaseFragment<FragmentArticleBinding>() {
    companion object {
        const val FEED_URL_KEY = "feedUrl"
    }

    private val feedViewModel by viewModels<ArticleViewModel>()
    private val feedUrl by lazy { arguments?.getString(FEED_URL_KEY) }
    private val intents = Channel<ArticleIntent>()

    private var waitingDialog: AlertDialog? = null
    private val adapter = VarietyAdapter(
        mutableListOf(Article1Proxy())
    )

    private fun updateState(articleState: ArticleState) {
        if (articleState.loadingDialog) {
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
        when (val feedListState = articleState.articleListState.apply {
            binding.srlArticleFragment.isRefreshing = loading
        }) {
            is ArticleListState.Failed -> {
                adapter.dataList = emptyList()
            }

            ArticleListState.Init -> {
                adapter.dataList = emptyList()
            }

            is ArticleListState.Success -> {
                adapter.dataList = feedListState.articleList
            }
        }
    }

    private fun showEvent(feedEvent: ArticleEvent) {
        when (feedEvent) {
            is ArticleEvent.InitArticleListResultEvent.Failed -> {
                showSnackbar(text = feedEvent.msg)
            }

            is ArticleEvent.RefreshArticleListResultEvent.Failed -> {
                showSnackbar(text = feedEvent.msg)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkArgument(
            messageRes = R.string.article_fragment_feed_url_illegal,
            action = { !feedUrl.isNullOrBlank() },
            onSuccess = {
                intents
                    .consumeAsFlow()
                    .startWith(ArticleIntent.Init(feedUrl!!))
                    .onEach(feedViewModel::processIntent)
                    .launchIn(lifecycleScope)

                feedViewModel.viewState.collectIn(this) { updateState(it) }
                feedViewModel.singleEvent.collectIn(this) { showEvent(it) }
            }
        )
    }

    override fun FragmentArticleBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }

        srlArticleFragment.setOnRefreshListener {
            intents.trySend(ArticleIntent.Refresh(feedUrl!!))
        }

        rvArticleFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        rvArticleFragment.addItemDecoration(divider)
        rvArticleFragment.adapter = adapter
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentArticleBinding.inflate(inflater, container, false)
}