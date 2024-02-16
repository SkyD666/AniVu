package com.skyd.anivu.model.repository

import android.database.DatabaseUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import com.skyd.anivu.appContext
import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.config.allSearchDomain
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.bean.ARTICLE_TABLE_NAME
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.bean.FEED_TABLE_NAME
import com.skyd.anivu.model.bean.FeedBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao
import com.skyd.anivu.model.db.dao.SearchDomainDao
import com.skyd.anivu.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.anivu.model.preference.search.UseRegexSearchPreference
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
) : BaseRepository() {
    fun requestSearchAll(query: String): Flow<List<Any>> {
        return requestSearchFeed(query = query)
            .zip(requestSearchArticle(feedUrl = null, query = query)) { feed, article ->
                feed + article
            }.flowOn(Dispatchers.IO)
    }

    fun requestSearchFeed(
        query: String,
    ): Flow<List<FeedBean>> {
        return flow { emit(genSql(tableName = FEED_TABLE_NAME, k = query)) }.flatMapConcat { sql ->
            feedDao.getFeedList(sql).take(1)
        }.flowOn(Dispatchers.IO)
    }

    fun requestSearchArticle(
        feedUrl: String? = null,
        query: String,
    ): Flow<List<ArticleBean>> {
        return flow {
            emit(
                genSql(
                    tableName = ARTICLE_TABLE_NAME,
                    k = query,
                    leadingFilter = if (feedUrl == null) "1"
                    else "${ArticleBean.FEED_URL_COLUMN} = ${DatabaseUtils.sqlEscapeString(feedUrl)}",
                )
            )
        }.flatMapConcat { sql ->
            articleDao.getArticleList(sql).take(1)
                .map { list -> list.sortedByDescending { it.date } }
        }.flowOn(Dispatchers.IO)
    }

    class SearchRegexInvalidException(message: String?) : IllegalArgumentException(message)

    companion object {
        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface SearchRepositoryEntryPoint {
            val searchDomainDao: SearchDomainDao
        }

        fun genSql(
            tableName: String,
            k: String,
            useRegexSearch: Boolean = appContext.dataStore.getOrDefault(UseRegexSearchPreference),
            // 是否使用多个关键字并集查询
            intersectSearchBySpace: Boolean = appContext.dataStore
                .getOrDefault(IntersectSearchBySpacePreference),
            useSearchDomain: (table: String, column: String) -> Boolean = { table, column ->
                EntryPointAccessors.fromApplication(
                    appContext, SearchRepositoryEntryPoint::class.java
                ).searchDomainDao.getSearchDomain(table, column)
            },
            leadingFilter: String = "1",
            leadingFilterLogicalConnective: String = "AND",
        ): SimpleSQLiteQuery {
            if (useRegexSearch) {
                // Check Regex format
                runCatching { k.toRegex() }.onFailure {
                    throw SearchRegexInvalidException(it.message)
                }
            }

            return if (intersectSearchBySpace) {
                // 以多个连续的空格/制表符/换行符分割
                val keywords = k.trim().split("\\s+".toRegex()).toSet()
                val sql = buildString {
                    keywords.forEachIndexed { i, s ->
                        if (i > 0) append("INTERSECT \n")
                        append(
                            "SELECT * FROM $tableName WHERE ${
                                getFilter(
                                    tableName = tableName,
                                    k = s,
                                    useRegexSearch = useRegexSearch,
                                    useSearchDomain = useSearchDomain,
                                    leadingFilter = leadingFilter,
                                    leadingFilterLogicalConnective = leadingFilterLogicalConnective,
                                )
                            } \n"
                        )
                    }
                }
                SimpleSQLiteQuery(sql)
            } else {
                val sql = buildString {
                    append(
                        "SELECT * FROM $tableName WHERE ${
                            getFilter(
                                tableName = tableName,
                                k = k,
                                useRegexSearch = useRegexSearch,
                                useSearchDomain = useSearchDomain,
                                leadingFilter = leadingFilter,
                                leadingFilterLogicalConnective = leadingFilterLogicalConnective,
                            )
                        } \n"
                    )
                }

                SimpleSQLiteQuery(sql)
            }
        }

        private fun getFilter(
            tableName: String,
            k: String,
            useRegexSearch: Boolean,
            useSearchDomain: (tableName: String, columnName: String) -> Boolean,
            leadingFilter: String = "1",
            leadingFilterLogicalConnective: String = "AND",
        ): String {
            if (k.isBlank()) return leadingFilter

            var filter = "0"

            // 转义输入，防止SQL注入
            val keyword = if (useRegexSearch) {
                // Check Regex format
                runCatching { k.toRegex() }.onFailure {
                    throw SearchRegexInvalidException(it.message)
                }
                DatabaseUtils.sqlEscapeString(k)
            } else {
                DatabaseUtils.sqlEscapeString("%$k%")
            }

            val columns = allSearchDomain[tableName].orEmpty()
            for (column in columns) {
                if (!useSearchDomain(tableName, column)) {
                    continue
                }
                filter += if (useRegexSearch) {
                    " OR $column REGEXP $keyword"
                } else {
                    " OR $column LIKE $keyword"
                }
            }

            if (filter == "0") {
                filter += " OR 1"
            }
            filter = "$leadingFilter $leadingFilterLogicalConnective ($filter)"
            return filter
        }
    }

}