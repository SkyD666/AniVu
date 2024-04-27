package com.skyd.anivu.ui.fragment.media

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.config.Const
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.activity.PlayActivity.Companion.VIDEO_URI_KEY
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BackIcon
import com.skyd.anivu.ui.component.OnLifecycleEvent
import com.skyd.anivu.ui.component.dialog.AniVuDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFragment : BaseComposeFragment() {
    companion object {
        const val PATH_KEY = "path"
        const val HAS_PARENT_DIR_KEY = "hasParentDir"
    }

    private val viewModel by viewModels<MediaViewModel>()
    private val path by lazy { arguments?.getString(PATH_KEY) ?: Const.VIDEO_DIR.path }
    private val hasParentDir by lazy { arguments?.getBoolean(HAS_PARENT_DIR_KEY) ?: false }

    override val transitionProvider
        get() = if (hasParentDir) defaultTransitionProvider else nullTransitionProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { MediaScreen(path, hasParentDir, viewModel) }
}

const val MEDIA_SCREEN_ROUTE = "mediaScreen"

@Composable
fun MediaScreen(path: String, hasParentDir: Boolean, viewModel: MediaViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val windowSizeClass = LocalWindowSizeClass.current

    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                title = { Text(text = stringResource(R.string.media_screen_name)) },
                navigationIcon = if (hasParentDir) {
                    { BackIcon() }
                } else {
                    { }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets.safeDrawing.run {
                    val leftPadding = hasParentDir || windowSizeClass.isCompact
                    var sides = WindowInsetsSides.Top + WindowInsetsSides.Right
                    if (leftPadding) sides += WindowInsetsSides.Left
                    only(sides)
                }
            )
        },
        floatingActionButton = {
            AniVuFloatingActionButton(
                onClick = { navController.navigate(R.id.action_to_download_fragment) },
                onSizeWithSinglePaddingChanged = { width, height ->
                    fabWidth = width
                    fabHeight = height
                },
                contentDescription = stringResource(R.string.download_fragment_name),
            ) {
                Icon(imageVector = Icons.Outlined.Download, contentDescription = null)
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.run {
            val leftPadding = hasParentDir || windowSizeClass.isCompact
            var sides = WindowInsetsSides.Top + WindowInsetsSides.Right
            if (leftPadding) sides += WindowInsetsSides.Left
            if (hasParentDir) sides += WindowInsetsSides.Bottom
            only(sides)
        },
    ) { innerPadding ->
        if (path.isBlank()) {
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
            val dispatch = viewModel.getDispatcher(startWith = MediaIntent.Init(path))

            OnLifecycleEvent { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) dispatch(MediaIntent.Refresh(path))
            }

            val state = rememberPullRefreshState(
                refreshing = uiState.mediaListState.loading,
                onRefresh = { dispatch(MediaIntent.Refresh(path)) },
            )
            Box(modifier = Modifier.pullRefresh(state)) {
                when (val mediaListState = uiState.mediaListState) {
                    is MediaListState.Failed -> Unit
                    is MediaListState.Init -> Unit
                    is MediaListState.Success -> {
                        MediaList(
                            data = mediaListState.list,
                            contentPadding = innerPadding,
                            onPlay = {
                                (context.activity).startActivity(
                                    Intent(context, PlayActivity::class.java).apply {
                                        putExtra(VIDEO_URI_KEY, it.file.toUri(context))
                                    }
                                )
                            },
                            onOpenDir = {
                                navController.navigate(
                                    R.id.action_to_video_fragment,
                                    Bundle().apply {
                                        putString(MediaFragment.PATH_KEY, "${path}/${it.name}")
                                        putBoolean(MediaFragment.HAS_PARENT_DIR_KEY, true)
                                    }
                                )
                            },
                            onRemove = {
                                dispatch(MediaIntent.Delete(it.file))
                            }
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = uiState.mediaListState.loading,
                    state = state,
                    modifier = Modifier
                        .padding(innerPadding)
                        .align(Alignment.TopCenter),
                )
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)

        when (val event = uiEvent) {
            is MediaEvent.DeleteUriResultEvent.Failed -> snackbarHostState.showSnackbar(
                scope = scope, message = event.msg,
            )

            null -> Unit
        }
    }
}

@Composable
private fun MediaList(
    modifier: Modifier = Modifier,
    data: List<Any>,
    contentPadding: PaddingValues,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        columns = GridCells.Adaptive(300.dp),
    ) {
        items(data) { item ->
            if (item is VideoBean) {
                Media1Item(
                    data = item,
                    onPlay = onPlay,
                    onOpenDir = onOpenDir,
                    onRemove = onRemove,
                )
            }
        }
    }
}