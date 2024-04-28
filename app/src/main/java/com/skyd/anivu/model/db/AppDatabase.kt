package com.skyd.anivu.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.EnclosureBean
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.bean.FeedViewBean
import com.skyd.anivu.model.bean.GroupBean
import com.skyd.anivu.model.bean.download.DownloadInfoBean
import com.skyd.anivu.model.bean.download.DownloadLinkUuidMapBean
import com.skyd.anivu.model.bean.download.SessionParamsBean
import com.skyd.anivu.model.bean.download.TorrentFileBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.DownloadInfoDao
import com.skyd.anivu.model.db.dao.EnclosureDao
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.db.dao.GroupDao
import com.skyd.anivu.model.db.dao.SessionParamsDao
import com.skyd.anivu.model.db.dao.TorrentFileDao
import com.skyd.anivu.model.db.migration.Migration1To2
import com.skyd.anivu.model.db.migration.Migration2To3
import com.skyd.anivu.model.db.migration.Migration3To4
import com.skyd.anivu.model.db.migration.Migration4To5

const val APP_DATA_BASE_FILE_NAME = "app.db"

@Database(
    entities = [
        FeedBean::class,
        ArticleBean::class,
        EnclosureBean::class,
        DownloadInfoBean::class,
        DownloadLinkUuidMapBean::class,
        SessionParamsBean::class,
        TorrentFileBean::class,
        GroupBean::class,
    ],
    views = [FeedViewBean::class],
    version = 5,
)
@TypeConverters(
    value = []
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun enclosureDao(): EnclosureDao
    abstract fun downloadInfoDao(): DownloadInfoDao
    abstract fun torrentFileDao(): TorrentFileDao
    abstract fun sessionParamsDao(): SessionParamsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val migrations =
            arrayOf(Migration1To2(), Migration2To3(), Migration3To4(), Migration4To5())

        fun getInstance(context: Context): AppDatabase {
            return if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            APP_DATA_BASE_FILE_NAME
                        )
                            .addMigrations(*migrations)
                            .build()
                            .apply { instance = this }
                    } else {
                        instance as AppDatabase
                    }
                }
            } else {
                instance as AppDatabase
            }
        }
    }
}
