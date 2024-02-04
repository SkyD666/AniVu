package com.skyd.anivu.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao

const val APP_DATA_BASE_FILE_NAME = "app.db"

@Database(
    entities = [
        FeedBean::class,
        ArticleBean::class,
    ],
    version = 1
)
@TypeConverters(
    value = []
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val migrations = arrayOf<Migration>()

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
