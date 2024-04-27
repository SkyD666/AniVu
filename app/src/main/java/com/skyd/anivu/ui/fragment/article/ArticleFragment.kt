package com.skyd.anivu.ui.fragment.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.dialog.AniVuDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Article1Proxy
import com.skyd.anivu.ui.fragment.search.SearchFragment
import com.skyd.anivu.ui.local.LocalNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleFragment : BaseComposeFragment() {
    companion object {
        const val FEED_URLS_KEY = "feedUrls"
    }

    private val feedViewModel by viewModels<ArticleViewModel>()
    private val feedUrls by lazy { arguments?.getStringArrayList(FEED_URLS_KEY).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { ArticleScreen(feedUrls = feedUrls, viewModel = feedViewModel) }
}

@Composable
fun ArticleScreen(feedUrls: List<String>, viewModel: ArticleViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                title = { Text(text = stringResource(R.string.article_screen_name)) },
                actions = {
                    AniVuIconButton(
                        onClick = {
                            navController.navigate(
                                resId = R.id.action_to_search_fragment,
                                args = Bundle().apply {
                                    putSerializable(
                                        SearchFragment.SEARCH_DOMAIN_KEY,
                                        SearchFragment.SearchDomain.Article(feedUrls),
                                    )
                                }
                            )
                        },
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(id = R.string.article_screen_search_article),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) {
        if (feedUrls.isEmpty()) {
            AniVuDialog(
                visible = true,
                text = { Text(text = stringResource(id = R.string.article_fragment_feed_url_illegal)) },
                confirmButton = {
                    TextButton(onClick = { navController.popBackStackWithLifecycle() }) {
                        Text(text = stringResource(id = R.string.exit))
                    }
                }
            )
        } else {
            val dispatch = viewModel.getDispatcher(startWith = ArticleIntent.Init(feedUrls))
            val state = rememberPullRefreshState(
                refreshing = uiState.articleListState.loading,
                onRefresh = { dispatch(ArticleIntent.Refresh(feedUrls)) },
            )
            Box(modifier = Modifier.pullRefresh(state)) {
                when (val articleListState = uiState.articleListState) {
                    is ArticleListState.Failed -> {}
                    is ArticleListState.Init -> {}
                    is ArticleListState.Success -> {
                        ArticleList(
                            modifier = Modifier.fillMaxSize(),
                            articles = articleListState.articlePagingDataFlow.collectAsLazyPagingItems(),
                            contentPadding = it,
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = uiState.articleListState.loading,
                    state = state,
                    modifier = Modifier
                        .padding(it)
                        .align(Alignment.TopCenter),
                )
            }
        }
    }

    WaitingDialog(visible = uiState.loadingDialog)

    when (val event = uiEvent) {
        is ArticleEvent.InitArticleListResultEvent.Failed -> {
            snackbarHostState.showSnackbar(message = event.msg, scope = scope)
        }

        is ArticleEvent.RefreshArticleListResultEvent.Failed -> {
            snackbarHostState.showSnackbar(message = event.msg, scope = scope)
        }

        null -> Unit
    }
}

@Composable
private fun ArticleList(
    modifier: Modifier = Modifier,
    articles: LazyPagingItems<ArticleWithFeed>,
    contentPadding: PaddingValues,
) {
    val adapter = remember {
        LazyGridAdapter(mutableListOf(Article1Proxy()))
    }
    AniVuLazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Adaptive(250.dp),
        dataList = articles,
        adapter = adapter,
        contentPadding = contentPadding,
        key = { _, item -> (item as ArticleWithFeed).articleWithEnclosure.article.articleId },
    )
}