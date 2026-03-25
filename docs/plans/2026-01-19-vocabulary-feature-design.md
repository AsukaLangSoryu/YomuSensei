# 词库功能设计方案

**日期**: 2026-01-19
**目标**: 为YomuSensei添加完整的词库功能，支持单词管理和智能复习

## 需求概述

### 核心功能
1. **自动保存** - 阅读时查询的生词自动保存到词库
2. **手动添加** - 用户可以主动添加想学习的单词
3. **单词列表** - 浏览、搜索、筛选所有单词
4. **统计信息** - 显示学习进度和掌握情况
5. **批量操作** - 多选单词进行批量管理
6. **单词详情** - 查看完整信息和复习历史
7. **标签分类** - 给单词打标签，按标签筛选
8. **复习模式** - 选择题测试 + 间隔重复算法

## 技术方案

### 数据存储
使用Room数据库，支持复杂查询和关系管理

## 详细设计

### 1. 数据模型

#### VocabularyWord（单词实体）
```kotlin
@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 基本信息
    val word: String,              // 日语单词/短语
    val reading: String?,          // 读音（假名）
    val meaning: String,           // 中文意思（简短）
    val explanation: String,       // AI的详细解释
    val partOfSpeech: String?,     // 词性（名词/动词/形容词等）
    val category: String?,         // 类别（食物/动物/动作等）

    // 来源信息
    val sourceArticleTitle: String?, // 来源文章标题
    val sourceArticleUrl: String?,   // 来源文章URL
    val addedTime: Long,           // 添加时间
    val isManuallyAdded: Boolean,  // 是否手动添加

    // 用户标记
    val isFavorite: Boolean = false,  // 是否收藏

    // 复习相关
    val reviewCount: Int = 0,      // 复习次数
    val correctCount: Int = 0,     // 答对次数
    val lastReviewTime: Long? = null, // 上次复习时间
    val nextReviewTime: Long? = null, // 下次复习时间
    val reviewLevel: Int = 0       // 复习等级 (0-5)
)
```

#### WordTag（标签）
```kotlin
@Entity(tableName = "word_tags")
data class WordTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val tag: String
)
```

#### ReviewSession（复习记录）
```kotlin
@Entity(tableName = "review_sessions")
data class ReviewSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val reviewTime: Long,
    val isCorrect: Boolean
)
```

#### CachedQuestion（缓存的题目）
```kotlin
@Entity(tableName = "cached_questions")
data class CachedQuestion(
    @PrimaryKey
    val wordId: Long,
    val distractors: List<String>,  // 3个干扰项
    val generatedTime: Long,
    val expiresAt: Long             // 过期时间（7天）
)
```

### 2. 复习算法

#### 简化间隔重复算法
```kotlin
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
            intervalHours * 3600 * 1000

        return Pair(newLevel, nextReviewTime)
    }
}
```

### 3. UI页面结构

#### VocabularyScreen（词库主页面）
使用Tab布局：
- **单词列表Tab**
- **复习模式Tab**

#### 单词列表Tab

**顶部统计卡片：**
```kotlin
StatisticsCard {
    - 总单词数：XXX个
    - 今日新增：XX个
    - 待复习：XX个
    - 掌握率：XX%（Level 4-5的单词占比）
}
```

**搜索和筛选栏：**
```kotlin
SearchAndFilterBar {
    - 搜索框：按单词/意思/标签搜索
    - 筛选按钮，点击展开：
      - 来源文章（多选）
      - 添加方式（自动/手动）
      - 复习等级（0-5）
      - 标签（多选）
      - 是否收藏
    - 排序：时间/字母/复习等级
}
```

**批量操作模式：**
```kotlin
BatchOperationMode {
    - 长按单词卡片进入批量选择模式
    - 顶部显示已选数量和操作按钮：
      - 删除
      - 添加标签
      - 标记为已掌握（设为Level 5）
      - 加入今日复习
      - 取消收藏/设为收藏
}
```

**单词卡片（列表项）：**
```kotlin
WordCard {
    - 左侧：收藏星标（可点击切换）
    - 主体：
      - 单词 + 读音
      - 意思（一行，超出省略）
      - 标签chips（最多显示3个）
      - 底部信息：来源 | Level X | 上次复习时间
    - 右侧：复习进度圆环（显示Level）
    - 点击：进入单词详情页
}
```

**手动添加单词：**
- FAB浮动按钮
- 点击弹出对话框，输入单词
- AI自动获取读音、意思、解释、词性、类别

#### 单词详情页（VocabularyDetailScreen）

```kotlin
VocabularyDetailScreen {
    - 顶部：单词 + 读音（大字体）
    - 收藏按钮
    - 完整意思
    - AI详细解释
    - 词性和类别标签
    - 标签列表（可编辑）
    - 来源文章（可点击跳转）
    - 复习统计卡片：
      - 复习次数 / 正确次数
      - 正确率
      - 当前等级
      - 下次复习时间
    - 复习历史图表（简单的时间线）
    - 底部操作按钮：
      - 编辑
      - 删除
      - 立即复习
}
```

#### 复习模式Tab

```kotlin
ReviewModeTab {
    // 准备界面
    - 显示待复习单词数量
    - 开始复习按钮
    - 后台预生成提示（如果正在准备）

    // 复习界面
    ReviewQuestionScreen {
        - 进度条：X / 总数
        - 显示日语单词 + 读音
        - 4个选项按钮
        - 选择后：
          - 高亮正确答案（绿色）
          - 高亮错误答案（红色，如果选错）
          - 显示"下一题"按钮
    }

    // 完成界面
    ReviewResultScreen {
        - 本次复习数量
        - 正确率
        - 鼓励语
        - 错题列表（可点击查看详情）
        - 返回按钮
    }
}
```

### 4. 与阅读页面的集成

#### 自动保存查询的单词

在`ReaderViewModel`中添加：

```kotlin
class ReaderViewModel(
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository,
    private val vocabularyRepository: VocabularyRepository  // 新增
) : ViewModel() {

    fun onTextSelected(text: String) {
        // ... 现有代码 ...

        viewModelScope.launch {
            val context = _article.value?.content?.take(500) ?: ""
            val result = geminiRepository.explainText(text, context)

            result.fold(
                onSuccess = { explanation ->
                    _explanation.value = TextExplanation(
                        selectedText = text,
                        explanation = explanation,
                        isLoading = false
                    )

                    // 新增：自动保存到词库
                    saveToVocabulary(text, explanation)
                },
                // ...
            )
        }
    }

    private suspend fun saveToVocabulary(word: String, explanation: String) {
        // 检查是否已存在
        val existing = vocabularyRepository.getWordByText(word)
        if (existing != null) return

        // 解析AI返回的解释，提取读音、意思、词性、类别
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
            nextReviewTime = System.currentTimeMillis()  // 立即可复习
        )

        vocabularyRepository.insertWord(vocabularyWord)
    }
}
```

#### 解释面板优化
- 底部添加"已保存到词库"提示（自动消失）
- 如果单词已存在，显示"已在词库中"
- 可选：在设置中添加"自动保存到词库"开关

### 5. 干扰选项生成策略

#### 预生成 + 缓存策略（核心方案）

**后台预生成服务：**
```kotlin
class ReviewPreparationService(
    private val vocabularyRepository: VocabularyRepository,
    private val geminiRepository: GeminiRepository
) {

    // 用户打开词库页面时，后台开始准备
    suspend fun prepareReviewQuestions() {
        val wordsToReview = vocabularyRepository.getWordsForReview(
            System.currentTimeMillis()
        )

        // 批量生成，减少API调用
        val uncachedWords = wordsToReview.filter { word ->
            val cached = vocabularyRepository.getCachedQuestion(word.id)
            cached == null || cached.isExpired()
        }

        if (uncachedWords.isNotEmpty()) {
            batchGenerateAndCache(uncachedWords)
        }
    }

    private suspend fun batchGenerateAndCache(words: List<VocabularyWord>) {
        // 每次最多处理10个单词
        words.chunked(10).forEach { batch ->
            val prompt = """
            为以下日语单词各生成3个错误但看起来合理的中文意思选项。
            要求：干扰项要与正确答案相似但不同，不要太明显错误。

            ${batch.mapIndexed { i, w ->
                "${i+1}. ${w.word} (正确答案：${w.meaning})"
            }.joinToString("\n")}

            返回格式（每行一个单词的3个干扰项，用逗号分隔）：
            1: 干扰项1, 干扰项2, 干扰项3
            2: 干扰项1, 干扰项2, 干扰项3
            ...
            """.trimIndent()

            val result = geminiRepository.generateText(prompt)
            result.onSuccess { response ->
                parseAndCacheDistractors(batch, response)
            }
        }
    }

    private suspend fun parseAndCacheDistractors(
        words: List<VocabularyWord>,
        response: String
    ) {
        val lines = response.lines()
        words.forEachIndexed { index, word ->
            val line = lines.getOrNull(index) ?: return@forEachIndexed
            val distractors = line
                .substringAfter(":")
                .split(",")
                .map { it.trim() }
                .take(3)

            if (distractors.size == 3) {
                vocabularyRepository.cacheQuestion(
                    CachedQuestion(
                        wordId = word.id,
                        distractors = distractors,
                        generatedTime = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + 7 * 24 * 3600 * 1000
                    )
                )
            }
        }
    }
}
```

**复习时使用缓存：**
```kotlin
class ReviewViewModel(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private suspend fun generateQuestion(word: VocabularyWord) {
        val cached = vocabularyRepository.getCachedQuestion(word.id)

        val distractors = if (cached != null && !cached.isExpired()) {
            // 使用缓存，瞬间加载
            cached.distractors
        } else {
            // 缓存失效，使用本地智能方案
            getSmartDistractors(word)
        }

        val options = (listOf(word.meaning) + distractors).shuffled()
        val correctIndex = options.indexOf(word.meaning)

        _currentQuestion.value = ReviewQuestion(
            word = word,
            question = word.word + (word.reading?.let { " ($it)" } ?: ""),
            options = options,
            correctIndex = correctIndex
        )
    }

    // 本地智能方案（备用）
    private suspend fun getSmartDistractors(word: VocabularyWord): List<String> {
        val distractors = mutableListOf<VocabularyWord>()

        // 1. 同词性同类别（最相似）
        if (word.partOfSpeech != null && word.category != null) {
            val similar = vocabularyRepository.getWordsByTypeAndCategory(
                word.partOfSpeech, word.category,
                excludeId = word.id, limit = 2
            )
            distractors.addAll(similar)
        }

        // 2. 同词性不同类别
        if (distractors.size < 3 && word.partOfSpeech != null) {
            val sameType = vocabularyRepository.getWordsByType(
                word.partOfSpeech,
                excludeIds = distractors.map { it.id } + word.id,
                limit = 3 - distractors.size
            )
            distractors.addAll(sameType)
        }

        // 3. 相似长度
        if (distractors.size < 3) {
            val similarLength = vocabularyRepository.getWordsBySimilarLength(
                word.word.length,
                excludeIds = distractors.map { it.id } + word.id,
                limit = 3 - distractors.size
            )
            distractors.addAll(similarLength)
        }

        // 4. 随机
        if (distractors.size < 3) {
            val random = vocabularyRepository.getRandomWords(
                3 - distractors.size,
                excludeIds = distractors.map { it.id } + word.id
            )
            distractors.addAll(random)
        }

        return distractors.take(3).map { it.meaning }
    }
}
```

### 6. VocabularyDao（数据访问层）

```kotlin
@Dao
interface VocabularyDao {
    // 基础操作
    @Insert
    suspend fun insertWord(word: VocabularyWord): Long

    @Update
    suspend fun updateWord(word: VocabularyWord)

    @Delete
    suspend fun deleteWord(word: VocabularyWord)

    @Query("DELETE FROM vocabulary WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<Long>)

    // 查询
    @Query("SELECT * FROM vocabulary ORDER BY addedTime DESC")
    fun getAllWords(): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getWordById(id: Long): VocabularyWord?

    @Query("SELECT * FROM vocabulary WHERE word = :word LIMIT 1")
    suspend fun getWordByText(word: String): VocabularyWord?

    // 搜索和筛选
    @Query("""
        SELECT * FROM vocabulary
        WHERE word LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        ORDER BY addedTime DESC
    """)
    fun searchWords(query: String): Flow<List<VocabularyWord>>

    @Query("""
        SELECT v.* FROM vocabulary v
        LEFT JOIN word_tags t ON v.id = t.wordId
        WHERE t.tag IN (:tags)
        GROUP BY v.id
        ORDER BY v.addedTime DESC
    """)
    fun getWordsByTags(tags: List<String>): Flow<List<VocabularyWord>>

    // 待复习单词
    @Query("""
        SELECT * FROM vocabulary
        WHERE nextReviewTime <= :currentTime
        OR nextReviewTime IS NULL
        ORDER BY RANDOM()
    """)
    suspend fun getWordsForReview(currentTime: Long): List<VocabularyWord>

    // 统计
    @Query("SELECT COUNT(*) FROM vocabulary")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE addedTime >= :startOfDay")
    fun getTodayAddedCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE nextReviewTime <= :currentTime")
    fun getPendingReviewCount(currentTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE reviewLevel >= 4")
    fun getMasteredCount(): Flow<Int>

    // 智能干扰项查询
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
        length: Int, excludeIds: List<Long>, limit: Int
    ): List<VocabularyWord> {
        val minLen = length - 1
        val maxLen = length + 1
        // 实际实现在上面的查询中
    }

    @Query("""
        SELECT * FROM vocabulary
        WHERE id NOT IN (:excludeIds)
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun getRandomWords(limit: Int, excludeIds: List<Long>): List<VocabularyWord>

    // 标签操作
    @Insert
    suspend fun insertTag(tag: WordTag)

    @Query("SELECT * FROM word_tags WHERE wordId = :wordId")
    suspend fun getTagsForWord(wordId: Long): List<WordTag>

    @Query("DELETE FROM word_tags WHERE wordId = :wordId")
    suspend fun deleteTagsForWord(wordId: Long)

    @Query("DELETE FROM word_tags WHERE wordId = :wordId AND tag = :tag")
    suspend fun deleteTag(wordId: Long, tag: String)

    // 缓存题目操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheQuestion(question: CachedQuestion)

    @Query("SELECT * FROM cached_questions WHERE wordId = :wordId")
    suspend fun getCachedQuestion(wordId: Long): CachedQuestion?

    @Query("DELETE FROM cached_questions WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredQuestions(currentTime: Long)

    // 复习记录
    @Insert
    suspend fun insertReviewSession(session: ReviewSession)

    @Query("SELECT * FROM review_sessions WHERE wordId = :wordId ORDER BY reviewTime DESC")
    suspend fun getReviewHistory(wordId: Long): List<ReviewSession>
}
```

## 实现顺序

1. **数据层** - Room数据库、实体、DAO
2. **Repository层** - VocabularyRepository封装数据操作
3. **阅读页面集成** - 自动保存查询的单词
4. **单词列表页面** - 显示、搜索、筛选、批量操作
5. **单词详情页面** - 完整信息展示
6. **手动添加功能** - FAB + 对话框
7. **复习算法** - ReviewScheduler实现
8. **预生成服务** - ReviewPreparationService
9. **复习模式页面** - 选择题界面
10. **统计和优化** - 统计卡片、性能优化

## 预期效果

- 用户阅读时查询的单词自动保存，无需手动操作
- 词库支持完整的管理功能（搜索、筛选、标签、批量操作）
- 复习模式使用科学的间隔重复算法，提高记忆效率
- 干扰选项预生成，复习时无延迟，体验流畅
- 本地智能方案作为备用，保证在任何情况下都能正常使用

## 技术亮点

1. **预生成 + 缓存** - 解决AI生成延迟问题
2. **批量API调用** - 减少API消耗
3. **智能本地备用** - 词性和类别匹配，提高干扰项质量
4. **间隔重复算法** - 科学的复习计划
5. **Room数据库** - 支持复杂查询和关系管理
