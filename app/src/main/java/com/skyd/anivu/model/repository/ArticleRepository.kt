package com.skyd.anivu.model.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val rssHelper: RssHelper,
    private val pagingConfig: PagingConfig,
) : BaseRepository() {
    fun requestArticleList(feedUrls: List<String>): Flow<PagingData<ArticleWithFeed>> {
        return Pager(pagingConfig) {
            articleDao.getArticlePagingSource(feedUrls)
        }.flow.flowOn(Dispatchers.IO)
    }

    fun refreshArticleList(feedUrls: List<String>): Flow<Unit> {
        return flow {
            emit(coroutineScope {
                val requests = mutableListOf<Deferred<Unit>>()
                feedUrls.forEach { feedUrl ->
                    requests += async {
                        val articleBeanListAsync = async {
                            rssHelper.queryRssXml(
                                feed = feedDao.getFeed(feedUrl),
                                latestLink = articleDao.queryLatestByFeedUrl(feedUrl)?.link
                            )
                        }
                        val iconAsync = async { rssHelper.getRssIcon(feedUrl) }
                        val articleBeanList = articleBeanListAsync.await()

                        if (articleBeanList.isEmpty()) return@async

                        val icon = iconAsync.await()
                        if (icon != null) feedDao.updateFeedIcon(feedUrl, icon)

                        articleDao.insertListIfNotExist(articleBeanList.map { articleWithEnclosure ->
                            if (articleWithEnclosure.article.feedUrl != feedUrl) {
                                articleWithEnclosure.copy(
                                    article = articleWithEnclosure.article.copy(feedUrl = feedUrl)
                                )
                            } else articleWithEnclosure
                        })
                    }
                }
                requests.forEach { it.await() }
            })
        }.flowOn(Dispatchers.IO)
    }
}