package com.skyd.anivu.model.db.converter

import androidx.room.TypeConverter
import com.skyd.anivu.model.bean.article.ArticleBean
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CategoriesConverter {
    @TypeConverter
    fun fromString(string: String?): ArticleBean.Categories? {
        string ?: return null
        return ArticleBean.Categories(categories = Json.decodeFromString(string))
    }

    @TypeConverter
    fun categoriesToString(categories: ArticleBean.Categories?): String? {
        val list = categories?.categories ?: return null
        return Json.encodeToString(list)
    }
}