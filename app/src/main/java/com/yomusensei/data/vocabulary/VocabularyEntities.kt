package com.yomusensei.data.vocabulary

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 单词实体
 */
@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 基本信息
    val word: String,
    val reading: String?,
    val meaning: String,
    val explanation: String,
    val partOfSpeech: String?,
    val category: String?,

    // 来源信息
    val sourceArticleTitle: String?,
    val sourceArticleUrl: String?,
    val addedTime: Long,
    val isManuallyAdded: Boolean,

    // 用户标记
    val isFavorite: Boolean = false,

    // 复习相关
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val lastReviewTime: Long? = null,
    val nextReviewTime: Long? = null,
    val reviewLevel: Int = 0
)

/**
 * 标签实体
 */
@Entity(tableName = "word_tags")
data class WordTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val tag: String
)

/**
 * 复习记录实体
 */
@Entity(tableName = "review_sessions")
data class ReviewSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val reviewTime: Long,
    val isCorrect: Boolean
)

/**
 * 缓存的题目实体
 */
@Entity(tableName = "cached_questions")
@TypeConverters(StringListConverter::class)
data class CachedQuestion(
    @PrimaryKey
    val wordId: Long,
    val distractors: List<String>,
    val generatedTime: Long,
    val expiresAt: Long
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }
}

/**
 * List<String>类型转换器
 */
class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return gson.toJson(list)
    }
}
