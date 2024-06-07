package com.skyd.anivu.ui.fragment.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.model.repository.ArticleSort
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.BackIcon
import com.skyd.anivu.ui.component.dialog.AniVuDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Article1Proxy
import com.skyd.anivu.ui.fragment.search.SearchFragment
import com.skyd.anivu.ui.local.LocalArticleListTonalElevation
import com.skyd.anivu.ui.local.LocalArticleTopBarTonalElevation
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalShowArticlePullRefresh
import com.skyd.anivu.ui.local.LocalShowArticleTopBarRefresh
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleFragment : BaseComposeFragment() {
    companion object {
        const val FEED_URLS_KEY = "feedUrls"
    }

    private val feedUrls by lazy { arguments?.getStringArrayList(FEED_URLS_KEY).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { ArticleScreen(feedUrls = feedUrls) }
}

private val DefaultBackClick = { }

@Composable
fun ArticleScreen(
    feedUrls: List<String>,
    onBackClick: () -> Unit = DefaultBackClick,
) {
    val navController = LocalNavController.current
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
        ArticleContentScreen(
            feedUrls = feedUrls,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun ArticleContentScreen(
    feedUrls: List<String>,
    onBackClick: () -> Unit = DefaultBackClick,
    viewModel: ArticleViewModel = hiltViewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    val listState: LazyGridState = rememberLazyGridState()
    var fabHeight by remember { mutableStateOf(0.dp) }
    var showFilterBar by rememberSaveable { mutableStateOf(false) }

    val dispatch = viewModel.getDispatcher(feedUrls, startWith = ArticleIntent.Init(feedUrls))
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                title = { Text(text = stringResource(R.string.article_screen_name)) },
                navigationIcon = {
                    if (onBackClick == DefaultBackClick) BackIcon()
                    else BackIcon(onClick = onBackClick)
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        LocalArticleTopBarTonalElevation.current.dp
                    ),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        LocalArticleTopBarTonalElevation.current.dp + 4.dp
                    ),
                ),
                actions = {
                    if (LocalShowArticleTopBarRefresh.current) {
                        val angle = if (uiState.articleListState.loading) {
                            val infiniteTransition =
                                rememberInfiniteTransition(label = "topBarRefreshTransition")
                            infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing)
                                ),
                                label = "topBarRefreshAnimate",
                            ).value
                        } else 0f
                        AniVuIconButton(
                            onClick = { dispatch(ArticleIntent.Refresh(feedUrls)) },
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(id = R.string.refresh),
                            rotate = angle,
                            enabled = !uiState.articleListState.loading,
                        )
                    }
                    FilterIcon(
                        filterCount = uiState.articleFilterState.filterCount,
                        showFilterBar = showFilterBar,
                        onFilterBarVisibilityChanged = { showFilterBar = it },
                        onFilterFavorite = { dispatch(ArticleIntent.FilterFavorite(it)) },
                        onFilterRead = { dispatch(ArticleIntent.FilterRead(it)) },
                        onSort = { dispatch(ArticleIntent.UpdateSort(it)) },
                    )
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
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }.value,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AniVuFloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                    contentDescription = stringResource(R.string.to_top),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowUpward,
                        contentDescription = null,
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
            LocalAbsoluteTonalElevation.current +
                    LocalArticleListTonalElevation.current.dp
        ),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { paddingValues ->
        Content(
            uiState = uiState,
            listState = listState,
            nestedScrollConnection = scrollBehavior.nestedScrollConnection,
            showFilterBar = showFilterBar,
            onRefresh = { dispatch(ArticleIntent.Refresh(feedUrls)) },
            onFilterFavorite = { dispatch(ArticleIntent.FilterFavorite(it)) },
            onFilterRead = { dispatch(ArticleIntent.FilterRead(it)) },
            onSort = { dispatch(ArticleIntent.UpdateSort(it)) },
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
            contentPadding = paddingValues + PaddingValues(bottom = fabHeight),
        )

        WaitingDialog(visible = uiState.loadingDialog)

        when (val event = uiEvent) {
            is ArticleEvent.InitArticleListResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is ArticleEvent.RefreshArticleListResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is ArticleEvent.FavoriteArticleResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is ArticleEvent.ReadArticleResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            null -> Unit
        }
    }
}

@Composable
private fun Content(
    uiState: ArticleState,
    listState: LazyGridState,
    nestedScrollConnection: NestedScrollConnection,
    showFilterBar: Boolean,
    onRefresh: () -> Unit,
    onFilterFavorite: (Boolean?) -> Unit,
    onFilterRead: (Boolean?) -> Unit,
    onSort: (ArticleSort) -> Unit,
    onFavorite: (ArticleWithFeed, Boolean) -> Unit,
    onRead: (ArticleWithFeed, Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    val state = rememberPullRefreshState(
        refreshing = uiState.articleListState.loading,
        onRefresh = onRefresh,
    )
    Box(
        modifier = Modifier
            .pullRefresh(state = state, enabled = LocalShowArticlePullRefresh.current)
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        Column {
            AnimatedVisibility(visible = showFilterBar) {
                Column(
                    modifier = Modifier.padding(
                        start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                    ),
                ) {
                    FilterRow(
                        articleFilterState = uiState.articleFilterState,
                        onFilterFavorite = onFilterFavorite,
                        onFilterRead = onFilterRead,
                        onSort = onSort,
                    )
                    HorizontalDivider()
                }
            }
            val articleListState = uiState.articleListState
            ArticleList(
                modifier = Modifier.nestedScroll(nestedScrollConnection),
                articles = ((articleListState as? ArticleListState.Success)
                    ?.articlePagingDataFlow
                    ?: flowOf(PagingData.empty())).collectAsLazyPagingItems(),
                listState = listState,
                onFavorite = onFavorite,
                onRead = onRead,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = contentPadding.calculateBottomPadding(),
                ) + PaddingValues(vertical = 4.dp),
            )
        }

        if (LocalShowArticlePullRefresh.current) {
            PullRefreshIndicator(
                refreshing = uiState.articleListState.loading,
                state = state,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun ArticleList(
    modifier: Modifier = Modifier,
    articles: LazyPagingItems<ArticleWithFeed>,
    listState: LazyGridState,
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
        listState = listState,
        adapter = adapter,
        contentPadding = contentPadding + PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        key = { _, item -> (item as ArticleWithFeed).articleWithEnclosure.article.articleId },
    )
}