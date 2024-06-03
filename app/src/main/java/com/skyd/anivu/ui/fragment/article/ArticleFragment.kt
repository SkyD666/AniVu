package com.skyd.anivu.ui.fragment.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.skyd.anivu.ui.component.BackIcon
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

private val DefaultBackClick = { }

@Composable
fun ArticleScreen(
    feedUrls: List<String>,
    onBackClick: () -> Unit = DefaultBackClick,
    viewModel: ArticleViewModel = hiltViewModel(),
) {
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
                navigationIcon = {
                    if (onBackClick == DefaultBackClick) BackIcon()
                    else BackIcon(onClick = onBackClick)
                },
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
    ) { paddingValues ->
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
            val dispatch =
                viewModel.getDispatcher(feedUrls, startWith = ArticleIntent.Init(feedUrls))
            Content(
                uiState = uiState,
                onRefresh = { dispatch(ArticleIntent.Refresh(feedUrls)) },
                onFilterFavorite = { dispatch(ArticleIntent.FilterFavorite(it)) },
                onFilterRead = { dispatch(ArticleIntent.FilterRead(it)) },
                onFavorite = { articleWithFeed, favorite ->
                    dispatch(
                        ArticleIntent.Favorite(
                            articleId = articleWithFeed.articleWithEnclosure.article.articleId,
                            favorite = favorite,
                        )
                    )
                },
                onRead = { articleWithFeed, read ->
                    dispatch(
                        ArticleIntent.Read(
                            articleId = articleWithFeed.articleWithEnclosure.article.articleId,
                            read = read,
                        )
                    )
                },
                contentPadding = paddingValues,
            )
        }
    }

    WaitingDialog(visible = uiState.loadingDialog)

    when (val event = uiEvent) {
        is ArticleEvent.InitArticleListResultEvent.Failed ->
            snackbarHostState.showSnackbar(message = event.msg, scope = scope)

        is ArticleEvent.RefreshArticleListResultEvent.Failed ->
            snackbarHostState.showSnackbar(message = event.msg, scope = scope)

        is ArticleEvent.FavoriteArticleResultEvent.Failed ->
            snackbarHostState.showSnackbar(message = event.msg, scope = scope)

        is ArticleEvent.ReadArticleResultEvent.Failed ->
            snackbarHostState.showSnackbar(message = event.msg, scope = scope)

        null -> Unit
    }
}

@Composable
private fun Content(
    uiState: ArticleState,
    onRefresh: () -> Unit,
    onFilterFavorite: (Boolean) -> Unit,
    onFilterRead: (Boolean) -> Unit,
    onFavorite: (ArticleWithFeed, Boolean) -> Unit,
    onRead: (ArticleWithFeed, Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    val state = rememberPullRefreshState(
        refreshing = uiState.articleListState.loading,
        onRefresh = onRefresh,
    )
    Box(modifier = Modifier.pullRefresh(state)) {
        when (val articleListState = uiState.articleListState) {
            is ArticleListState.Failed -> {}
            is ArticleListState.Init -> {}
            is ArticleListState.Success -> {
                Column {
                    FilterRow(
                        modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
                        onFilterFavorite = onFilterFavorite,
                        onFilterRead = onFilterRead,
                    )
                    ArticleList(
                        articles = articleListState.articlePagingDataFlow.collectAsLazyPagingItems(),
                        onFavorite = onFavorite,
                        onRead = onRead,
                        contentPadding = PaddingValues(
                            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                            bottom = contentPadding.calculateBottomPadding(),
                        ),
                    )
                }
            }
        }
        PullRefreshIndicator(
            refreshing = uiState.articleListState.loading,
            state = state,
            modifier = Modifier
                .padding(contentPadding)
                .align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun ArticleList(
    modifier: Modifier = Modifier,
    articles: LazyPagingItems<ArticleWithFeed>,
    onFavorite: (ArticleWithFeed, Boolean) -> Unit,
    onRead: (ArticleWithFeed, Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    val adapter = remember {
        LazyGridAdapter(mutableListOf(Article1Proxy(onFavorite = onFavorite, onRead = onRead)))
    }
    AniVuLazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Adaptive(360.dp),
        dataList = articles,
        adapter = adapter,
        contentPadding = contentPadding,
        key = { _, item -> (item as ArticleWithFeed).articleWithEnclosure.article.articleId },
    )
}

@Composable
private fun FilterRow(
    modifier: Modifier,
    onFilterFavorite: (Boolean) -> Unit,
    onFilterRead: (Boolean) -> Unit,
) {
    var favoriteSelected by rememberSaveable { mutableStateOf(false) }
    var readSelected by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
//        FilterChip(
//            onClick = {
//                favoriteSelected = !favoriteSelected
//                onFilterFavorite(favoriteSelected)
//            },
//            label = { Text(stringResource(id = R.string.article_screen_favorite_filter)) },
//            selected = favoriteSelected,
//            leadingIcon = if (favoriteSelected) {
//                {
//                    Icon(
//                        imageVector = Icons.Filled.Done,
//                        contentDescription = stringResource(id = R.string.item_selected),
//                        modifier = Modifier.size(FilterChipDefaults.IconSize)
//                    )
//                }
//            } else {
//                null
//            },
//        )
//        FilterChip(
//            onClick = {
//                readSelected = !readSelected
//                onFilterRead(readSelected)
//            },
//            label = { Text(stringResource(id = R.string.article_screen_read_filter)) },
//            selected = readSelected,
//            leadingIcon = if (readSelected) {
//                {
//                    Icon(
//                        imageVector = Icons.Filled.Done,
//                        contentDescription = stringResource(id = R.string.item_selected),
//                        modifier = Modifier.size(FilterChipDefaults.IconSize)
//                    )
//                }
//            } else {
//                null
//            },
//        )
    }
}