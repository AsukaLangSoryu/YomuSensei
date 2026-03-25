# 词库功能实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为YomuSensei添加完整的词库功能，支持单词自动保存、管理和智能复习

**Architecture:** 使用Room数据库存储单词数据，实现间隔重复算法，通过预生成+缓存策略解决AI生成延迟，集成到现有的阅读页面

**Tech Stack:** Kotlin, Jetpack Compose, Room Database, Coroutines, Gemini API

---

## Task 1: 添加Room数据库依赖

**Files:**
- Modify: `app/build.gradle.kts`

**Step 1: 添加Room依赖**

在dependencies块中添加：
```kotlin
// Room Database
val room_version = "2.6.1"
implementation("androidx.room:room-runtime:$room_version")
implementation("androidx.room:room-ktx:$room_version")
ksp("androidx.room:room-compiler:$room_version")
```

在plugins块顶部添加KSP插件：
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"  // 新增
}
```

**Step 2: Sync项目**

点击"Sync Now"同步Gradle

**Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "feat: add Room database dependencies"
```

---

## Task 2: 创建数据模型实体

**Files:**
- Create: `app/src/main/java/com/yomusensei/data/vocabulary/VocabularyEntities.kt`

**Step 1: 创建实体文件**

```kotlin
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
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/vocabulary/VocabularyEntities.kt
git commit -m "feat: add vocabulary database entities"
```

---

## Task 3: 创建DAO接口

**Files:**
- Create: `app/src/main/java/com/yomusensei/data/vocabulary/VocabularyDao.kt`

**Step 1: 创建DAO文件**

```kotlin
package com.yomusensei.data.vocabulary

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    // ========== 单词基础操作 ==========
    @Insert
    suspend fun insertWord(word: VocabularyWord): Long

    @Update
    suspend fun updateWord(word: VocabularyWord)

    @Delete
    suspend fun deleteWord(word: VocabularyWord)

    @Query("DELETE FROM vocabulary WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<Long>)

    // ========== 单词查询 ==========
    @Query("SELECT * FROM vocabulary ORDER BY addedTime DESC")
    fun getAllWords(): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getWordById(id: Long): VocabularyWord?

    @Query("SELECT * FROM vocabulary WHERE word = :word LIMIT 1")
    suspend fun getWordByText(word: String): VocabularyWord?

    @Query("""
        SELECT * FROM vocabulary
        WHERE word LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        ORDER BY addedTime DESC
    """)
    fun searchWords(query: String): Flow<List<VocabularyWord>>

    // ========== 待复习单词 ==========
    @Query("""
        SELECT * FROM vocabulary
        WHERE nextReviewTime <= :currentTime
        OR nextReviewTime IS NULL
        ORDER BY RANDOM()
    """)
    suspend fun getWordsForReview(currentTime: Long): List<VocabularyWord>

    // ========== 统计 ==========
    @Query("SELECT COUNT(*) FROM vocabulary")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE addedTime >= :startOfDay")
    fun getTodayAddedCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE nextReviewTime <= :currentTime")
    fun getPendingReviewCount(currentTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE reviewLevel >= 4")
    fun getMasteredCount(): Flow<Int>

    // ========== 智能干扰项查询 ==========
    @Query("""
        SELECT * FROM vocabulary
        WHERE partOfSpeech = :pos AND category = :cat AND id != :excludeId
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun getWordsByTypeAndCategory(
        pos: String, cat: String, excludeId: Long, limit: Int
    ): List<VocabularyWord>

    @Query("""
        SELECT * FROM vocabulary
        WHERE partOfSpeech = :pos AND id NOT IN (:excludeIds)
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun getWordsByType(
        pos: String, excludeIds: List<Long>, limit: Int
    ): List<VocabularyWord>

    @Query("""
        SELECT * FROM vocabulary
        WHERE LENGTH(word) BETWEEN :minLen AND :maxLen
        AND id NOT IN (:excludeIds)
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun getWordsBySimilarLength(
        minLen: Int, maxLen: Int, excludeIds: List<Long>, limit: Int
    ): List<VocabularyWord>

    @Query("""
        SELECT * FROM vocabulary
        WHERE id NOT IN (:excludeIds)
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun getRandomWords(limit: Int, excludeIds: List<Long>): List<VocabularyWord>

    // ========== 标签操作 ==========
    @Insert
    suspend fun insertTag(tag: WordTag)

    @Query("SELECT * FROM word_tags WHERE wordId = :wordId")
    suspend fun getTagsForWord(wordId: Long): List<WordTag>

    @Query("DELETE FROM word_tags WHERE wordId = :wordId")
    suspend fun deleteTagsForWord(wordId: Long)

    @Query("DELETE FROM word_tags WHERE wordId = :wordId AND tag = :tag")
    suspend fun deleteTag(wordId: Long, tag: String)

    @Query("""
        SELECT v.* FROM vocabulary v
        INNER JOIN word_tags t ON v.id = t.wordId
        WHERE t.tag IN (:tags)
        GROUP BY v.id
        ORDER BY v.addedTime DESC
    """)
    fun getWordsByTags(tags: List<String>): Flow<List<VocabularyWord>>

    // ========== 缓存题目操作 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheQuestion(question: CachedQuestion)

    @Query("SELECT * FROM cached_questions WHERE wordId = :wordId")
    suspend fun getCachedQuestion(wordId: Long): CachedQuestion?

    @Query("DELETE FROM cached_questions WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredQuestions(currentTime: Long)

    // ========== 复习记录 ==========
    @Insert
    suspend fun insertReviewSession(session: ReviewSession)

    @Query("SELECT * FROM review_sessions WHERE wordId = :wordId ORDER BY reviewTime DESC")
    suspend fun getReviewHistory(wordId: Long): List<ReviewSession>
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/vocabulary/VocabularyDao.kt
git commit -m "feat: add vocabulary DAO interface"
```

---

## Task 4: 创建Room数据库

**Files:**
- Create: `app/src/main/java/com/yomusensei/data/vocabulary/VocabularyDatabase.kt`

**Step 1: 创建数据库文件**

```kotlin
package com.yomusensei.data.vocabulary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        VocabularyWord::class,
        WordTag::class,
        ReviewSession::class,
        CachedQuestion::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class VocabularyDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao

    companion object {
        @Volatile
        private var INSTANCE: VocabularyDatabase? = null

        fun getDatabase(context: Context): VocabularyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabularyDatabase::class.java,
                    "vocabulary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/vocabulary/VocabularyDatabase.kt
git commit -m "feat: add vocabulary Room database"
```

---

## Task 5: 创建复习调度器

**Files:**
- Create: `app/src/main/java/com/yomusensei/data/vocabulary/ReviewScheduler.kt`

**Step 1: 创建调度器文件**

```kotlin
package com.yomusensei.data.vocabulary

/**
 * 复习调度器 - 简化间隔重复算法
 */
object ReviewScheduler {
    // 复习间隔（小时）
    private val REVIEW_INTERVALS = listOf(
        0,      // Level 0: 立即复习
        24,     // Level 1: 1天后
        72,     // Level 2: 3天后
        168,    // Level 3: 7天后
        720,    // Level 4: 30天后
        2160    // Level 5: 90天后（已掌握）
    )

    /**
     * 计算下次复习时间
     * @param currentLevel 当前复习等级 (0-5)
     * @param isCorrect 本次是否答对
     * @return Pair<新等级, 下次复习时间戳>
     */
    fun calculateNextReview(
        currentLevel: Int,
        isCorrect: Boolean
    ): Pair<Int, Long> {
        val newLevel = if (isCorrect) {
            // 答对：升级
            (currentLevel + 1).coerceAtMost(5)
        } else {
            // 答错：降级（但不低于0）
            (currentLevel - 1).coerceAtLeast(0)
        }

        val intervalHours = REVIEW_INTERVALS[newLevel]
        val nextReviewTime = System.currentTimeMillis() +
            intervalHours * 3600 * 1000L

        return Pair(newLevel, nextReviewTime)
    }

    /**
     * 获取等级描述
     */
    fun getLevelDescription(level: Int): String {
        return when (level) {
            0 -> "新学习"
            1 -> "初步记忆"
            2 -> "短期记忆"
            3 -> "中期记忆"
            4 -> "长期记忆"
            5 -> "已掌握"
            else -> "未知"
        }
    }
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/vocabulary/ReviewScheduler.kt
git commit -m "feat: add review scheduler with spaced repetition algorithm"
```

---

## Task 6: 创建VocabularyRepository

**Files:**
- Create: `app/src/main/java/com/yomusensei/data/vocabulary/VocabularyRepository.kt`

**Step 1: 创建Repository文件**

```kotlin
package com.yomusensei.data.vocabulary

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class VocabularyRepository(private val dao: VocabularyDao) {

    // ========== 单词操作 ==========
    suspend fun insertWord(word: VocabularyWord): Long {
        return dao.insertWord(word)
    }

    suspend fun updateWord(word: VocabularyWord) {
        dao.updateWord(word)
    }

    suspend fun deleteWord(word: VocabularyWord) {
        dao.deleteWord(word)
    }

    suspend fun deleteWords(ids: List<Long>) {
        dao.deleteWords(ids)
    }

    fun getAllWords(): Flow<List<VocabularyWord>> {
        return dao.getAllWords()
    }

    suspend fun getWordById(id: Long): VocabularyWord? {
        return dao.getWordById(id)
    }

    suspend fun getWordByText(word: String): VocabularyWord? {
        return dao.getWordByText(word)
    }

    fun searchWords(query: String): Flow<List<VocabularyWord>> {
        return dao.searchWords(query)
    }

    // ========== 复习相关 ==========
    suspend fun getWordsForReview(): List<VocabularyWord> {
        return dao.getWordsForReview(System.currentTimeMillis())
    }

    suspend fun insertReviewSession(session: ReviewSession) {
        dao.insertReviewSession(session)
    }

    suspend fun getReviewHistory(wordId: Long): List<ReviewSession> {
        return dao.getReviewHistory(wordId)
    }

    // ========== 统计 ==========
    fun getTotalCount(): Flow<Int> {
        return dao.getTotalCount()
    }

    fun getTodayAddedCount(): Flow<Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return dao.getTodayAddedCount(calendar.timeInMillis)
    }

    fun getPendingReviewCount(): Flow<Int> {
        return dao.getPendingReviewCount(System.currentTimeMillis())
    }

    fun getMasteredCount(): Flow<Int> {
        return dao.getMasteredCount()
    }

    // ========== 标签操作 ==========
    suspend fun addTag(wordId: Long, tag: String) {
        dao.insertTag(WordTag(wordId = wordId, tag = tag))
    }

    suspend fun getTagsForWord(wordId: Long): List<String> {
        return dao.getTagsForWord(wordId).map { it.tag }
    }

    suspend fun deleteTag(wordId: Long, tag: String) {
        dao.deleteTag(wordId, tag)
    }

    suspend fun deleteAllTagsForWord(wordId: Long) {
        dao.deleteTagsForWord(wordId)
    }

    fun getWordsByTags(tags: List<String>): Flow<List<VocabularyWord>> {
        return dao.getWordsByTags(tags)
    }

    // ========== 缓存题目 ==========
    suspend fun cacheQuestion(question: CachedQuestion) {
        dao.cacheQuestion(question)
    }

    suspend fun getCachedQuestion(wordId: Long): CachedQuestion? {
        return dao.getCachedQuestion(wordId)
    }

    suspend fun cleanExpiredQuestions() {
        dao.deleteExpiredQuestions(System.currentTimeMillis())
    }

    // ========== 智能干扰项 ==========
    suspend fun getSmartDistractors(word: VocabularyWord): List<String> {
        val distractors = mutableListOf<VocabularyWord>()

        // 1. 同词性同类别
        if (word.partOfSpeech != null && word.category != null) {
            val similar = dao.getWordsByTypeAndCategory(
                word.partOfSpeech!!, word.category!!, word.id, 2
            )
            distractors.addAll(similar)
        }

        // 2. 同词性
        if (distractors.size < 3 && word.partOfSpeech != null) {
            val sameType = dao.getWordsByType(
                word.partOfSpeech!!,
                distractors.map { it.id } + word.id,
                3 - distractors.size
            )
            distractors.addAll(sameType)
        }

        // 3. 相似长度
        if (distractors.size < 3) {
            val minLen = (word.word.length - 1).coerceAtLeast(1)
            val maxLen = word.word.length + 1
            val similarLength = dao.getWordsBySimilarLength(
                minLen, maxLen,
                distractors.map { it.id } + word.id,
                3 - distractors.size
            )
            distractors.addAll(similarLength)
        }

        // 4. 随机
        if (distractors.size < 3) {
            val random = dao.getRandomWords(
                3 - distractors.size,
                distractors.map { it.id } + word.id
            )
            distractors.addAll(random)
        }

        return distractors.take(3).map { it.meaning }
    }
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/vocabulary/VocabularyRepository.kt
git commit -m "feat: add vocabulary repository"
```

---

## Task 7: 在Models.kt中添加词库相关数据类

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/model/Models.kt`

**Step 1: 在文件末尾添加数据类**

```kotlin
/**
 * 复习题目
 */
data class ReviewQuestion(
    val word: VocabularyWord,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

/**
 * 复习结果
 */
data class ReviewResult(
    val word: String,
    val isCorrect: Boolean
)

/**
 * 词库统计信息
 */
data class VocabularyStats(
    val totalCount: Int = 0,
    val todayAdded: Int = 0,
    val pendingReview: Int = 0,
    val masteredCount: Int = 0
) {
    val masteryRate: Float
        get() = if (totalCount > 0) masteredCount.toFloat() / totalCount else 0f
}
```

**Step 2: 添加import**

在文件顶部添加：
```kotlin
import com.yomusensei.data.vocabulary.VocabularyWord
```

**Step 3: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/model/Models.kt
git commit -m "feat: add vocabulary related data models"
```

---

## Task 8: 更新ReaderViewModel集成词库

**Files:**
- Modify: `app/src/main/java/com/yomusensei/ui/reader/ReaderViewModel.kt`

**Step 1: 添加VocabularyRepository依赖**

修改构造函数：
```kotlin
class ReaderViewModel(
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository,
    private val vocabularyRepository: VocabularyRepository  // 新增
) : ViewModel() {
```

**Step 2: 添加自动保存方法**

在类中添加：
```kotlin
/**
 * 保存单词到词库
 */
private suspend fun saveToVocabulary(word: String, explanation: String) {
    // 检查是否已存在
    val existing = vocabularyRepository.getWordByText(word)
    if (existing != null) return

    // 解析AI返回的解释
    val parsedInfo = parseExplanation(explanation)

    val vocabularyWord = VocabularyWord(
        word = word,
        reading = parsedInfo.reading,
        meaning = parsedInfo.meaning,
        explanation = explanation,
        partOfSpeech = parsedInfo.partOfSpeech,
        category = parsedInfo.category,
        sourceArticleTitle = _article.value?.title,
        sourceArticleUrl = _article.value?.url,
        addedTime = System.currentTimeMillis(),
        isManuallyAdded = false,
        nextReviewTime = System.currentTimeMillis()
    )

    vocabularyRepository.insertWord(vocabularyWord)
}

/**
 * 解析AI返回的解释
 */
private data class ParsedInfo(
    val reading: String?,
    val meaning: String,
    val partOfSpeech: String?,
    val category: String?
)

private fun parseExplanation(explanation: String): ParsedInfo {
    val lines = explanation.lines()
    var reading: String? = null
    var meaning = explanation
    var partOfSpeech: String? = null
    var category: String? = null

    for (line in lines) {
        when {
            line.startsWith("読み：") || line.startsWith("读音：") -> {
                reading = line.substringAfter("：").trim()
            }
            line.startsWith("意味：") || line.startsWith("意思：") -> {
                meaning = line.substringAfter("：").trim()
            }
            line.startsWith("词性：") || line.startsWith("品詞：") -> {
                partOfSpeech = line.substringAfter("：").trim()
            }
            line.startsWith("类别：") || line.startsWith("カテゴリ：") -> {
                category = line.substringAfter("：").trim()
            }
        }
    }

    return ParsedInfo(reading, meaning, partOfSpeech, category)
}
```

**Step 3: 在onTextSelected中调用保存**

修改onTextSelected方法，在成功获取解释后添加：
```kotlin
result.fold(
    onSuccess = { explanation ->
        _explanation.value = TextExplanation(
            selectedText = text,
            explanation = explanation,
            isLoading = false
        )

        // 新增：自动保存到词库
        viewModelScope.launch {
            saveToVocabulary(text, explanation)
        }
    },
    // ...
)
```

**Step 4: 添加import**

```kotlin
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.vocabulary.VocabularyWord
```

**Step 5: Commit**

```bash
git add app/src/main/java/com/yomusensei/ui/reader/ReaderViewModel.kt
git commit -m "feat: integrate vocabulary auto-save in reader"
```

---

## 总结

这个计划包含了词库功能的核心数据层实现：

1. ✅ Room数据库依赖
2. ✅ 数据模型实体（单词、标签、复习记录、缓存题目）
3. ✅ DAO接口（完整的数据访问方法）
4. ✅ Room数据库配置
5. ✅ 复习调度器（间隔重复算法）
6. ✅ Repository层（业务逻辑封装）
7. ✅ 数据模型扩展
8. ✅ 阅读页面集成（自动保存单词）

**下一步：**
- UI层实现（词库页面、复习模式页面）
- 预生成服务（后台生成干扰项）
- 导航集成

由于计划较长，UI层将在下一个计划中实现。
