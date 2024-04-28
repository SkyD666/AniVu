package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.db.dao.GroupDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val groupDao: GroupDao,
    private val rssHelper: RssHelper,
) : BaseRepository() {
    suspend fun requestGroupAnyList(): Flow<List<Any>> {
        return combine(
            groupDao.getGroupWithFeeds(),
            groupDao.getGroupIds(),
        ) { groupList, groupIds ->
            groupList to feedDao.getFeedsNotIn(groupIds)
        }.map { (groupList, defaultFeeds) ->
            val dataList = mutableListOf<Any>()
            dataList.add(GroupBean.DefaultGroup)
            dataList.addAll(defaultFeeds)
            groupList.forEach { group ->
                dataList.add(group.group)
                dataList.addAll(group.feeds)
            }
            dataList
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteGroup(groupId: String): Flow<Int> {
        return if (groupId == GroupBean.DEFAULT_GROUP_ID) flowOf(0)
        else flowOf(groupDao.removeGroupWithFeed(groupId)).flowOn(Dispatchers.IO)
    }

    suspend fun moveGroupFeedsTo(fromGroupId: String, toGroupId: String): Flow<Int> {
        val realFromGroupId = if (fromGroupId == GroupBean.DEFAULT_GROUP_ID) null else fromGroupId
        val realToGroupId = if (toGroupId == GroupBean.DEFAULT_GROUP_ID) null else toGroupId
        return flowOf(groupDao.moveGroupFeedsTo(realFromGroupId, realToGroupId))
            .flowOn(Dispatchers.IO)
    }

    suspend fun setFeed(feedBean: FeedBean): Flow<Unit> {
        return flowOf(feedDao.setFeed(feedBean))
            .flowOn(Dispatchers.IO)
    }

    suspend fun setFeed(
        url: String,
        groupId: String?,
        nickname: String?,
    ): Flow<Unit> {
        return flow {
            val realNickname = if (nickname.isNullOrBlank()) null else nickname
            val realGroupId =
                if (nickname.isNullOrBlank() || groupId == GroupBean.DEFAULT_GROUP_ID) null else groupId
            val feedWithArticleBean = rssHelper.searchFeed(url = url).run {
                copy(
                    feed = feed.copy(
                        groupId = realGroupId,
                        nickname = realNickname,
                    )
                )
            }
            emit(feedDao.setFeedWithArticle(feedWithArticleBean))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun editFeed(
        oldUrl: String,
        newUrl: String,
        nickname: String?,
        groupId: String?,
    ): Flow<Unit> {
        return flow {
            val realNickname = if (nickname.isNullOrBlank()) null else nickname
            val realGroupId =
                if (groupId.isNullOrBlank() || groupId == GroupBean.DEFAULT_GROUP_ID) null else groupId
            if (oldUrl != newUrl) {
                val feedWithArticleBean = rssHelper.searchFeed(url = newUrl).run {
                    copy(
                        feed = feed.copy(
                            groupId = realGroupId,
                            nickname = realNickname,
                        )
                    )
                }
                feedDao.removeFeed(oldUrl)
                feedDao.setFeedWithArticle(feedWithArticleBean)
                emit(Unit)
            } else {
                feedDao.updateFeedGroupId(oldUrl, realNickname, realGroupId)
                emit(Unit)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun removeFeed(url: String): Flow<Int> {
        return flowOf(feedDao.removeFeed(url))
            .flowOn(Dispatchers.IO)
    }

    suspend fun createGroup(group: GroupBean): Flow<Unit> {
        return flowOf(groupDao.setGroup(group))
            .flowOn(Dispatchers.IO)
    }
}