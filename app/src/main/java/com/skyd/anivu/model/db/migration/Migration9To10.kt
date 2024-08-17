package com.skyd.anivu.model.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.skyd.anivu.model.bean.GROUP_TABLE_NAME
import com.skyd.anivu.model.bean.GroupBean

class Migration9To10 : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `$GROUP_TABLE_NAME` ADD ${GroupBean.PREVIOUS_GROUP_ID_COLUMN} TEXT")
        db.execSQL("ALTER TABLE `$GROUP_TABLE_NAME` ADD ${GroupBean.NEXT_GROUP_ID_COLUMN} TEXT")
    }
}