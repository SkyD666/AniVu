package com.skyd.anivu.ui.screen.read

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.skyd.anivu.R
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.ifNullOfBlank
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toEncodedUrl
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.html.HtmlText
import com.skyd.anivu.ui.screen.article.enclosure.EnclosureBottomSheet
import com.skyd.anivu.ui.screen.article.enclosure.getEnclosuresList
import com.skyd.anivu.util.ShareUtil


const val READ_SCREEN_ROUTE = "readScreen"
const val ARTICLE_ID_KEY = "articleId"

fun openReadScreen(
    navController: NavController,
    articleId: String,
    navOptions: NavOptions? = null,
) {
    navController.navigate(
        "${READ_SCREEN_ROUTE}/${articleId.toEncodedUrl(allow = null)}",
        navOptions = navOptions,
    )
}

@Composable
fun ReadScreen(articleId: String, viewModel: ReadViewModel = hiltViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    var openEnclosureBottomSheet by rememberSaveable { mutableStateOf<List<Any>?>(null) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)
    viewModel.getDispatcher(startWith = ReadIntent.Init(articleId))

    var fabHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.CenterAligned,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.read_screen_name)) },
                actions = {
                    AniVuIconButton(
                        enabled = uiState.articleState is ArticleState.Success,
                        onClick = {
                            val articleState = viewModel.viewState.value.articleState
                            if (articleState is ArticleState.Success) {
                                val link = articleState.article.article.link
                                if (!link.isNullOrBlank()) {
                                    link.openBrowser(context)
                                }
                            }
                        },
                        imageVector = Icons.Outlined.Public,
                        contentDescription = stringResource(R.string.read_screen_open_browser),
                    )
                    AniVuIconButton(
                        enabled = uiState.articleState is ArticleState.Success,
                        onClick = {
                            val articleState = viewModel.viewState.value.articleState
                            if (articleState is ArticleState.Success) {
                                val link = articleState.article.article.link
                                val title = articleState.article.article.title
                                if (!link.isNullOrBlank()) {
                                    ShareUtil.shareText(
                                        context = context,
                                        text = if (title.isNullOrBlank()) link else "[$title] $link",
                                    )
                                }
                            }
                        },
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share),
                    )
                }
            )
        },
        floatingActionButton = {
            AniVuFloatingActionButton(
                onSizeWithSinglePaddingChanged = { _, height -> fabHeight = height },
                onClick = {
                    val articleState = viewModel.viewState.value.articleState
                    if (articleState is ArticleState.Success) {
                        openEnclosureBottomSheet = getEnclosuresList(context, articleState.article)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = stringResource(R.string.bottom_sheet_enclosure_title),
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues + 16.dp)
                .padding(bottom = fabHeight),
        ) {
            when (val articleState = uiState.articleState) {
                is ArticleState.Failed -> {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(20.dp),
                        text = articleState.msg,
                    )
                }

                ArticleState.Init,
                ArticleState.Loading -> Unit

                is ArticleState.Success -> {
                    val article = articleState.article
                    SelectionContainer {
                        Column {
                            article.article.title?.let { title ->
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            val date = article.article.date
                            val author = article.article.author
                            if (date != null || !author.isNullOrBlank()) {
                                Row(modifier = Modifier.padding(vertical = 10.dp)) {
                                    if (date != null) {
                                        Text(
                                            text = date.toDateTimeString(context = context),
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (date != null && !author.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    if (!author.isNullOrBlank()) {
                                        Text(
                                            text = author,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HtmlText(
                        text = article.article.content.ifNullOfBlank {
                            article.article.description.orEmpty()
                        }
                    )
                }
            }
        }

        when (val event = uiEvent) {
            is ReadEvent.FavoriteArticleResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            is ReadEvent.ReadArticleResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(message = event.msg, key1 = event)

            null -> Unit
        }

        if (openEnclosureBottomSheet != null) {
            EnclosureBottomSheet(
                onDismissRequest = { openEnclosureBottomSheet = null },
                dataList = openEnclosureBottomSheet.orEmpty()
            )
        }
    }
}