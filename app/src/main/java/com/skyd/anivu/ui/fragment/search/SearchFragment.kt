package com.skyd.anivu.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.skyd.anivu.base.BaseFragment
import com.skyd.anivu.databinding.FragmentSearchBinding
import com.skyd.anivu.ext.addInsetsByPadding
import com.skyd.anivu.ext.collectIn
import com.skyd.anivu.ext.gone
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSoftKeyboard
import com.skyd.anivu.ext.startWith
import com.skyd.anivu.ext.visible
import com.skyd.anivu.ui.adapter.variety.AniSpanSize
import com.skyd.anivu.ui.adapter.variety.VarietyAdapter
import com.skyd.anivu.ui.adapter.variety.proxy.Article1Proxy
import com.skyd.anivu.ui.adapter.variety.proxy.Feed1Proxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.Serializable


@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    @kotlinx.serialization.Serializable
    sealed interface SearchDomain : Serializable {
        data object All : SearchDomain
        data object Feed : SearchDomain
        data class Article(val feedUrl: String?) : SearchDomain
    }

    companion object {
        const val SEARCH_DOMAIN_KEY = "searchDomain"
    }

    private val viewModel by viewModels<SearchViewModel>()
    private val searchDomain by lazy {
        (arguments?.getSerializable(SEARCH_DOMAIN_KEY) as? SearchDomain) ?: SearchDomain.All
    }
    private val intents = Channel<SearchIntent>()

    private val adapter = VarietyAdapter(
        mutableListOf(Feed1Proxy(), Article1Proxy())
    )

    private fun updateState(searchState: SearchState) {
        when (val searchResultState = searchState.searchResultState) {
            is SearchResultState.Failed -> {
                binding.cpiSearchFragment.gone()
                adapter.dataList = emptyList()
            }

            SearchResultState.Init -> {
                binding.cpiSearchFragment.gone()
            }

            SearchResultState.Loading -> {
                adapter.dataList = emptyList()
                binding.cpiSearchFragment.visible()
            }

            is SearchResultState.Success -> {
                binding.cpiSearchFragment.gone()
                adapter.dataList = searchResultState.result
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intents
            .consumeAsFlow()
            .startWith(SearchIntent.Init)
            .onEach(viewModel::processIntent)
            .launchIn(lifecycleScope)

        viewModel.viewState.collectIn(this) { updateState(it) }
    }

    override fun FragmentSearchBinding.initView() {
        tilSearchFragment.setStartIconOnClickListener { findNavController().popBackStackWithLifecycle() }
        tilSearchFragment.editText?.apply {
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch(v.text.toString())
                    true
                } else false
            }
            showSoftKeyboard(requireActivity().window)
        }

        rvSearchFragment.layoutManager = GridLayoutManager(
            requireContext(),
            AniSpanSize.MAX_SPAN_SIZE
        ).apply {
            spanSizeLookup = AniSpanSize(adapter)
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        rvSearchFragment.addItemDecoration(divider)
        rvSearchFragment.adapter = adapter
    }

    private fun doSearch(query: String) {
        when (val searchDomain = searchDomain) {
            SearchDomain.All -> intents.trySend(SearchIntent.SearchAll(query = query))
            SearchDomain.Feed -> intents.trySend(SearchIntent.SearchFeed(query = query))
            is SearchDomain.Article -> {
                intents.trySend(
                    SearchIntent.SearchArticle(
                        feedUrl = searchDomain.feedUrl,
                        query = query,
                    )
                )
            }
        }
    }

    override fun FragmentSearchBinding.setWindowInsets() {
        tilSearchFragment.addInsetsByPadding(top = true, left = true, right = true)
//        fabReadFragment.addInsetsByMargin(bottom = true, right = true)
//        tvReadFragmentContent.addInsetsByPadding(
//            bottom = true, left = true, right = true, hook = ::addFabBottomPaddingHook
//        )
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSearchBinding.inflate(inflater, container, false)
}