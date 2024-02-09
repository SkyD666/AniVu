package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.db.dao.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
) : BaseRepository() {
    fun requestFeedList(): Flow<List<FeedBean>> {
        return feedDao.getFeedList()
            .flowOn(Dispatchers.IO)
    }

    suspend fun setFeed(feedBean: FeedBean): Flow<Unit> {
        return flowOf(feedDao.setFeed(feedBean))
            .flowOn(Dispatchers.IO)
    }

    suspend fun setFeed(url: String): Flow<Unit> {
        return flow {
            val feedWithArticleBean = rssHelper.searchFeed(url = url)
            emit(feedDao.setFeedWithArticle(feedWithArticleBean))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun editFeed(oldUrl: String, newUrl: String): Flow<Unit> {
        return flow {
            val feedWithArticleBean = rssHelper.searchFeed(url = newUrl)
            if (oldUrl != newUrl) {
                feedDao.removeFeed(oldUrl)
            }
            emit(feedDao.setFeedWithArticle(feedWithArticleBean))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun removeFeed(url: String): Flow<Int> {
        return flowOf(feedDao.removeFeed(url))
            .flowOn(Dispatchers.IO)
    }
}