package com.yomusensei.data.local

import com.google.gson.Gson
import com.yomusensei.data.api.JishoApiService
import com.yomusensei.data.model.DictionaryEntry
import com.yomusensei.data.model.Meaning

class DictionaryRepository(
    private val dictionaryDao: DictionaryDao,
    private val jishoService: JishoApiService
) {
    private val gson = Gson()

    suspend fun lookup(word: String): DictionaryEntry? {
        // 1. 先查本地词典
        val localWord = dictionaryDao.getWord(word)
        if (localWord != null) {
            return localWord.toDictionaryEntry()
        }

        // 2. 查在线 API（带缓存）
        val onlineEntry = JishoApiService.searchWithCache(jishoService, word)
        if (onlineEntry != null) {
            // 保存到本地
            cacheWord(onlineEntry)
            return onlineEntry
        }

        return null
    }

    private suspend fun cacheWord(entry: DictionaryEntry) {
        val dbWord = DictionaryWord(
            word = entry.word,
            reading = entry.reading,
            meanings = gson.toJson(entry.meanings),
            partOfSpeech = entry.meanings.firstOrNull()?.partOfSpeech,
            jlptLevel = entry.jlptLevel,
            frequency = 0,
            isCommon = entry.commonness > 0,
            examples = if (entry.examples.isNotEmpty()) gson.toJson(entry.examples) else null
        )
        dictionaryDao.insertWord(dbWord)
    }

    suspend fun getWordCount(): Int = dictionaryDao.getWordCount()

    suspend fun browseWords(searchQuery: String, kana: String?): List<DictionaryEntry> {
        val words = when {
            searchQuery.isNotEmpty() -> dictionaryDao.searchWords("%$searchQuery%")
            kana != null -> dictionaryDao.getWordsByKana("$kana%")
            else -> dictionaryDao.getAllWordsSorted()
        }
        return words.map { it.toDictionaryEntry() }
    }
}

private fun DictionaryWord.toDictionaryEntry(): DictionaryEntry {
    val gson = Gson()
    val meaningsList = gson.fromJson(meanings, Array<Meaning>::class.java).toList()
    val examplesList = examples?.let {
        gson.fromJson(it, Array<com.yomusensei.data.model.Example>::class.java).toList()
    } ?: emptyList()

    return DictionaryEntry(
        word = word,
        reading = reading,
        meanings = meaningsList,
        examples = examplesList,
        jlptLevel = jlptLevel,
        commonness = if (isCommon) 5 else 0
    )
}
