package com.skyd.anivu.model.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.anivu.appContext
import com.skyd.anivu.model.bean.GROUP_TABLE_NAME
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.bean.GroupWithFeedBean
import com.skyd.anivu.model.repository.tryDeleteFeedIconFile
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
    @Delete
    suspend fun removeGroup(groupBean: GroupBean): Int

    @Transaction
    @Query("DELETE FROM `$GROUP_TABLE_NAME` WHERE ${GroupBean.GROUP_ID_COLUMN} = :groupId")
    suspend fun removeGroup(groupId: String): Int

    @Transaction
    @Query(
        "UPDATE `$GROUP_TABLE_NAME` SET ${GroupBean.NAME_COLUMN} = :name " +
                "WHERE ${GroupBean.GROUP_ID_COLUMN} = :groupId"
    )
    suspend fun renameGroup(groupId: String, name: String): Int

    @Transaction
    suspend fun removeGroupWithFeed(groupId: String): Int {
        removeGroup(groupId)
        return EntryPointAccessors.fromApplication(appContext, GroupDaoEntryPoint::class.java).run {
            feedDao.getFeedsIn(listOf(groupId)).forEach {
                it.feed.customIcon?.let { icon -> tryDeleteFeedIconFile(icon) }
            }
            feedDao.removeFeedByGroupId(groupId)
        }
    }

    @Transaction
    suspend fun moveGroupFeedsTo(fromGroupId: String?, toGroupId: String?): Int {
        return EntryPointAccessors.fromApplication(appContext, GroupDaoEntryPoint::class.java).run {
            feedDao.moveFeedToGroup(fromGroupId = fromGroupId, toGroupId = toGroupId)
        }
    }

    @Transaction
    @Query("SELECT * FROM `$GROUP_TABLE_NAME`")
    fun getGroupWithFeeds(): Flow<List<GroupWithFeedBean>>

    @Transaction
    @Query("SELECT DISTINCT ${GroupBean.GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME`")
    fun getGroupIds(): Flow<List<String>>

    @Transaction
    @Query("SELECT COUNT(*) FROM `$GROUP_TABLE_NAME` WHERE ${GroupBean.NAME_COLUMN} LIKE :name")
    fun containsByName(name: String): Int

    @Transaction
    @Query(
        "SELECT ${GroupBean.GROUP_ID_COLUMN} FROM `$GROUP_TABLE_NAME` " +
                "WHERE ${GroupBean.NAME_COLUMN} LIKE :name " +
                "LIMIT 1"
    )
    fun queryGroupIdByName(name: String): String
}