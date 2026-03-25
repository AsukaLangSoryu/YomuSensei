package com.yomusensei.data.vocabulary

import kotlinx.coroutines.flow.Flow
import com.yomusensei.data.model.ReviewQuestion
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

        return distractors.take(3).map { it.word }
    }

    suspend fun generateReviewQuestion(word: VocabularyWord): ReviewQuestion? {
        if (word.meaning.isBlank()) return null
        val distractors = getSmartDistractors(word)
        if (distractors.isEmpty()) return null
        val options = (listOf(word.word) + distractors).shuffled()
        val correctIndex = options.indexOf(word.word)
        return ReviewQuestion(
            word = word,
            question = word.meaning,
            options = options,
            correctIndex = correctIndex
        )
    }

    suspend fun updateReviewResult(word: VocabularyWord, isCorrect: Boolean) {
        val (newLevel, nextReviewTime) = ReviewScheduler.calculateNextReview(word.reviewLevel, isCorrect)
        dao.updateWord(
            word.copy(
                reviewLevel = newLevel,
                nextReviewTime = nextReviewTime,
                reviewCount = word.reviewCount + 1,
                correctCount = if (isCorrect) word.correctCount + 1 else word.correctCount,
                lastReviewTime = System.currentTimeMillis()
            )
        )
    }
}
