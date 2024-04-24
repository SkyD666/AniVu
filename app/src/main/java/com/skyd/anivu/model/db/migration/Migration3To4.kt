package com.skyd.anivu.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.anivu.model.bean.FEED_TABLE_NAME
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.GROUP_TABLE_NAME
import com.skyd.anivu.model.bean.GroupBean

class Migration3To4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE `$GROUP_TABLE_NAME` (
                    ${GroupBean.GROUP_ID_COLUMN} TEXT NOT NULL PRIMARY KEY,
                    ${GroupBean.NAME_COLUMN} TEXT NOT NULL
                )
                """
        )
        db.execSQL("ALTER TABLE $FEED_TABLE_NAME ADD ${FeedBean.GROUP_ID_COLUMN} TEXT")
        db.execSQL("ALTER TABLE $FEED_TABLE_NAME ADD ${FeedBean.NICKNAME_COLUMN} TEXT")
    }
}