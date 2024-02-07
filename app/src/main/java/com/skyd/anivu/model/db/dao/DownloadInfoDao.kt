package com.skyd.anivu.model.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.skyd.anivu.model.bean.DOWNLOAD_INFO_TABLE_NAME
import com.skyd.anivu.model.bean.DownloadInfoBean
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadInfoDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateDownloadInfo(downloadInfo: DownloadInfoBean)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDownloadInfo(downloadInfoBeanList: List<DownloadInfoBean>)

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.DOWNLOAD_REQUEST_ID_COLUMN} = :downloadRequestId
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadInfoRequestId(
        articleId: String,
        link: String,
        downloadRequestId: String,
    )

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.NAME_COLUMN} = :name,
            ${DownloadInfoBean.SIZE_COLUMN} = :size,
            ${DownloadInfoBean.PROGRESS_COLUMN} = :progress
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadInfo(
        articleId: String,
        link: String,
        name: String,
        size: Long,
        progress: Float,
    )

    @Transaction
    @Delete
    suspend fun deleteDownloadInfo(downloadInfoBean: DownloadInfoBean): Int

    @Transaction
    @Query(
        """
        DELETE FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    suspend fun deleteDownloadInfo(
        articleId: String,
        link: String,
    ): Int

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.DOWNLOAD_STATE_COLUMN} = :downloadState
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadState(
        articleId: String,
        link: String,
        downloadState: DownloadInfoBean.DownloadState,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.DOWNLOAD_STATE_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadState(
        articleId: String,
        link: String,
    ): DownloadInfoBean.DownloadState?

    @Transaction
    @Query(
        """
        SELECT * FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadInfo(
        articleId: String,
        link: String,
    ): DownloadInfoBean?

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.DESCRIPTION_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadDescription(
        articleId: String,
        link: String,
    ): String?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.DESCRIPTION_COLUMN} = :description
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadDescription(
        articleId: String,
        link: String,
        description: String?,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.SIZE_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadSize(
        articleId: String,
        link: String,
    ): Long?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.SIZE_COLUMN} = :size
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadSize(
        articleId: String,
        link: String,
        size: Long,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.PROGRESS_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadProgress(
        articleId: String,
        link: String,
    ): Float?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.PROGRESS_COLUMN} = :progress
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadProgress(
        articleId: String,
        link: String,
        progress: Float,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.NAME_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadName(
        articleId: String,
        link: String,
    ): String?

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.DOWNLOADING_DIR_NAME_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadingDirName(
        articleId: String,
        link: String,
    ): String?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.NAME_COLUMN} = :name
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadName(
        articleId: String,
        link: String,
        name: String,
    )

    @Transaction
    @Query(
        """
        SELECT COUNT(1) FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.ARTICLE_ID_COLUMN} = :articleId
        AND ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun containsDownloadInfo(
        articleId: String,
        link: String,
    ): Int

    @Transaction
    @Query("SELECT * FROM $DOWNLOAD_INFO_TABLE_NAME WHERE ${DownloadInfoBean.PROGRESS_COLUMN} < 1")
    fun getDownloadingListFlow(): Flow<List<DownloadInfoBean>>

    @Transaction
    @Query("SELECT * FROM $DOWNLOAD_INFO_TABLE_NAME WHERE ${DownloadInfoBean.PROGRESS_COLUMN} < 1")
    fun getDownloadingList(): List<DownloadInfoBean>

    @Transaction
    @Query("SELECT * FROM $DOWNLOAD_INFO_TABLE_NAME WHERE ${DownloadInfoBean.PROGRESS_COLUMN} == 1")
    fun getDownloadedList(): Flow<List<DownloadInfoBean>>
}