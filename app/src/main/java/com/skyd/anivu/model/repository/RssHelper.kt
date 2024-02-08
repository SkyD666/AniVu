package com.skyd.anivu.model.repository

import android.content.Context
import android.util.Log
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.skyd.anivu.ext.toEncodedUrl
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.ArticleWithEnclosureBean
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedWithArticleBean
import com.skyd.anivu.model.db.dao.FeedDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import java.io.InputStream
import java.util.*
import javax.inject.Inject

/**
 * Some operations on RSS.
 */
class RssHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val okHttpClient: OkHttpClient,
) {

    @Throws(Exception::class)
    suspend fun searchFeed(url: String): FeedWithArticleBean {
        return withContext(Dispatchers.IO) {
            val syndFeed = SyndFeedInput().build(XmlReader(inputStream(okHttpClient, url)))
            val feed = FeedBean(
                url = url,
                title = syndFeed.title,
                description = syndFeed.description,
                link = syndFeed.link,
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

    fun findImg(rawDescription: String): String? {
        // From: https://gitlab.com/spacecowboy/Feeder
        // Using negative lookahead to skip data: urls, being inline base64
        // And capturing original quote to use as ending quote
        val regex = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)
        // Base64 encoded images can be quite large - and crash database cursors
        return regex.find(rawDescription)?.groupValues?.get(2)?.takeIf { !it.startsWith("data:") }
    }

    @Throws(Exception::class)
    suspend fun queryRssIcon(
        feedDao: FeedDao,
        feed: FeedBean,
        articleLink: String,
    ) {
        withContext(Dispatchers.IO) {
            val domainRegex = Regex("(http|https)://(www.)?(\\w+(\\.)?)+")
            val request = response(okHttpClient, articleLink)
            val content = request.body.string()
            val regex = Regex("""<link(.+?)rel="shortcut icon"(.+?)href="(.+?)"""")
            var iconLink = regex
                .find(content)
                ?.groups?.get(3)
                ?.value
            Log.i("rlog", "queryRssIcon: $iconLink")
            if (iconLink != null) {
                if (iconLink.startsWith("//")) {
                    iconLink = "http:$iconLink"
                }
                if (iconLink.startsWith("/")) {
                    iconLink = "${domainRegex.find(articleLink)?.value}$iconLink"
                }
                saveRssIcon(feedDao, feed, iconLink)
            } else {
                domainRegex.find(articleLink)?.value?.let {
                    Log.i("RLog", "favicon: $it")
                    if (response(okHttpClient, "$it/favicon.ico").isSuccessful) {
                        saveRssIcon(feedDao, feed, it)
                    }
                }
            }
        }
    }

    private suspend fun saveRssIcon(feedDao: FeedDao, feed: FeedBean, iconLink: String) {
        feedDao.setFeed(feed.copy(icon = iconLink))
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
