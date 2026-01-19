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
