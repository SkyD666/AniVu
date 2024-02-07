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
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadInfoRequestId(
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
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadInfo(
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
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    suspend fun deleteDownloadInfo(
        link: String,
    ): Int

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.DOWNLOAD_STATE_COLUMN} = :downloadState
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadState(
        link: String,
        downloadState: DownloadInfoBean.DownloadState,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.DOWNLOAD_STATE_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadState(
        link: String,
    ): DownloadInfoBean.DownloadState?

    @Transaction
    @Query(
        """
        SELECT * FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadInfo(
        link: String,
    ): DownloadInfoBean?

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.DESCRIPTION_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadDescription(
        link: String,
    ): String?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.DESCRIPTION_COLUMN} = :description
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadDescription(
        link: String,
        description: String?,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.SIZE_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadSize(
        link: String,
    ): Long?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.SIZE_COLUMN} = :size
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadSize(
        link: String,
        size: Long,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.PROGRESS_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadProgress(
        link: String,
    ): Float?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.PROGRESS_COLUMN} = :progress
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadProgress(
        link: String,
        progress: Float,
    )

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.NAME_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadName(
        link: String,
    ): String?

    @Transaction
    @Query(
        """
        SELECT ${DownloadInfoBean.DOWNLOADING_DIR_NAME_COLUMN} FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun getDownloadingDirName(
        link: String,
    ): String?

    @Transaction
    @Query(
        """
        UPDATE $DOWNLOAD_INFO_TABLE_NAME
        SET ${DownloadInfoBean.NAME_COLUMN} = :name
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun updateDownloadName(
        link: String,
        name: String,
    )

    @Transaction
    @Query(
        """
        SELECT COUNT(1) FROM $DOWNLOAD_INFO_TABLE_NAME
        WHERE ${DownloadInfoBean.LINK_COLUMN} = :link
        """
    )
    fun containsDownloadInfo(
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