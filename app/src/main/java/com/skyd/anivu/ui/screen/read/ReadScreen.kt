package com.skyd.anivu.ui.screen.read

import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import coil.EventListener
import coil.decode.VideoFrameDecoder
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.skyd.anivu.R
import com.skyd.anivu.base.mvi.MviEventListener
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.copy
import com.skyd.anivu.ext.ifNullOfBlank
import com.skyd.anivu.ext.isWifi
import com.skyd.anivu.ext.openBrowser
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.ext.toEncodedUrl
import com.skyd.anivu.model.bean.article.ArticleBean
import com.skyd.anivu.model.bean.article.ArticleWithEnclosureBean
import com.skyd.anivu.model.bean.article.EnclosureBean
import com.skyd.anivu.model.bean.article.RssMediaBean
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.component.AniVuFloatingActionButton
import com.skyd.anivu.ui.component.AniVuIconButton
import com.skyd.anivu.ui.component.AniVuImage
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.dialog.WaitingDialog
import com.skyd.anivu.ui.component.html.HtmlText
import com.skyd.anivu.ui.component.rememberAniVuImageLoader
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    var openEnclosureBottomSheet by rememberSaveable { mutableStateOf<List<Any>?>(null) }

    val uiState by viewModel.viewState.collectAsStateWithLifecycle()
    val dispatcher = viewModel.getDispatcher(startWith = ReadIntent.Init(articleId))

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
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
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

                is ArticleState.Success -> Content(
                    articleState = articleState,
                    shareImage = { dispatcher(ReadIntent.ShareImage(url = it)) },
                    copyImage = { dispatcher(ReadIntent.CopyImage(url = it)) },
                    downloadImage = {
                        dispatcher(
                            ReadIntent.DownloadImage(
                                url = it,
                                title = articleState.article.article.title,
                            )
                        )
                    },
                )
            }
        }

        MviEventListener(viewModel.singleEvent) { event ->
            when (event) {
                is ReadEvent.FavoriteArticleResultEvent.Failed ->
                    snackbarHostState.showSnackbar(event.msg)

                is ReadEvent.ReadArticleResultEvent.Failed -> snackbarHostState.showSnackbar(event.msg)
                is ReadEvent.ShareImageResultEvent.Failed -> snackbarHostState.showSnackbar(event.msg)
                is ReadEvent.CopyImageResultEvent.Failed -> snackbarHostState.showSnackbar(event.msg)
                is ReadEvent.DownloadImageResultEvent.Failed -> snackbarHostState.showSnackbar(event.msg)
                is ReadEvent.CopyImageResultEvent.Success,
                is ReadEvent.DownloadImageResultEvent.Success -> Unit
            }
        }

        WaitingDialog(visible = uiState.loadingDialog)

        if (openEnclosureBottomSheet != null) {
            EnclosureBottomSheet(
                onDismissRequest = { openEnclosureBottomSheet = null },
                dataList = openEnclosureBottomSheet.orEmpty()
            )
        }
    }
}

@Composable
private fun CategoryArea(categories: ArticleBean.Categories) {
    val context = LocalContext.current
    FlowRow(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        categories.categories.forEach { category ->
            SuggestionChip(
                onClick = { category.copy(context) },
                label = { Text(text = category) },
            )
        }
    }
}

@Composable
private fun Content(
    articleState: ArticleState.Success,
    downloadImage: (url: String) -> Unit,
    copyImage: (url: String) -> Unit,
    shareImage: (url: String) -> Unit,
) {
    val context = LocalContext.current
    val article = articleState.article
    var openImageSheet by rememberSaveable { mutableStateOf<String?>(null) }

    SelectionContainer {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            var expandTitle by rememberSaveable { mutableStateOf(false) }
            article.article.title?.let { title ->
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .animateContentSize()
                        .clickable { expandTitle = !expandTitle },
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = if (expandTitle) Int.MAX_VALUE else 3,
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
    MediaRow(articleWithEnclosureBean = article, onPlay = { url ->
        PlayActivity.play(context.activity, Uri.parse(url))
    })
    HtmlText(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = article.article.content.ifNullOfBlank {
            article.article.description.orEmpty()
        },
        onImageClick = { imageUrl -> openImageSheet = imageUrl }
    )
    article.article.catrgories?.let { catrgories ->
        CategoryArea(catrgories)
    }

    if (openImageSheet != null) {
        ImageBottomSheet(
            imageUrl = openImageSheet!!,
            onDismissRequest = { openImageSheet = null },
            shareImage = shareImage,
            copyImage = copyImage,
            downloadImage = downloadImage,
        )
    }
}

@Composable
private fun RssMediaEpisode(modifier: Modifier = Modifier, rssMedia: RssMediaBean) {
    val episode = rssMedia.episode
    if (episode != null) {
        Text(
            modifier = modifier,
            text = stringResource(id = R.string.read_screen_episode, episode),
            color = Color.White,
        )
    }
}

@Composable
private fun RssMediaDuration(modifier: Modifier = Modifier, rssMedia: RssMediaBean) {
    val duration = rssMedia.duration
    if (duration != null) {
        Text(
            modifier = modifier,
            text = DateUtils.formatElapsedTime(duration / 1000),
            color = Color.White,
        )
    }
}

@Composable
private fun MediaRow(articleWithEnclosureBean: ArticleWithEnclosureBean, onPlay: (String) -> Unit) {
    val enclosures = articleWithEnclosureBean.enclosures.filter { it.isMedia }
    val cover = articleWithEnclosureBean.media?.image
    if (enclosures.size > 1) {
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(enclosures) { item ->
                MediaCover(
                    modifier = Modifier
                        .height(160.dp)
                        .widthIn(min = 200.dp),
                    cover = cover,
                    enclosure = item,
                    onClick = { onPlay(item.url) },
                )
            }
        }
        articleWithEnclosureBean.media?.let { media ->
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                RssMediaEpisode(rssMedia = media)
                Spacer(modifier = Modifier.width(12.dp))
                RssMediaDuration(rssMedia = media)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    } else if (enclosures.size == 1) {
        val item = enclosures.first()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            MediaCover(
                modifier = Modifier
                    .height(200.dp)
                    .widthIn(max = 350.dp)
                    .align(Alignment.Center),
                cover = cover,
                enclosure = item,
                onClick = { onPlay(item.url) },
            ) {
                articleWithEnclosureBean.media?.let { media ->
                    RssMediaEpisode(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 15.dp, top = 10.dp),
                        rssMedia = media,
                    )
                    RssMediaDuration(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 15.dp, bottom = 10.dp),
                        rssMedia = media,
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaCover(
    modifier: Modifier = Modifier,
    cover: String?,
    enclosure: EnclosureBean,
    onClick: () -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            var realImage by rememberSaveable(enclosure) {
                mutableStateOf(if (context.isWifi() && enclosure.isVideo) enclosure.url else cover)
            }
            AniVuImage(
                modifier = Modifier.fillMaxHeight(),
                imageLoader = rememberAniVuImageLoader(
                    listener = object : EventListener {
                        override fun onError(request: ImageRequest, result: ErrorResult) {
                            if (cover != null && realImage != cover) {
                                realImage = cover
                            }
                        }
                    },
                    components = { add(VideoFrameDecoder.Factory()) },
                ),
                model = realImage,
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.5f),
                    blendMode = BlendMode.Darken,
                ),
            )
            Icon(
                modifier = Modifier.size(50.dp),
                imageVector = Icons.Outlined.PlayCircleOutline,
                contentDescription = stringResource(id = R.string.play),
                tint = Color.White,
            )
            content()
        }
    }
}

@Composable
private fun ImageBottomSheet(
    imageUrl: String,
    onDismissRequest: () -> Unit,
    shareImage: (url: String) -> Unit,
    copyImage: (url: String) -> Unit,
    downloadImage: (url: String) -> Unit,
) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            ImageBottomSheetItem(
                icon = Icons.Outlined.Download,
                title = stringResource(id = R.string.read_screen_download_image),
                onClick = {
                    downloadImage(imageUrl)
                    onDismissRequest()
                }
            )
            ImageBottomSheetItem(
                icon = Icons.Outlined.Share,
                title = stringResource(id = R.string.share),
                onClick = {
                    shareImage(imageUrl)
                    onDismissRequest()
                }
            )
            ImageBottomSheetItem(
                icon = Icons.Outlined.ContentCopy,
                title = stringResource(id = android.R.string.copy),
                onClick = {
                    copyImage(imageUrl)
                    onDismissRequest()
                }
            )
            ImageBottomSheetItem(
                icon = Icons.Outlined.Public,
                title = stringResource(id = R.string.read_screen_open_image_in_browser),
                onClick = {
                    imageUrl.openBrowser(context)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun ImageBottomSheetItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = title)
    }
}