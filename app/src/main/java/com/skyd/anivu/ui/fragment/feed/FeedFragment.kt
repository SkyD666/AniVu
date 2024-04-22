package com.skyd.anivu.ui.fragment.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.dialog.TextFieldDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Feed1Proxy
import com.skyd.anivu.ui.fragment.search.SearchFragment
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : BaseComposeFragment() {
    override val transitionProvider = nullTransitionProvider

    private val feedViewModel by viewModels<FeedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { FeedScreen(viewModel = feedViewModel) }
}

const val FEED_SCREEN_ROUTE = "feedScreen"

@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val windowSizeClass = LocalWindowSizeClass.current
    val scope = rememberCoroutineScope()
    var openAddDialog by rememberSaveable { mutableStateOf(false) }
    var addDialogText by rememberSaveable { mutableStateOf("") }
    var openEditDialog by rememberSaveable { mutableStateOf<String?>(null) }

    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    val dispatch = viewModel.getDispatcher(startWith = FeedIntent.Init)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                title = { Text(text = stringResource(id = R.string.feed_screen_name)) },
                actions = {
                    AniVuIconButton(
                        onClick = {
                            navController.navigate(
                                resId = R.id.action_to_search_fragment,
                                args = Bundle().apply {
                                    putSerializable(
                                        SearchFragment.SEARCH_DOMAIN_KEY,
                                        SearchFragment.SearchDomain.Feed,
                                    )
                                }
                            )
                        },
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.feed_screen_search_feed),
                    )
                },
                navigationIcon = {},
                windowInsets = WindowInsets.safeDrawing.only(
                    (WindowInsetsSides.Top + WindowInsetsSides.Right).run {
                        if (windowSizeClass.isCompact) plus(WindowInsetsSides.Left) else this
                    }
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            AniVuFloatingActionButton(
                onClick = { openAddDialog = true },
                onSizeWithSinglePaddingChanged = { width, height ->
                    fabWidth = width
                    fabHeight = height
                },
                contentDescription = stringResource(R.string.add),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            (WindowInsetsSides.Top + WindowInsetsSides.Right).run {
                if (windowSizeClass.isCompact) plus(WindowInsetsSides.Left) else this
            }
        )
    ) { innerPadding ->
        when (val feedListState = uiState.feedListState) {
            is FeedListState.Failed, FeedListState.Init, FeedListState.Loading -> {}
            is FeedListState.Success -> {
                FeedList(
                    result = feedListState.feedPagingData.collectAsLazyPagingItems(),
                    contentPadding = innerPadding + PaddingValues(bottom = fabHeight),
                    onRemove = { feed -> FeedIntent.RemoveFeed(feed.url) },
                    onEdit = { feed -> openEditDialog = feed.url },
                )
            }
        }

        TextFieldDialog(
            visible = openAddDialog,
            icon = { Icon(imageVector = Icons.Default.RssFeed, contentDescription = null) },
            title = stringResource(id = R.string.add),
            placeholder = stringResource(id = R.string.feed_screen_add_rss_hint),
            value = addDialogText,
            onValueChange = { text -> addDialogText = text },
            onConfirm = { text ->
                if (text.isNotBlank()) {
                    dispatch(FeedIntent.AddFeed(text))
                }
                addDialogText = ""
                openAddDialog = false
            },
            onDismissRequest = {
                addDialogText = ""
                openAddDialog = false
            }
        )

        TextFieldDialog(
            visible = openEditDialog != null,
            icon = { Icon(imageVector = Icons.Default.RssFeed, contentDescription = null) },
            title = stringResource(id = R.string.edit),
            placeholder = stringResource(id = R.string.feed_screen_add_rss_hint),
            value = openEditDialog.orEmpty(),
            onValueChange = { openEditDialog = it },
            onConfirm = { text ->
                if (text.isNotBlank()) {
                    dispatch(FeedIntent.EditFeed(oldUrl = openEditDialog!!, newUrl = text))
                }
                openEditDialog = null
            },
            onDismissRequest = { openEditDialog = null }
        )

        WaitingDialog(visible = uiState.loadingDialog)

        when (val event = uiEvent) {
            is FeedEvent.AddFeedResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.EditFeedResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.InitFeetListResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.RemoveFeedResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            FeedEvent.AddFeedResultEvent.Success,
            FeedEvent.EditFeedResultEvent.Success,
            FeedEvent.RemoveFeedResultEvent.Success,
            null -> Unit
        }
    }
}

@Composable
private fun FeedList(
    result: LazyPagingItems<FeedBean>,
    contentPadding: PaddingValues = PaddingValues(),
    onRemove: (FeedBean) -> Unit,
    onEdit: (FeedBean) -> Unit,
) {
    val adapter = remember {
        LazyGridAdapter(mutableListOf(Feed1Proxy(onRemove = onRemove, onEdit = onEdit)))
    }
    AniVuLazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(250.dp),
        dataList = result,
        adapter = adapter,
        contentPadding = contentPadding,
        key = { _, item -> (item as FeedBean).url },
    )
}