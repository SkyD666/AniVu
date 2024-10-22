package com.skyd.anivu.model.repository.feed

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.feed.FeedBean
import com.skyd.anivu.model.db.dao.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RequestHeadersRepository @Inject constructor(
    private val feedDao: FeedDao,
) : BaseRepository() {
    fun getFeedHeaders(feedUrl: String): Flow<FeedBean.RequestHeaders?> {
        return feedDao.getFeedHeaders(feedUrl).flowOn(Dispatchers.IO)
    }

    suspend fun updateFeedHeaders(feedUrl: String, headers: FeedBean.RequestHeaders): Flow<Unit> {
        return flowOf(feedDao.updateFeedHeaders(feedUrl, headers))
            .flowOn(Dispatchers.IO)
    }
}