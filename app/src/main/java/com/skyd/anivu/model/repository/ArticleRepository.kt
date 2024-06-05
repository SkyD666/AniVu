package com.skyd.anivu.model.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.ArticleWithFeed
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val rssHelper: RssHelper,
    private val pagingConfig: PagingConfig,
) : BaseRepository() {
    private val filterFavorite = MutableStateFlow<Boolean?>(null)
    private val filterRead = MutableStateFlow<Boolean?>(null)

    fun filterFavorite(favorite: Boolean?) {
        filterFavorite.value = favorite
    }

    fun filterRead(read: Boolean?) {
        filterRead.value = read
    }

    fun requestArticleList(feedUrls: List<String>): Flow<PagingData<ArticleWithFeed>> {
        return combine(filterFavorite, filterRead) { favorite, read ->
            favorite to read
        }.flatMapLatest { (favorite, read) ->
            Pager(pagingConfig) {
                articleDao.getArticlePagingSource(
                    feedUrls = feedUrls,
                    isFavorite = favorite,
                    isRead = read,
                )
            }.flow
        }.flowOn(Dispatchers.IO)
    }

    fun refreshGroupArticles(groupId: String?): Flow<Unit> {
        return flow {
            val realGroupId = if (groupId == GroupBean.DEFAULT_GROUP_ID) null else groupId
            emit(feedDao.getFeedsByGroupId(realGroupId).map { it.feed.url })
        }.flatMapConcat {
            refreshArticleList(it)
        }.flowOn(Dispatchers.IO)
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
                            )?.also { feedWithArticle ->
                                feedDao.updateFeed(feedWithArticle.feed)
                            }?.articles
                        }
                        val articleBeanList = articleBeanListAsync.await() ?: return@async

                        if (articleBeanList.isEmpty()) return@async

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

    fun favoriteArticle(articleId: String, favorite: Boolean): Flow<Unit> {
        return flow {
            emit(articleDao.favoriteArticle(articleId, favorite))
        }.flowOn(Dispatchers.IO)
    }

    fun readArticle(articleId: String, read: Boolean): Flow<Unit> {
        return flow {
            emit(articleDao.readArticle(articleId, read))
        }.flowOn(Dispatchers.IO)
    }
}