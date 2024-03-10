package com.skyd.anivu.ui.fragment.read

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentReadBinding
import com.skyd.anivu.ext.addFabBottomPaddingHook
import com.skyd.anivu.ext.addInsetsByMargin
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.ifNullOfBlank
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ext.toHtml
import com.skyd.anivu.model.bean.LinkEnclosureBean
import com.skyd.anivu.model.preference.rss.ParseLinkTagAsEnclosurePreference
import com.skyd.anivu.model.worker.download.doIfMagnetOrTorrentLink
import com.skyd.anivu.util.html.ImageGetter
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
                showExitDialog(message = articleState.msg)
                enclosureBottomSheet?.updateData(emptyList())
                binding.topAppBar.menu?.findItem(R.id.action_read_fragment_open_in_browser)
                    ?.isEnabled = false
            }

            ArticleState.Init -> {
                enclosureBottomSheet?.updateData(emptyList())
                binding.topAppBar.menu?.findItem(R.id.action_read_fragment_open_in_browser)
                    ?.isEnabled = false
            }

            ArticleState.Loading -> {
                enclosureBottomSheet?.updateData(emptyList())
                binding.topAppBar.menu?.findItem(R.id.action_read_fragment_open_in_browser)
                    ?.isEnabled = false
            }

            is ArticleState.Success -> {
                binding.topAppBar.menu?.findItem(R.id.action_read_fragment_open_in_browser)
                    ?.isEnabled = true
                val article = articleState.article
                binding.tvReadFragmentContent.post {
                    binding.tvReadFragmentContent.text = article.article.content
                        .ifNullOfBlank { article.article.description.orEmpty() }
                        .toHtml(
                            imageGetter = ImageGetter(
                                context = requireContext(),
                                maxWidth = { binding.tvReadFragmentContent.width },
                                onSuccess = { _, _ ->
                                    binding.tvReadFragmentContent.text =
                                        binding.tvReadFragmentContent.text
                                }
                            ),
                            tagHandler = null,
                        )
                }

                val dataList: MutableList<Any> = article.enclosures.toMutableList()
                if (requireContext().dataStore.getOrDefault(ParseLinkTagAsEnclosurePreference)) {
                    article.article.link?.let { link ->
                        doIfMagnetOrTorrentLink(
                            link = link,
                            onMagnet = { dataList += LinkEnclosureBean(link = link) },
                            onTorrent = { dataList += LinkEnclosureBean(link = link) },
                        )
                    }
                }
                enclosureBottomSheet?.updateData(dataList)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkArgument(
            messageRes = R.string.read_fragment_article_id_illegal,
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
        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_read_fragment_open_in_browser -> {
                    val articleState = feedViewModel.viewState.value.articleState
                    if (articleState is ArticleState.Success) {
                        val link = articleState.article.article.link
                        if (!link.isNullOrBlank()) {
                            link.openBrowser(requireContext())
                        }
                    }
                    true
                }

                else -> false
            }
        }
        tvReadFragmentContent.movementMethod = LinkMovementMethod.getInstance()

        fabReadFragment.setOnClickListener {
            enclosureBottomSheet = EnclosureBottomSheet()
            enclosureBottomSheet?.show(
                requireActivity().supportFragmentManager,
                EnclosureBottomSheet.TAG
            )
            val articleState = feedViewModel.viewState.value.articleState
            if (articleState is ArticleState.Success) {
                val dataList: MutableList<Any> = articleState.article.enclosures.toMutableList()
                if (requireContext().dataStore.getOrDefault(ParseLinkTagAsEnclosurePreference)) {
                    articleState.article.article.link?.let { link ->
                        doIfMagnetOrTorrentLink(
                            link = link,
                            onMagnet = { dataList += LinkEnclosureBean(link = link) },
                            onTorrent = { dataList += LinkEnclosureBean(link = link) },
                        )
                    }
                }
                enclosureBottomSheet?.updateData(dataList)
            }
        }
    }

    override fun FragmentReadBinding.setWindowInsets() {
        ablReadFragment.addInsetsByPadding(top = true, left = true, right = true)
        fabReadFragment.addInsetsByMargin(bottom = true, right = true)
        tvReadFragmentContent.addInsetsByPadding(
            bottom = true, left = true, right = true, hook = ::addFabBottomPaddingHook
        )
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentReadBinding.inflate(inflater, container, false)
}