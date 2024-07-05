package com.skyd.anivu.ui.fragment.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Workspaces
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastLastOrNull
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.popBackStackWithLifecycle
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.ext.snapshotStateMapSaver
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.model.preference.data.medialib.MediaLibLocationPreference
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BackIcon
import com.skyd.anivu.ui.component.dialog.AniVuDialog
import com.skyd.anivu.ui.component.dialog.TextFieldDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.fragment.media.item.Media1Item
import com.skyd.anivu.ui.fragment.media.item.MediaGroup1Item
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
    private val path by lazy {
        arguments?.getString(PATH_KEY) ?: requireContext().dataStore.getOrDefault(
            MediaLibLocationPreference
        )
    }
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
    val context = LocalContext.current
    val windowSizeClass = LocalWindowSizeClass.current

    var fabHeight by remember { mutableStateOf(0.dp) }
    var fabWidth by remember { mutableStateOf(0.dp) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
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
            val bottomPadding = hasParentDir || !windowSizeClass.isCompact
            var sides = WindowInsetsSides.Top + WindowInsetsSides.Right
            if (leftPadding) sides += WindowInsetsSides.Left
            if (bottomPadding) sides += WindowInsetsSides.Bottom
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
            val dispatch = viewModel.getDispatcher(
                startWith = MediaIntent.Init(path = path, isMediaLibRoot = !hasParentDir)
            )

            val state = rememberPullRefreshState(
                refreshing = uiState.mediaListState.loading,
                onRefresh = {
                    dispatch(MediaIntent.Refresh(path = path, isMediaLibRoot = !hasParentDir))
                },
            )
            var openCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
            var createGroupDialogGroup by rememberSaveable { mutableStateOf("") }

            var openEditMediaDialog by rememberSaveable {
                mutableStateOf<Pair<Int, VideoBean>?>(null, policy = referentialEqualityPolicy())
            }
            var openEditGroupDialog by rememberSaveable { mutableStateOf<MediaGroupBean?>(value = null) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(state),
            ) {
                when (val mediaListState = uiState.mediaListState) {
                    is MediaListState.Failed -> Unit
                    is MediaListState.Init -> Unit
                    is MediaListState.Success -> {
                        MediaListContent(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            hasParentDir = hasParentDir,
                            data = mediaListState.list,
                            contentPadding = innerPadding + PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            openEditMediaDialog = openEditMediaDialog,
                            openEditGroupDialog = openEditGroupDialog,
                            onOpenEditMediaDialogChanged = { openEditMediaDialog = it },
                            onOpenEditGroupDialog = { openEditGroupDialog = it },
                            onPlay = {
                                PlayActivity.play(context.activity, it.file.toUri(context))
                            },
                            onOpenDir = {
                                navController.navigate(
                                    R.id.action_to_video_fragment,
                                    Bundle().apply {
                                        putString(MediaFragment.PATH_KEY, it.file.path)
                                        putBoolean(MediaFragment.HAS_PARENT_DIR_KEY, true)
                                    }
                                )
                            },
                            onRemove = { dispatch(MediaIntent.Delete(it.file)) },
                            onGroupChange = { videoBean, group ->
                                dispatch(MediaIntent.ChangeMediaGroup(path, videoBean, group))
                            },
                            openCreateGroupDialog = {
                                openCreateGroupDialog = true
                                createGroupDialogGroup = ""
                            },
                            onDeleteGroup = { dispatch(MediaIntent.DeleteGroup(path, it)) },
                            onRenameGroup = { group, name ->
                                dispatch(MediaIntent.RenameGroup(path, group, name))
                            },
                            onMoveTo = { from, to ->
                                dispatch(MediaIntent.MoveFilesToGroup(path, from, to))
                            },
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

            CreateGroupDialog(
                visible = openCreateGroupDialog,
                value = createGroupDialogGroup,
                onValueChange = { text -> createGroupDialogGroup = text },
                onCreateGroup = {
                    dispatch(MediaIntent.CreateGroup(path, it))
                    openCreateGroupDialog = false
                },
                onDismissRequest = {
                    openCreateGroupDialog = false
                }
            )

            when (val event = uiEvent) {
                is MediaEvent.MediaListResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg, key1 = event
                    )

                is MediaEvent.DeleteFileResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg, key1 = event
                    )

                is MediaEvent.ChangeFileGroupResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg, key1 = event
                    )

                is MediaEvent.CreateGroupResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg, key1 = event
                    )

                is MediaEvent.DeleteGroupResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg,
                        key1 = event
                    )

                is MediaEvent.EditGroupResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg, key1 = event
                    )

                is MediaEvent.MoveFilesToGroupResultEvent.Failed ->
                    snackbarHostState.showSnackbarWithLaunchedEffect(
                        message = event.msg, key1 = event
                    )

                is MediaEvent.EditGroupResultEvent.Success -> LaunchedEffect(event) {
                    dispatch(MediaIntent.Refresh(path = path, isMediaLibRoot = !hasParentDir))
                    if (openEditGroupDialog != null) openEditGroupDialog = event.group
                }

                is MediaEvent.ChangeFileGroupResultEvent.Success -> LaunchedEffect(event) {
                    dispatch(MediaIntent.Refresh(path = path, isMediaLibRoot = !hasParentDir))
                    if (openEditMediaDialog != null) {
                        openEditMediaDialog = openEditMediaDialog!!.copy(
                            second = openEditMediaDialog!!.second
                        )
                    }
                }

                is MediaEvent.CreateGroupResultEvent.Success,
                is MediaEvent.DeleteGroupResultEvent.Success,
                is MediaEvent.MoveFilesToGroupResultEvent.Success -> LaunchedEffect(event) {
                    dispatch(MediaIntent.Refresh(path = path, isMediaLibRoot = !hasParentDir))
                }

                null -> Unit
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)
    }
}

@Composable
private fun MediaListContent(
    modifier: Modifier = Modifier,
    hasParentDir: Boolean,
    data: List<Any>,
    contentPadding: PaddingValues,
    openEditMediaDialog: Pair<Int, VideoBean>?,
    openEditGroupDialog: MediaGroupBean?,
    onOpenEditMediaDialogChanged: (Pair<Int, VideoBean>?) -> Unit,
    onOpenEditGroupDialog: (MediaGroupBean?) -> Unit,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
    onGroupChange: (VideoBean, MediaGroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
    onDeleteGroup: (MediaGroupBean) -> Unit,
    onRenameGroup: (MediaGroupBean, String) -> Unit,
    onMoveTo: (MediaGroupBean, MediaGroupBean) -> Unit,
) {
    if (hasParentDir) {
        MediaList(
            modifier = modifier.fillMaxSize(),
            data = data,
            contentPadding = contentPadding,
            onPlay = onPlay,
            onOpenDir = onOpenDir,
            onRemove = onRemove,
        )
    } else {
        MediaListWithGroup(
            modifier = modifier.fillMaxSize(),
            data = data,
            contentPadding = contentPadding,
            openEditMediaDialog = openEditMediaDialog,
            openEditGroupDialog = openEditGroupDialog,
            onOpenEditMediaDialogChanged = onOpenEditMediaDialogChanged,
            onOpenEditGroupDialog = onOpenEditGroupDialog,
            onPlay = onPlay,
            onOpenDir = onOpenDir,
            onRemove = onRemove,
            onGroupChange = onGroupChange,
            openCreateGroupDialog = openCreateGroupDialog,
            onDeleteGroup = onDeleteGroup,
            onRenameGroup = onRenameGroup,
            onMoveTo = onMoveTo,
        )
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
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(data) { index, item ->
            if (item is VideoBean) {
                Media1Item(
                    index = index,
                    data = item,
                    onPlay = onPlay,
                    onOpenDir = onOpenDir,
                    onRemove = onRemove,
                )
            }
        }
    }
}

@Composable
private fun MediaListWithGroup(
    modifier: Modifier = Modifier,
    data: List<Any>,
    contentPadding: PaddingValues,
    openEditMediaDialog: Pair<Int, VideoBean>?,
    openEditGroupDialog: MediaGroupBean?,
    onOpenEditMediaDialogChanged: (Pair<Int, VideoBean>?) -> Unit,
    onOpenEditGroupDialog: (MediaGroupBean?) -> Unit,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
    onGroupChange: (VideoBean, MediaGroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
    onDeleteGroup: (MediaGroupBean) -> Unit,
    onRenameGroup: (MediaGroupBean, String) -> Unit,
    onMoveTo: (MediaGroupBean, MediaGroupBean) -> Unit,
) {
    val groups = remember(data) { data.filterIsInstance<MediaGroupBean>() }
    val groupsIndex = remember(data) {
        listOf(
            *(data.mapIndexedNotNull { index, data ->
                if (data is MediaGroupBean) index to data else null
            }.toTypedArray())
        )
    }
    val mediaVisible = rememberSaveable(saver = snapshotStateMapSaver()) {
        mutableStateMapOf(
            *(groups.map { it to true }.toTypedArray())
        )
    }
    // Update mediaVisible when groups change
    LaunchedEffect(groups) {
        mediaVisible.forEach { (t, u) ->
            if (groups.find { it == t } == null) {
                mediaVisible.remove(t)
            } else {
                mediaVisible[t] = u
            }
        }
        groups.forEach {
            mediaVisible.putIfAbsent(it, true)
        }
    }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        columns = GridCells.Fixed(1),
    ) {
        itemsIndexed(data) { index, item ->
            if (item is VideoBean) {
                Media1Item(
                    index = index,
                    data = item,
                    mediaGroup = { groupsIndex.fastLastOrNull { pair -> pair.first < index }!!.second },
                    visible = { mediaVisible[it] == true },
                    isEnded = { it == data.lastIndex || data[it + 1] is MediaGroupBean },
                    onPlay = onPlay,
                    onOpenDir = onOpenDir,
                    onRemove = onRemove,
                    onLongClick = { onOpenEditMediaDialogChanged(index to it) },
                )
            } else if (item is MediaGroupBean) {
                MediaGroup1Item(
                    index = index,
                    data = item,
                    initExpand = { mediaVisible[item] == true },
                    onExpandChange = { data, expand -> mediaVisible[data] = expand },
                    isEmpty = { it == data.lastIndex || data[it + 1] is MediaGroupBean },
                    onEdit = { onOpenEditGroupDialog(it) },
                )
            }
        }
    }

    if (openEditMediaDialog != null) {
        val (index, videoBean) = openEditMediaDialog
        EditMediaSheet(
            onDismissRequest = { onOpenEditMediaDialogChanged(null) },
            file = videoBean.file,
            currentGroup = groupsIndex.fastLastOrNull { pair -> pair.first < index }!!.second,
            groups = groups,
            onDelete = {
                onRemove(videoBean)
                onOpenEditMediaDialogChanged(null)
            },
            onGroupChange = { onGroupChange(videoBean, it) },
            openCreateGroupDialog = openCreateGroupDialog,
        )
    }

    if (openEditGroupDialog != null) {
        EditMediaGroupSheet(
            onDismissRequest = { onOpenEditGroupDialog(null) },
            group = openEditGroupDialog,
            groups = groups,
            onDelete = {
                onDeleteGroup(it)
                onOpenEditGroupDialog(null)
            },
            onNameChange = { onRenameGroup(openEditGroupDialog, it) },
            onMoveTo = { onMoveTo(openEditGroupDialog, it) },
            openCreateGroupDialog = openCreateGroupDialog,
        )
    }
}

@Composable
private fun CreateGroupDialog(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onCreateGroup: (MediaGroupBean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    TextFieldDialog(
        visible = visible,
        icon = { Icon(imageVector = Icons.Outlined.Workspaces, contentDescription = null) },
        titleText = stringResource(id = R.string.media_screen_add_group),
        placeholder = stringResource(id = R.string.media_group),
        value = value,
        onValueChange = onValueChange,
        onConfirm = { text -> onCreateGroup(MediaGroupBean(name = text)) },
        onDismissRequest = onDismissRequest,
    )
}