package com.skyd.anivu.model.repository

import android.util.Log
import androidx.compose.ui.util.fastMaxBy
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.skyd.anivu.ext.toEncodedUrl
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedWithArticleBean
import com.skyd.anivu.model.service.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import retrofit2.Retrofit
import java.io.InputStream
import java.util.*
import javax.inject.Inject

/**
 * Some operations on RSS.
 */
class RssHelper @Inject constructor(
    private val retrofit: Retrofit,
    private val okHttpClient: OkHttpClient,
) {

    @Throws(Exception::class)
    suspend fun searchFeed(url: String): FeedWithArticleBean {
        return withContext(Dispatchers.IO) {
            val iconAsync = async { getRssIcon(url) }
            val syndFeed = SyndFeedInput().build(XmlReader(inputStream(okHttpClient, url)))
            val feed = FeedBean(
                url = url,
                title = syndFeed.title,
                description = syndFeed.description,
                link = syndFeed.link,
                icon = syndFeed.icon?.link ?: iconAsync.await(),
            )
            val list = syndFeed.entries.map { article(feed, it) }
            FeedWithArticleBean(feed, list)
        }
    }

    suspend fun queryRssXml(
        feed: FeedBean,
        latestLink: String?,        // 日期最新的文章链接，更新时不会take比这个文章更旧的文章
    ): List<ArticleWithEnclosureBean> =
        try {
            inputStream(okHttpClient, feed.url).use { inputStream ->
                SyndFeedInput().apply { isPreserveWireFeed = true }
                    .build(XmlReader(inputStream))
                    .entries
                    .asSequence()
                    .takeWhile { latestLink == null || latestLink != it.link }
                    .map { article(feed, it) }
                    .toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "queryRssXml[${feed.title}]: ${e.message}")
            throw e
        }

    private fun article(
        feed: FeedBean,
        syndEntry: SyndEntry,
    ): ArticleWithEnclosureBean {
        val desc = syndEntry.description?.value
        val content = syndEntry.contents
            .takeIf { it.isNotEmpty() }
            ?.let { list -> list.joinToString("\n") { it.value } }
        Log.i(
            "RLog",
            "request rss:\n" +
                    "name: ${feed.title}\n" +
                    "feedUrl: ${feed.url}\n" +
                    "url: ${syndEntry.link}\n" +
                    "title: ${syndEntry.title}\n" +
                    "desc: ${desc}\n" +
                    "content: ${content}\n"
        )
        val articleId = UUID.randomUUID().toString()
        return ArticleWithEnclosureBean(
            article = ArticleBean(
                articleId = articleId,
                feedUrl = feed.url,
                date = (syndEntry.publishedDate ?: syndEntry.updatedDate ?: Date()).time,
                title = syndEntry.title.toString(),
                author = syndEntry.author,
                description = content ?: desc,
                content = content,
                image = findImg((content ?: desc) ?: ""),
                link = syndEntry.link ?: "",
                updateAt = Date().time,
            ),
            enclosures = syndEntry.enclosures.map {
                EnclosureBean(
                    articleId = articleId,
                    url = it.url.orEmpty().toEncodedUrl(),
                    length = it.length,
                    type = it.type,
                )
            }
        )
    }

    suspend fun getRssIcon(url: String): String? {
        return runCatching {
            retrofit.create(HttpService::class.java)
                .requestFavicon(url)
                .icons?.fastMaxBy { it.width ?: 0 }?.url
        }.onFailure { it.printStackTrace() }.getOrNull()
    }

    fun findImg(rawDescription: String): String? {
        // From: https://gitlab.com/spacecowboy/Feeder
        // Using negative lookahead to skip data: urls, being inline base64
        // And capturing original quote to use as ending quote
        val regex = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)
        // Base64 encoded images can be quite large - and crash database cursors
        return regex.find(rawDescription)?.groupValues?.get(2)?.takeIf { !it.startsWith("data:") }
    }

    private suspend fun inputStream(
        client: OkHttpClient,
        url: String,
    ): InputStream = response(client, url).body.byteStream()

    private suspend fun response(
        client: OkHttpClient,
        url: String,
    ) = client.newCall(Request.Builder().url(url).build()).executeAsync()
}
