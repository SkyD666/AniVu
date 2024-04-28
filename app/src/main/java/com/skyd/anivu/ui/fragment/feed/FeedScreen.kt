package com.skyd.anivu.ui.fragment.feed

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoveUp
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.isCompact
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbar
import com.skyd.anivu.ext.snapshotStateMapSaver
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedBean.Companion.isDefaultGroup
import com.skyd.anivu.model.bean.FeedViewBean
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.bean.GroupBean.Companion.isDefaultGroup
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTextField
import com.skyd.anivu.ui.component.AniVuTextFieldStyle
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.ClipboardTextField
import com.skyd.anivu.ui.component.dialog.AniVuDialog
import com.skyd.anivu.ui.component.dialog.TextFieldDialog
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.component.lazyverticalgrid.AniVuLazyVerticalGrid
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.LazyGridAdapter
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.DefaultGroup1Proxy
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Feed1Proxy
import com.skyd.anivu.ui.component.lazyverticalgrid.adapter.proxy.Group1Proxy
import com.skyd.anivu.ui.fragment.article.ArticleFragment
import com.skyd.anivu.ui.fragment.search.SearchFragment
import com.skyd.anivu.ui.local.LocalFeedGroupExpand
import com.skyd.anivu.ui.local.LocalHideEmptyDefault
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.local.LocalTextFieldStyle
import com.skyd.anivu.ui.local.LocalWindowSizeClass
import kotlinx.coroutines.android.awaitFrame
import java.util.UUID

const val FEED_SCREEN_ROUTE = "feedScreen"

@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val windowSizeClass = LocalWindowSizeClass.current
    val scope = rememberCoroutineScope()
    var openAddDialog by rememberSaveable { mutableStateOf(false) }
    var addDialogUrl by rememberSaveable { mutableStateOf("") }
    var addDialogNickname by rememberSaveable { mutableStateOf("") }
    var addDialogGroup by rememberSaveable { mutableStateOf<GroupBean>(GroupBean.DefaultGroup) }
    var openEditDialog by rememberSaveable { mutableStateOf<FeedBean?>(null) }
    var editDialogUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var editDialogNickname by rememberSaveable { mutableStateOf("") }
    var editDialogGroupId by rememberSaveable { mutableStateOf<String?>(null) }

    var openCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
    var createGroupDialogGroup by rememberSaveable { mutableStateOf("") }

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
                            navController.navigate(R.id.action_to_article_fragment, Bundle().apply {
                                putStringArrayList(
                                    ArticleFragment.FEED_URLS_KEY,
                                    ArrayList(
                                        (uiState.groupListState as? GroupListState.Success)
                                            ?.dataList
                                            ?.filterIsInstance(FeedViewBean::class.java)
                                            ?.map { it.feed.url }
                                            .orEmpty()
                                    )
                                )
                            })
                        },
                        imageVector = Icons.AutoMirrored.Outlined.Article,
                        contentDescription = stringResource(id = R.string.feed_screen_all_articles),
                    )
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
                        imageVector = Icons.Outlined.Search,
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
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            (WindowInsetsSides.Top + WindowInsetsSides.Right).run {
                if (windowSizeClass.isCompact) plus(WindowInsetsSides.Left) else this
            }
        )
    ) { innerPadding ->
        when (val groupListState = uiState.groupListState) {
            is GroupListState.Failed, GroupListState.Init, GroupListState.Loading -> {}
            is GroupListState.Success -> {
                FeedList(
                    result = groupListState.dataList,
                    contentPadding = innerPadding + PaddingValues(bottom = fabHeight + 16.dp),
                    onRemoveFeed = { feed -> dispatch(FeedIntent.RemoveFeed(feed.url)) },
                    onShowAllArticles = { group ->
                        val feedUrls = (uiState.groupListState as? GroupListState.Success)
                            ?.dataList
                            ?.filterIsInstance(FeedViewBean::class.java)
                            ?.filter { it.feed.groupId == group.groupId || group.isDefaultGroup() && it.feed.isDefaultGroup() }
                            ?.map { it.feed.url }
                            .orEmpty()
                        if (feedUrls.isNotEmpty()) {
                            navController.navigate(R.id.action_to_article_fragment, Bundle().apply {
                                putStringArrayList(
                                    ArticleFragment.FEED_URLS_KEY, ArrayList(feedUrls)
                                )
                            })
                        }
                    },
                    onEditFeed = { feed ->
                        openEditDialog = feed
                        editDialogUrl = feed.url
                        editDialogNickname = feed.nickname.orEmpty()
                        editDialogGroupId = feed.groupId
                    },
                    onDeleteGroup = { dispatch(FeedIntent.DeleteGroup(it.groupId)) },
                    onMoveToGroup = { from, to ->
                        if (from.groupId != to.groupId) {
                            dispatch(FeedIntent.MoveFeedsToGroup(from.groupId, to.groupId))
                        }
                    },
                    openCreateGroupDialog = {
                        openCreateGroupDialog = true
                        createGroupDialogGroup = ""
                    }
                )
            }
        }

        if (openAddDialog) {
            val groups = (uiState.groupListState as? GroupListState.Success)
                ?.dataList?.filterIsInstance<GroupBean>().orEmpty()
            EditFeedDialog(
                title = stringResource(id = R.string.add),
                url = addDialogUrl,
                onUrlChange = { text -> addDialogUrl = text },
                nickname = addDialogNickname,
                onNicknameChange = { addDialogNickname = it },
                group = addDialogGroup,
                groups = groups,
                onGroupChange = { addDialogGroup = it },
                openCreateGroupDialog = {
                    openCreateGroupDialog = true
                    createGroupDialogGroup = ""
                },
                onConfirm = { newUrl, nickname, group ->
                    if (newUrl.isNotBlank()) {
                        dispatch(
                            FeedIntent.AddFeed(url = newUrl, nickname = nickname, group = group)
                        )
                    }
                    addDialogUrl = ""
                    addDialogNickname = ""
                    addDialogGroup = GroupBean.DefaultGroup
                    openAddDialog = false
                },
                onDismissRequest = {
                    addDialogUrl = ""
                    addDialogNickname = ""
                    addDialogGroup = GroupBean.DefaultGroup
                    openAddDialog = false
                }
            )
        }

        if (openEditDialog != null) {
            val groups = (uiState.groupListState as? GroupListState.Success)
                ?.dataList?.filterIsInstance<GroupBean>().orEmpty()
            EditFeedDialog(
                url = editDialogUrl!!,
                onUrlChange = { editDialogUrl = it },
                nickname = editDialogNickname,
                onNicknameChange = { editDialogNickname = it },
                group = groups.find { it.groupId == editDialogGroupId } ?: GroupBean.DefaultGroup,
                groups = groups,
                onGroupChange = { editDialogGroupId = it.groupId },
                openCreateGroupDialog = {
                    openCreateGroupDialog = true
                    createGroupDialogGroup = ""
                },
                onConfirm = { newUrl, nickname, group ->
                    dispatch(
                        FeedIntent.EditFeed(
                            oldUrl = openEditDialog!!.url,
                            newUrl = newUrl,
                            nickname = nickname,
                            groupId = group.groupId,
                        )
                    )
                    openEditDialog = null
                    editDialogNickname = ""
                    editDialogUrl = null
                },
                onDismissRequest = {
                    openEditDialog = null
                    editDialogNickname = ""
                    editDialogUrl = null
                },
            )
        }

        WaitingDialog(visible = uiState.loadingDialog)

        CreateGroupDialog(
            visible = openCreateGroupDialog,
            value = createGroupDialogGroup,
            onValueChange = { text -> createGroupDialogGroup = text },
            onCreateGroup = {
                dispatch(FeedIntent.CreateGroup(it))
                openCreateGroupDialog = false
            },
            onDismissRequest = {
                openCreateGroupDialog = false
            }
        )

        when (val event = uiEvent) {
            is FeedEvent.AddFeedResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.EditFeedResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.InitFeetListResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.RemoveFeedResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.CreateGroupResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.MoveFeedsToGroupResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            is FeedEvent.DeleteGroupResultEvent.Failed ->
                snackbarHostState.showSnackbar(message = event.msg, scope = scope)

            FeedEvent.AddFeedResultEvent.Success,
            FeedEvent.EditFeedResultEvent.Success,
            FeedEvent.RemoveFeedResultEvent.Success,
            FeedEvent.CreateGroupResultEvent.Success,
            FeedEvent.MoveFeedsToGroupResultEvent.Success,
            FeedEvent.DeleteGroupResultEvent.Success,
            null -> Unit
        }
    }
}

@Composable
private fun EditFeedDialog(
    title: String = stringResource(id = R.string.edit),
    url: String,
    onUrlChange: (String) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    group: GroupBean,
    groups: List<GroupBean>,
    onGroupChange: (GroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
    onConfirm: (url: String, nickname: String, group: GroupBean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var expandMenu by rememberSaveable { mutableStateOf(false) }

    AniVuDialog(
        visible = true,
        icon = { Icon(imageVector = Icons.Outlined.RssFeed, contentDescription = null) },
        title = { Text(text = title) },
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                val focusManager = LocalFocusManager.current
                val focusRequester = remember { FocusRequester() }
                val keyboard = LocalSoftwareKeyboardController.current
                ClipboardTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = url,
                    onValueChange = onUrlChange,
                    autoRequestFocus = false,
                    label = stringResource(id = R.string.feed_screen_add_rss_hint),
                    focusManager = focusManager,
                    imeAction = ImeAction.Next,
                    keyboardAction = { _, _ ->
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                ClipboardTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    autoRequestFocus = false,
                    label = stringResource(id = R.string.feed_screen_rss_nickname),
                    focusManager = focusManager,
                    keyboardAction = { _, _ ->
                        focusManager.clearFocus()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExposedDropdownMenuBox(
                        modifier = Modifier.weight(1f),
                        expanded = expandMenu,
                        onExpandedChange = { expandMenu = it },
                    ) {
                        AniVuTextField(
                            // The `menuAnchor` modifier must be passed to the text field to handle
                            // expanding/collapsing the menu on click. A read-only text field has
                            // the anchor type `PrimaryNotEditable`.
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            value = group.name,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 1,
                            label = stringResource(R.string.feed_group),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandMenu) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = expandMenu,
                            onDismissRequest = { expandMenu = false },
                        ) {
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = group.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    },
                                    onClick = {
                                        onGroupChange(group)
                                        expandMenu = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                    AniVuIconButton(
                        // https://m3.material.io/components/text-fields/specs#605e24f1-1c1f-4c00-b385-4bf50733a5ef
                        modifier = Modifier.run {
                            if (LocalTextFieldStyle.current == AniVuTextFieldStyle.Outlined.value)
                                padding(top = 8.dp)
                            else this
                        },
                        onClick = openCreateGroupDialog,
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = R.string.feed_screen_add_group),
                    )
                }

                LaunchedEffect(focusRequester) {
                    focusRequester.requestFocus()
                    awaitFrame()
                    keyboard?.show()
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = url.isNotBlank(),
                onClick = {
                    onConfirm(url, nickname, group)
                }
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    color = if (url.isNotBlank()) Color.Unspecified
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun CreateGroupDialog(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onCreateGroup: (GroupBean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    TextFieldDialog(
        visible = visible,
        icon = { Icon(imageVector = Icons.Outlined.Workspaces, contentDescription = null) },
        title = stringResource(id = R.string.feed_screen_add_group),
        placeholder = stringResource(id = R.string.feed_group),
        value = value,
        onValueChange = onValueChange,
        onConfirm = { text ->
            onCreateGroup(
                GroupBean(
                    groupId = UUID.randomUUID().toString(),
                    name = text,
                )
            )
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun FeedList(
    result: List<Any>,
    contentPadding: PaddingValues = PaddingValues(),
    onRemoveFeed: (FeedBean) -> Unit,
    onEditFeed: (FeedBean) -> Unit,
    onShowAllArticles: (GroupBean) -> Unit,
    onDeleteGroup: (GroupBean) -> Unit,
    onMoveToGroup: (from: GroupBean, to: GroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
) {
    val hideEmptyDefault = LocalHideEmptyDefault.current
    val feedGroupExpand = LocalFeedGroupExpand.current
    val groups = rememberSaveable(result) { result.filterIsInstance<GroupBean>() }
    val feedVisible = rememberSaveable(saver = snapshotStateMapSaver()) {
        mutableStateMapOf(
            GroupBean.DEFAULT_GROUP_ID to feedGroupExpand,
            *(groups
                .map { it.groupId to feedGroupExpand }
                .toTypedArray())
        )
    }
    // Update feedVisible when groups change
    LaunchedEffect(groups) {
        feedVisible.forEach { (t, u) ->
            if (groups.find { it.groupId == t } == null) {
                feedVisible.remove(t)
            } else {
                feedVisible[t] = u
            }
        }
        groups.forEach {
            feedVisible[it.groupId] = feedVisible[it.groupId] ?: false
        }
    }
    var openSelectGroupDialog by rememberSaveable { mutableStateOf<GroupBean?>(null) }
    var selectGroupDialogCurrentGroup by rememberSaveable { mutableStateOf<GroupBean?>(null) }

    val shouldHideEmptyDefault: (index: Int) -> Boolean = remember(hideEmptyDefault, result) {
        { hideEmptyDefault && result.getOrNull(it + 1) !is FeedViewBean }
    }
    val adapter = remember(shouldHideEmptyDefault) {
        val group1Proxy = Group1Proxy(
            isExpand = { feedVisible[it.groupId] ?: false },
            onExpandChange = { data, expand -> feedVisible[data.groupId] = expand },
            onShowAllArticles = onShowAllArticles,
            onDelete = onDeleteGroup,
            onMoveFeedsTo = {
                openSelectGroupDialog = it
                selectGroupDialogCurrentGroup = it
            },
        )
        LazyGridAdapter(
            mutableListOf(
                DefaultGroup1Proxy(
                    group1Proxy = group1Proxy,
                    hide = shouldHideEmptyDefault,
                ),
                group1Proxy,
                Feed1Proxy(
                    visible = { feedVisible[it] ?: false },
                    onRemove = onRemoveFeed,
                    onEdit = onEditFeed
                )
            )
        )
    }
    AniVuLazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(1),
        dataList = result,
        adapter = adapter,
        contentPadding = contentPadding,
        key = { _, item ->
            when (item) {
                is GroupBean.DefaultGroup -> item.groupId
                is GroupBean -> item.groupId
                is FeedViewBean -> item.feed.url
                else -> item
            }
        },
    )

    if (openSelectGroupDialog != null) {
        SelectGroupDialog(
            currentGroup = selectGroupDialogCurrentGroup!!,
            groups = groups,
            onGroupIdChange = { group -> selectGroupDialogCurrentGroup = group },
            onConfirm = { group ->
                onMoveToGroup(openSelectGroupDialog!!, group)
                openSelectGroupDialog = null
            },
            openCreateGroupDialog = openCreateGroupDialog,
            onDismissRequest = { openSelectGroupDialog = null }
        )
    }
}

@Composable
private fun SelectGroupDialog(
    currentGroup: GroupBean,
    groups: List<GroupBean>,
    onGroupIdChange: (GroupBean) -> Unit,
    onConfirm: (groupId: GroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AniVuDialog(
        visible = true,
        icon = { Icon(imageVector = Icons.Outlined.MoveUp, contentDescription = null) },
        title = { Text(stringResource(id = R.string.feed_screen_group_feeds_move_to)) },
        onDismissRequest = onDismissRequest,
        selectable = false,
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                overflow = FlowRowOverflow.Visible
            ) {
                groups.forEach { group ->
                    FilterChip(
                        selected = currentGroup.groupId == group.groupId,
                        onClick = { onGroupIdChange(group) },
                        label = { Text(text = group.name) }
                    )
                }
                AniVuIconButton(
                    onClick = { openCreateGroupDialog() },
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(id = R.string.feed_screen_add_group),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentGroup) }) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}