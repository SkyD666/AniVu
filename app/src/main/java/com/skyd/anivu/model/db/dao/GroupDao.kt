package com.skyd.anivu.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.group.GROUP_TABLE_NAME
import com.skyd.anivu.model.bean.group.GroupBean
import com.skyd.anivu.model.bean.group.GroupVo
import com.skyd.anivu.model.bean.group.GroupWithFeedBean
import com.skyd.anivu.model.repository.feed.tryDeleteFeedIconFile
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GroupDaoEntryPoint {
        val feedDao: FeedDao
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGroup(groupBean: GroupBean)

    @Transaction
    @Query("SELECT * FROM `$GROUP_TABLE_NAME` WHERE ${GroupBean.GROUP_ID_COLUMN} = :groupId")
    suspend fun getGroupById(groupId: String): GroupBean

    @Transaction
    @Query("DELETE FROM `$GROUP_TABLE_NAME` WHERE ${GroupBean.GROUP_ID_COLUMN} = :groupId")
    suspend fun innerRemoveGroup(groupId: String): Int

    @Transaction
    @Query(
        "UPDATE `$GROUP_TABLE_NAME` SET ${GroupBean.NAME_COLUMN} = :name " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :groupId"
    )
    suspend fun renameGroup(groupId: String, name: String): Int

    @Transaction
    @Query(
        "UPDATE `$GROUP_TABLE_NAME` SET ${GroupBean.PREVIOUS_GROUP_ID_COLUMN} = :previousGroupId " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :currentGroupId"
    )
    suspend fun updatePreviousGroup(currentGroupId: String, previousGroupId: String?): Int

    @Transaction
    @Query(
        "UPDATE `$GROUP_TABLE_NAME` SET ${GroupBean.NEXT_GROUP_ID_COLUMN} = :nextGroupId " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :currentGroupId"
    )
    suspend fun updateNextGroup(currentGroupId: String, nextGroupId: String?): Int

    @Transaction
    @Query(
        "SELECT ${GroupBean.PREVIOUS_GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME` " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :currentGroupId"
    )
    suspend fun getPreviousGroupId(currentGroupId: String): String?

    @Transaction
    @Query(
        "SELECT ${GroupBean.NEXT_GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME` " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :currentGroupId"
    )
    suspend fun getNextGroupId(currentGroupId: String): String?

    private suspend fun removeGroupIdFromList(groupId: String) {
        // Linked list
        val previousGroupId = getPreviousGroupId(groupId)
        val nextGroupId = getNextGroupId(groupId)
        if (groupId == previousGroupId ||
            groupId == nextGroupId ||
            previousGroupId != null && previousGroupId == nextGroupId
        ) {
            return
        }
        if (previousGroupId != null) {
            updateNextGroup(currentGroupId = previousGroupId, nextGroupId = nextGroupId)
        }
        if (nextGroupId != null) {
            updatePreviousGroup(currentGroupId = nextGroupId, previousGroupId = previousGroupId)
        }
    }

    private suspend fun addGroupIdToList(
        groupId: String,
        previousGroupId: String?,
        nextGroupId: String?,
    ): Boolean {
        if (groupId == previousGroupId ||
            groupId == nextGroupId ||
            previousGroupId != null && previousGroupId == nextGroupId
        ) {
            return false
        }
        // Linked list
        if (previousGroupId != null) {
            updateNextGroup(
                currentGroupId = previousGroupId,
                nextGroupId = groupId,
            )
        }
        if (nextGroupId != null) {
            updatePreviousGroup(
                currentGroupId = nextGroupId,
                previousGroupId = groupId,
            )
        }
        updatePreviousGroup(
            currentGroupId = groupId,
            previousGroupId = previousGroupId,
        )
        updateNextGroup(
            currentGroupId = groupId,
            nextGroupId = nextGroupId,
        )
        return true
    }

    @Transaction
    suspend fun removeGroupWithFeed(groupId: String): Int {
        removeGroupIdFromList(groupId)
        innerRemoveGroup(groupId)
        return EntryPointAccessors.fromApplication(appContext, GroupDaoEntryPoint::class.java).run {
            feedDao.getFeedsInGroup(listOf(groupId)).forEach {
                it.feed.customIcon?.let { icon -> tryDeleteFeedIconFile(icon) }
            }
            feedDao.removeFeedByGroupId(groupId)
        }
    }

    @Transaction
    suspend fun reorderGroup(
        groupId: String,
        newPreviousGroupId: String? = null,
        newNextGroupId: String? = null,
    ): Boolean {
        if (groupId == GroupVo.DEFAULT_GROUP_ID ||
            newPreviousGroupId == GroupVo.DEFAULT_GROUP_ID ||
            newNextGroupId == GroupVo.DEFAULT_GROUP_ID ||
            containsById(groupId) == 0 ||
            newPreviousGroupId != null && containsById(newPreviousGroupId) == 0 ||
            newNextGroupId != null && containsById(newNextGroupId) == 0 ||
            groupId == newPreviousGroupId ||
            groupId == newNextGroupId ||
            newPreviousGroupId != null && newPreviousGroupId == newNextGroupId
        ) {
            return false
        }
        // Linked list
        removeGroupIdFromList(groupId)
        return addGroupIdToList(
            groupId,
            previousGroupId = newPreviousGroupId,
            nextGroupId = newNextGroupId,
        )
    }

    @Transaction
    @Query(
        "UPDATE `$GROUP_TABLE_NAME` SET ${GroupBean.PREVIOUS_GROUP_ID_COLUMN} = NULL, " +
                "${GroupBean.NEXT_GROUP_ID_COLUMN} = NULL"
    )
    suspend fun resetGroupOrder(): Int

    @Transaction
    suspend fun moveGroupFeedsTo(fromGroupId: String?, toGroupId: String?): Int {
        return EntryPointAccessors.fromApplication(appContext, GroupDaoEntryPoint::class.java).run {
            feedDao.moveFeedToGroup(fromGroupId = fromGroupId, toGroupId = toGroupId)
        }
    }

    @Transaction
    @Query(
        "UPDATE `$GROUP_TABLE_NAME` SET ${GroupBean.IS_EXPANDED_COLUMN} = :expanded " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :groupId"
    )
    suspend fun changeGroupExpanded(groupId: String, expanded: Boolean): Int

    @Transaction
    @Query("SELECT * FROM `$GROUP_TABLE_NAME`")
    fun getGroupWithFeeds(): Flow<List<GroupWithFeedBean>>

    @Transaction
    @Query("SELECT * FROM `$GROUP_TABLE_NAME`")
    fun getGroups(): Flow<List<GroupBean>>

    @Transaction
    @Query("SELECT DISTINCT ${GroupBean.GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME`")
    fun getGroupIds(): Flow<List<String>>

    @Transaction
    @Query("SELECT COUNT(*) FROM `$GROUP_TABLE_NAME` WHERE ${GroupBean.NAME_COLUMN} LIKE :name")
    fun containsByName(name: String): Int

    @Transaction
    @Query("SELECT COUNT(*) FROM `$GROUP_TABLE_NAME` WHERE ${GroupBean.GROUP_ID_COLUMN} LIKE :groupId")
    fun containsById(groupId: String): Int

    @Transaction
    @Query(
        "SELECT ${GroupBean.GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME` " +
                "WHERE ${GroupBean.NAME_COLUMN} LIKE :name " +
                "LIMIT 1"
    )
    fun queryGroupIdByName(name: String): String
}