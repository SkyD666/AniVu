package com.skyd.anivu.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.model.bean.FeedViewBean
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.BackIcon
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Article1Proxy
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Feed1Proxy
import com.skyd.anivu.ui.local.LocalSearchListTonalElevation
import com.skyd.anivu.ui.local.LocalSearchTopBarTonalElevation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.Serializable


@AndroidEntryPoint
class SearchFragment : BaseComposeFragment() {
    @kotlinx.serialization.Serializable
    sealed interface SearchDomain : Serializable {
        data object Feed : SearchDomain {
            private fun readResolve(): Any = Feed
        }

        data class Article(val feedUrls: List<String>) : SearchDomain
    }

    companion object {
        const val SEARCH_DOMAIN_KEY = "searchDomain"
    }

    private val viewModel by viewModels<SearchViewModel>()
    private val searchDomain by lazy {
        (arguments?.getSerializable(SEARCH_DOMAIN_KEY) as? SearchDomain) ?: SearchDomain.Feed
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { SearchScreen(searchDomain, viewModel) }
}

@Composable
fun SearchScreen(
    searchDomain: SearchFragment.SearchDomain,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    val keyboardController = LocalSoftwareKeyboardController.current

    val searchResultListState = rememberLazyGridState()
    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }

    var searchFieldValueState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
    }

    val dispatch = viewModel.getDispatcher(
        startWith = when (searchDomain) {
            SearchFragment.SearchDomain.Feed -> SearchIntent.ListenSearchFeed
            is SearchFragment.SearchDomain.Article ->
                SearchIntent.ListenSearchArticle(feedUrls = searchDomain.feedUrls)
        }
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = remember {
                    derivedStateOf { searchResultListState.firstVisibleItemIndex > 2 }
                }.value,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AniVuFloatingActionButton(
                    onClick = { scope.launch { searchResultListState.animateScrollToItem(0) } },
                    onSizeWithSinglePaddingChanged = { width, height ->
                        fabWidth = width
                        fabHeight = height
                    },
                    contentDescription = stringResource(R.string.to_top),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowUpward,
                        contentDescription = null,
                    )
                }
            }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            LocalSearchTopBarTonalElevation.current.dp
                        )
                    )
                    .windowInsetsPadding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    )
            ) {
                SearchBarInputField(
                    onQueryChange = {
                        searchFieldValueState = it
                        dispatch(SearchIntent.UpdateQuery(it.text))
                    },
                    query = searchFieldValueState,
                    onSearch = { keyboardController?.hide() },
                    placeholder = { Text(text = stringResource(R.string.search_screen_hint)) },
                    leadingIcon = { BackIcon() },
                    trailingIcon = {
                        TrailingIcon(showClearButton = searchFieldValueState.text.isNotEmpty()) {
                            searchFieldValueState = TextFieldValue(
                                text = "", selection = TextRange(0)
                            )
                        }
                    }
                )
                HorizontalDivider()
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
            LocalAbsoluteTonalElevation.current +
                    LocalSearchListTonalElevation.current.dp
        ),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { innerPaddings ->
        when (val searchResultState = uiState.searchResultState) {
            is SearchResultState.Failed -> Unit
            SearchResultState.Init -> CircularProgressIndicator()
            SearchResultState.Loading -> CircularProgressIndicator()
            is SearchResultState.Success -> SearchResultList(
                result = searchResultState.result.collectAsLazyPagingItems(),
                listState = searchResultListState,
                onFavorite = { articleWithFeed, favorite ->
                    dispatch(
                        SearchIntent.Favorite(
                            articleId = articleWithFeed.articleWithEnclosure.article.articleId,
                            favorite = favorite,
                        )
                    )
                },
                onRead = { articleWithFeed, read ->
                    dispatch(
                        SearchIntent.Read(
                            articleId = articleWithFeed.articleWithEnclosure.article.articleId,
                            read = read,
                        )
                    )
                },
                contentPadding = innerPaddings + PaddingValues(
                    top = 4.dp,
                    bottom = 4.dp + fabHeight,
                ),
            )
        }

        WaitingDialog(visible = uiState.loadingDialog)

        when (val event = uiEvent) {
            is SearchEvent.FavoriteArticleResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is SearchEvent.ReadArticleResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            null -> Unit
        }
    }
}

@Composable
private fun SearchResultList(
    modifier: Modifier = Modifier,
    result: LazyPagingItems<Any>,
    listState: LazyGridState,
    onFavorite: (ArticleWithFeed, Boolean) -> Unit,
    onRead: (ArticleWithFeed, Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    val adapter = remember {
        LazyGridAdapter(
            mutableListOf(
                Feed1Proxy(),
                Article1Proxy(onFavorite = onFavorite, onRead = onRead),
            )
        )
    }
    AniVuLazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(360.dp),
        dataList = result,
        listState = listState,
        adapter = adapter,
        contentPadding = contentPadding + PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        key = { _, item ->
            when (item) {
                is ArticleWithFeed -> item.articleWithEnclosure.article.articleId
                is FeedViewBean -> item.feed.url
                else -> item.hashCode()
            }
        },
    )
}

@Composable
fun TrailingIcon(
    showClearButton: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    if (showClearButton) {
        AniVuIconButton(
            imageVector = Icons.Outlined.Clear,
            contentDescription = stringResource(R.string.clear_input_text),
            onClick = { onClick?.invoke() }
        )
    }
}

@Composable
private fun SearchBarInputField(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusRequester = remember { FocusRequester() }
    TextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(72.dp),
        value = query,
        onValueChange = onQueryChange,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
        ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        interactionSource = interactionSource,
        singleLine = true,
        shape = RectangleShape,
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}