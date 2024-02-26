package com.skyd.anivu.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.anivu.model.bean.download.DOWNLOAD_INFO_TABLE_NAME
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.bean.download.TORRENT_FILE_TABLE_NAME
import com.skyd.anivu.model.bean.download.TorrentFileBean

class Migration2To3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $TORRENT_FILE_TABLE_NAME (
                    ${TorrentFileBean.LINK_COLUMN} TEXT NOT NULL,
                    ${TorrentFileBean.PATH_COLUMN} TEXT NOT NULL,
                    ${TorrentFileBean.SIZE_COLUMN} INTEGER NOT NULL,
                    PRIMARY KEY (${TorrentFileBean.LINK_COLUMN}, ${TorrentFileBean.PATH_COLUMN})
                    FOREIGN KEY (${TorrentFileBean.LINK_COLUMN})
                                REFERENCES $DOWNLOAD_INFO_TABLE_NAME(${DownloadInfoBean.LINK_COLUMN})
                                ON DELETE CASCADE
                )
                """
        )
    }
}