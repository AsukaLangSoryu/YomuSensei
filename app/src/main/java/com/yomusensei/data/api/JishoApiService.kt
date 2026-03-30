package com.yomusensei.data.api

import android.util.LruCache
import com.yomusensei.data.model.DictionaryEntry
import com.yomusensei.data.model.Example
import com.yomusensei.data.model.Meaning
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class JishoResponse(val data: List<JishoEntry>)

data class JishoEntry(
    val japanese: List<JishoJapanese>,
    val senses: List<JishoSense>,
    val jlpt: List<String>? = null,
    val is_common: Boolean? = null
)

data class JishoJapanese(
    val word: String?,
    val reading: String?
)

data class JishoSense(
    val english_definitions: List<String>,
    val parts_of_speech: List<String>,
    val tags: List<String> = emptyList(),
    val sentences: List<JishoSentence>? = null
)

data class JishoSentence(
    val en: String?,
    val ja: String?
)

interface JishoApiService {

    @GET("api/v1/search/words")
    suspend fun search(@Query("keyword") keyword: String): JishoResponse

    companion object {
        private val cache = LruCache<String, DictionaryEntry>(100)

        fun create(): JishoApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://jisho.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JishoApiService::class.java)
        }

        suspend fun searchWithCache(service: JishoApiService, word: String): DictionaryEntry? {
            cache.get(word)?.let { return it }

            return try {
                val response = service.search(word)
                val entry = toDictionaryEntry(response)
                entry?.let { cache.put(word, it) }
                entry
            } catch (e: Exception) {
                null
            }
        }

        fun toDictionaryEntry(response: JishoResponse): DictionaryEntry? {
            val entry = response.data.firstOrNull() ?: return null
            val japanese = entry.japanese.firstOrNull() ?: return null

            val examples = entry.senses.flatMap { sense ->
                sense.sentences?.mapNotNull { sentence ->
                    if (sentence.ja != null && sentence.en != null) {
                        Example(
                            japanese = sentence.ja,
                            reading = "",
                            translation = sentence.en
                        )
                    } else null
                } ?: emptyList()
            }.take(3)

            return DictionaryEntry(
                word = japanese.word ?: japanese.reading ?: "",
                reading = japanese.reading ?: "",
                meanings = entry.senses.map { sense ->
                    Meaning(
                        partOfSpeech = sense.parts_of_speech.joinToString(", "),
                        definitions = sense.english_definitions,
                        tags = sense.tags
                    )
                },
                examples = examples,
                jlptLevel = entry.jlpt?.firstOrNull(),
                commonness = if (entry.is_common == true) 5 else 0
            )
        }
    }
}
