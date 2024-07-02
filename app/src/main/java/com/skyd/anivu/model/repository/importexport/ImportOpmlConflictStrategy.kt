package com.skyd.anivu.model.repository.importexport

import android.os.Parcelable
import com.skyd.anivu.R
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.db.dao.GroupDao
import kotlinx.parcelize.Parcelize
import java.util.UUID


@Parcelize
sealed interface ImportOpmlConflictStrategy : Parcelable {
    suspend fun handle(
        groupDao: GroupDao,
        feedDao: FeedDao,
        opmlGroupWithFeed: ImportExportRepository.OpmlGroupWithFeed,
    ): Int

    fun checkOpmlGroupWithFeedFormat(opmlGroupWithFeed: ImportExportRepository.OpmlGroupWithFeed) {
        opmlGroupWithFeed.feeds.forEach {
            check(it.url.isNotBlank()) { "Feed's URL is blank: Feed title: ${it.title}" }
        }
    }

    /**
     * Add or merge a group
     *
     * @return groupId. return null if the group is default group.
     */
    suspend fun addOrMergeGroup(groupDao: GroupDao, group: GroupBean): String? {
        if (group.groupId == GroupBean.DEFAULT_GROUP_ID) {
            return null
        }
        var groupId = UUID.randomUUID().toString()
        if (groupDao.containsByName(group.name) == 0) {
            groupDao.setGroup(
                GroupBean(
                    groupId = groupId,
                    name = group.name,
                )
            )
        } else {
            groupId = groupDao.queryGroupIdByName(group.name)
        }
        return groupId
    }

    val displayName: String

    // Skip on conflict
    @Parcelize
    data object SkipStrategy : ImportOpmlConflictStrategy {
        override suspend fun handle(
            groupDao: GroupDao,
            feedDao: FeedDao,
            opmlGroupWithFeed: ImportExportRepository.OpmlGroupWithFeed,
        ): Int {
            checkOpmlGroupWithFeedFormat(opmlGroupWithFeed)

            var importedCount = 0
            val groupId = addOrMergeGroup(groupDao, opmlGroupWithFeed.group)

            opmlGroupWithFeed.feeds.forEach { feed ->
                // Skip
                if (feedDao.containsByUrl(feed.url) == 0) {
                    feedDao.setFeed(feed.copy(groupId = groupId))
                    importedCount++
                }
            }
            return importedCount
        }

        override val displayName: String
            get() = appContext.getString(R.string.import_opml_conflict_strategy_skip)
    }

    // Replace on conflict
    @Parcelize
    data object ReplaceStrategy : ImportOpmlConflictStrategy {
        override suspend fun handle(
            groupDao: GroupDao,
            feedDao: FeedDao,
            opmlGroupWithFeed: ImportExportRepository.OpmlGroupWithFeed,
        ): Int {
            checkOpmlGroupWithFeedFormat(opmlGroupWithFeed)

            val groupId = addOrMergeGroup(groupDao, opmlGroupWithFeed.group)

            opmlGroupWithFeed.feeds.forEach { feed ->
                // Replace
                // @Insert(onConflict = OnConflictStrategy.REPLACE)
                feedDao.setFeed(feed.copy(groupId = groupId))
            }
            return opmlGroupWithFeed.feeds.size
        }

        override val displayName: String
            get() = appContext.getString(R.string.import_opml_conflict_strategy_replace)
    }
}