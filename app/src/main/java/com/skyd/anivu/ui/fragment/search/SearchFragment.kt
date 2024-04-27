package com.skyd.anivu.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.BackIcon
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Article1Proxy
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Feed1Proxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.Serializable


@AndroidEntryPoint
class SearchFragment : BaseComposeFragment() {
    @kotlinx.serialization.Serializable
    sealed interface SearchDomain : Serializable {
        data object All : SearchDomain {
            private fun readResolve(): Any = All
        }

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
        (arguments?.getSerializable(SEARCH_DOMAIN_KEY) as? SearchDomain) ?: SearchDomain.All
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
    val keyboardController = LocalSoftwareKeyboardController.current

    val searchResultListState = rememberLazyStaggeredGridState()
    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }

    var searchFieldValueState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
    }

    val dispatch = viewModel.getDispatcher(startWith = SearchIntent.Init)

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(visible = searchResultListState.firstVisibleItemIndex > 2) {
                AniVuFloatingActionButton(
                    onClick = { scope.launch { searchResultListState.animateScrollToItem(0) } },
                    onSizeWithSinglePaddingChanged = { width, height ->
                        fabWidth = width
                        fabHeight = height
                    },
                    contentDescription = stringResource(R.string.search_screen_list_to_top),
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
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    )
            ) {
                SearchBarInputField(
                    onQueryChange = { searchFieldValueState = it },
                    query = searchFieldValueState,
                    onSearch = { state ->
                        keyboardController?.hide()
                        searchFieldValueState = state
                        doSearch(
                            searchDomain = searchDomain,
                            query = state.text,
                            onSearch = { dispatch(it) },
                        )
                    },
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
        }
    ) { innerPaddings ->
        when (val searchResultState = uiState.searchResultState) {
            is SearchResultState.Failed -> Unit
            SearchResultState.Init -> CircularProgressIndicator()
            SearchResultState.Loading -> CircularProgressIndicator()
            is SearchResultState.Success -> SearchResultList(
                result = searchResultState.result.collectAsLazyPagingItems(),
                contentPadding = innerPaddings + PaddingValues(bottom = fabHeight),
            )
        }
    }
}

@Composable
private fun SearchResultList(
    modifier: Modifier = Modifier,
    result: LazyPagingItems<Any>,
    contentPadding: PaddingValues,
) {
    val adapter = remember {
        LazyGridAdapter(mutableListOf(Feed1Proxy(), Article1Proxy()))
    }
    AniVuLazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(250.dp),
        dataList = result,
        adapter = adapter,
        contentPadding = contentPadding,
    )
}

private fun doSearch(
    searchDomain: SearchFragment.SearchDomain,
    query: String,
    onSearch: (SearchIntent) -> Unit,
) {
    when (searchDomain) {
        SearchFragment.SearchDomain.All -> onSearch(SearchIntent.SearchAll(query = query))
        SearchFragment.SearchDomain.Feed -> onSearch(SearchIntent.SearchFeed(query = query))
        is SearchFragment.SearchDomain.Article -> {
            onSearch(SearchIntent.SearchArticle(feedUrls = searchDomain.feedUrls, query = query))
        }
    }
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
    onSearch: (TextFieldValue) -> Unit,
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
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        interactionSource = interactionSource,
        singleLine = true,
        shape = RectangleShape,
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}