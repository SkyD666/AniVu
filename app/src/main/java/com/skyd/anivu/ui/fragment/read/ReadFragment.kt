package com.skyd.anivu.ui.fragment.read

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentReadBinding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.startWith
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ReadFragment : BaseFragment<FragmentReadBinding>() {
    companion object {
        const val ARTICLE_ID_KEY = "articleId"
    }

    private val feedViewModel by viewModels<ReadViewModel>()
    private val articleId by lazy { arguments?.getString(ARTICLE_ID_KEY) }
    private val intents = Channel<ReadIntent>()

    private var enclosureBottomSheet: EnclosureBottomSheet? = null

    private fun updateState(readState: ReadState) {
        when (val articleState = readState.articleState) {
            is ArticleState.Failed -> {
                enclosureBottomSheet?.updateData(emptyList())
            }

            ArticleState.Init -> {
                enclosureBottomSheet?.updateData(emptyList())
            }

            ArticleState.Loading -> {
                enclosureBottomSheet?.updateData(emptyList())
            }

            is ArticleState.Success -> {
                enclosureBottomSheet?.updateData(articleState.article.enclosures)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkArgument(
            messageRes = R.string.article_fragment_feed_url_illegal,
            action = { !articleId.isNullOrBlank() },
            onSuccess = {
                intents
                    .consumeAsFlow()
                    .startWith(ReadIntent.Init(articleId!!))
                    .onEach(feedViewModel::processIntent)
                    .launchIn(lifecycleScope)

                feedViewModel.viewState.collectIn(this) { updateState(it) }
            }
        )
    }

    override fun FragmentReadBinding.initView() {
        topAppBar.setNavigationOnClickListener { findNavController().popBackStackWithLifecycle() }
        fabReadFragment.setOnClickListener {
            enclosureBottomSheet = EnclosureBottomSheet()
            enclosureBottomSheet?.show(
                requireActivity().supportFragmentManager,
                EnclosureBottomSheet.TAG
            )
            val articleState = feedViewModel.viewState.value.articleState
            if (articleState is ArticleState.Success) {
                enclosureBottomSheet?.updateData(articleState.article.enclosures)
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentReadBinding.inflate(inflater, container, false)
}