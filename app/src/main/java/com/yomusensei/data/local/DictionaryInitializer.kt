package com.yomusensei.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CoreDictionaryEntry(
    val word: String,
    val reading: String,
    val meanings: List<CoreMeaning>,
    val jlpt: String?,
    val freq: Int
)

data class CoreMeaning(
    val pos: String,
    val defs: List<String>
)

class DictionaryInitializer(
    private val context: Context,
    private val dictionaryDao: DictionaryDao
) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("dictionary_prefs", Context.MODE_PRIVATE)

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (prefs.getBoolean("dictionary_initialized", false)) {
            return@withContext
        }

        try {
            val json = context.assets.open("dictionary_core.json").bufferedReader().use { it.readText() }
            val entries = gson.fromJson(json, Array<CoreDictionaryEntry>::class.java)

            val dbWords = entries.map { entry ->
                DictionaryWord(
                    word = entry.word,
                    reading = entry.reading,
                    meanings = gson.toJson(entry.meanings.map {
                        mapOf("partOfSpeech" to it.pos, "definitions" to it.defs, "tags" to emptyList<String>())
                    }),
                    partOfSpeech = entry.meanings.firstOrNull()?.pos,
                    jlptLevel = entry.jlpt,
                    frequency = entry.freq,
                    isCommon = entry.freq <= 1000,
                    examples = null
                )
            }

            dictionaryDao.insertWords(dbWords)
            prefs.edit().putBoolean("dictionary_initialized", true).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
