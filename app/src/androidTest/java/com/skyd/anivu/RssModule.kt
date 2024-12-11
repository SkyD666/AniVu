package com.skyd.anivu

import android.content.Context
import androidx.paging.PagingConfig
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.skyd.anivu.base.mvi.mviViewModelNeedMainThread
import com.skyd.anivu.config.Const
import com.skyd.anivu.model.bean.group.GroupVo
import com.skyd.anivu.model.db.AppDatabase
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.db.dao.GroupDao
import com.skyd.anivu.model.repository.ArticleRepository
import com.skyd.anivu.model.repository.ReadRepository
import com.skyd.anivu.model.repository.RssHelper
import com.skyd.anivu.model.repository.SearchRepository
import com.skyd.anivu.model.repository.feed.FeedRepository
import com.skyd.anivu.model.repository.feed.ReorderGroupRepository
import com.skyd.anivu.model.repository.feed.RequestHeadersRepository
import com.skyd.anivu.util.favicon.FaviconExtractor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import retrofit2.Retrofit
import java.io.IOException


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class RssModule {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit
        .Builder()
        .baseUrl(Const.BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(okHttpClient)
        .build()

    private val faviconExtractor = FaviconExtractor(retrofit)
    private val pagingConfig = PagingConfig(pageSize = 20, enablePlaceholders = false)

    private lateinit var db: AppDatabase
    private lateinit var groupDao: GroupDao
    private lateinit var feedDao: FeedDao
    private lateinit var articleDao: ArticleDao
    private var rssHelper: RssHelper = RssHelper(okHttpClient, faviconExtractor)
    private lateinit var reorderGroupRepository: ReorderGroupRepository
    private lateinit var feedRepository: FeedRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var readRepository: ReadRepository
    private lateinit var searchRepository: SearchRepository
    private lateinit var requestHeadersRepository: RequestHeadersRepository


    @Test
    fun test1RequestGroupAnyList() = runTest {
        feedRepository.requestGroupAnyList().first().also {
            assertTrue(it[0] is GroupVo.DefaultGroup)
        }
    }

    @Test
    fun test2SetFeed() = runTest {
        feedRepository.setFeed(
            url = "https://blogs.nvidia.cn/feed/",
            groupId = null,
            nickname = "Nvidia",
        ).first().also {
            assertTrue(
                it.url == "https://blogs.nvidia.cn/feed/" &&
                        it.groupId == null && it.nickname == "Nvidia"
            )
        }
    }

    @Before
    fun init() {
        mviViewModelNeedMainThread = false

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        groupDao = db.groupDao()
        feedDao = db.feedDao()
        articleDao = db.articleDao()

        reorderGroupRepository = ReorderGroupRepository(groupDao)
        feedRepository =
            FeedRepository(groupDao, feedDao, articleDao, reorderGroupRepository, rssHelper)
        articleRepository = ArticleRepository(feedDao, articleDao, rssHelper, pagingConfig)
        searchRepository = SearchRepository(feedDao, articleDao, pagingConfig)
        requestHeadersRepository = RequestHeadersRepository(feedDao)
    }

    @After
    @Throws(IOException::class)
    fun destroy() {
//        db.close()
    }
}