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
            ${DownloadInfoBean.FILE_COLUMN} = :file,
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
        file: String?,
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
    fun getDownloadingList(): Flow<List<DownloadInfoBean>>

    @Transaction
    @Query("SELECT * FROM $DOWNLOAD_INFO_TABLE_NAME WHERE ${DownloadInfoBean.PROGRESS_COLUMN} == 1")
    fun getDownloadedList(): Flow<List<DownloadInfoBean>>
}