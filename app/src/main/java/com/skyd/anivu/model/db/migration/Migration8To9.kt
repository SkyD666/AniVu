package com.skyd.anivu.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.anivu.model.bean.download.DOWNLOAD_INFO_TABLE_NAME
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.DESCRIPTION_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.DOWNLOAD_DATE_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.DOWNLOAD_REQUEST_ID_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.DOWNLOAD_STATE_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.LINK_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.NAME_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.PROGRESS_COLUMN
import com.skyd.anivu.model.bean.download.DownloadInfoBean.Companion.SIZE_COLUMN

class Migration8To9 : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${DOWNLOAD_INFO_TABLE_NAME}_Backup (" +
                    "$LINK_COLUMN TEXT PRIMARY KEY NOT NULL, " +
                    "$NAME_COLUMN TEXT NOT NULL, " +
                    "$DOWNLOAD_DATE_COLUMN INTEGER NOT NULL, " +
                    "$SIZE_COLUMN INTEGER NOT NULL, " +
                    "$PROGRESS_COLUMN REAL NOT NULL, " +
                    "$DESCRIPTION_COLUMN TEXT, " +
                    "$DOWNLOAD_STATE_COLUMN TEXT NOT NULL, " +
                    "$DOWNLOAD_REQUEST_ID_COLUMN TEXT NOT NULL" +
                    ")"
        )
        db.execSQL("INSERT INTO ${DOWNLOAD_INFO_TABLE_NAME}_Backup SELECT " +
                "$LINK_COLUMN, $NAME_COLUMN, $DOWNLOAD_DATE_COLUMN, $SIZE_COLUMN, $PROGRESS_COLUMN, " +
                "$DESCRIPTION_COLUMN, $DOWNLOAD_STATE_COLUMN, $DOWNLOAD_REQUEST_ID_COLUMN" +
                " FROM $DOWNLOAD_INFO_TABLE_NAME")
        db.execSQL("DROP TABLE $DOWNLOAD_INFO_TABLE_NAME")
        db.execSQL("ALTER TABLE ${DOWNLOAD_INFO_TABLE_NAME}_Backup RENAME to $DOWNLOAD_INFO_TABLE_NAME")
        db.execSQL("CREATE UNIQUE INDEX index_DownloadInfo_link ON $DOWNLOAD_INFO_TABLE_NAME ($LINK_COLUMN)")
    }
}