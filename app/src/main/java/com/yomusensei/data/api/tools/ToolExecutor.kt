package com.yomusensei.data.api.tools

import com.yomusensei.data.api.JishoApiService
import com.yomusensei.data.api.TavilyApiService
import com.yomusensei.data.api.TavilySearchRequest
import com.yomusensei.data.model.FunctionCall
import com.yomusensei.data.model.FunctionResponse
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.vocabulary.VocabularyWord
import com.yomusensei.data.web.SyosetuScraper
import com.yomusensei.data.web.WebScraper
import com.yomusensei.data.web.WikipediaScraper

class ToolExecutor(
    private val webScraper: WebScraper,
    private val vocabularyRepository: VocabularyRepository,
    private val jishoService: JishoApiService,
    private val tavilyService: TavilyApiService,
    private val tavilyApiKey: String,
    private val syosetuScraper: SyosetuScraper,
    private val wikipediaScraper: WikipediaScraper
) {

    suspend fun execute(call: FunctionCall): FunctionResponse {
        val result: Map<String, Any> = try {
            when (call.name) {
                "fetch_webpage" -> executeFetchWebpage(call.args)
                "search_japanese_articles" -> executeSearchJapaneseArticles(call.args)
                "lookup_word" -> executeLookupWord(call.args)
                "save_vocabulary" -> executeSaveVocabulary(call.args)
                else -> mapOf("error" to "未知工具: ${call.name}")
            }
        } catch (e: Exception) {
            mapOf("error" to "工具执行失败: ${e.message}")
        }
        return FunctionResponse(name = call.name, response = result)
    }

    private suspend fun executeFetchWebpage(args: Map<String, Any>): Map<String, Any> {
        val url = args["url"]?.toString() ?: return mapOf("error" to "缺少 url 参数")

        // Validate domain whitelist
        if (!isUrlSupported(url)) {
            return mapOf("error" to "不支持的网站域名，请选择 NHK、朝日、每日、读卖、日经或青空文库的文章")
        }

        val result = webScraper.fetchArticle(url)
        return result.fold(
            onSuccess = { article ->
                mapOf(
                    "title" to article.title,
                    "content" to article.content.take(3000),
                    "url" to article.url
                )
            },
            onFailure = { e ->
                mapOf("error" to (e.message ?: "网页抓取失败"))
            }
        )
    }

    private fun isUrlSupported(url: String): Boolean {
        val supportedDomains = listOf(
            "nhk.or.jp",
            "asahi.com",
            "mainichi.jp",
            "yomiuri.co.jp",
            "nikkei.com",
            "aozora.gr.jp",
            "syosetu.com"
        )
        return supportedDomains.any { url.contains(it) }
    }

    private suspend fun executeSearchJapaneseArticles(args: Map<String, Any>): Map<String, Any> {
        val topic = args["topic"]?.toString()
        val query = args["query"]?.toString() ?: ""

        // 优先使用免费源，避免付费墙
        return try {
            when (topic) {
                "easy" -> {
                    // NHK Easy - 完全免费
                    val result = webScraper.fetchNhkEasyArticleList()
                    result.fold(
                        onSuccess = { articles ->
                            mapOf(
                                "articles" to articles.map { article ->
                                    mapOf(
                                        "title" to article.title,
                                        "url" to article.url,
                                        "summary" to article.description
                                    )
                                }
                            )
                        },
                        onFailure = { mapOf("error" to "NHK Easy 获取失败: ${it.message}") }
                    )
                }
                "literature" -> {
                    // 青空文库 - 根据关键词搜索
                    val query = args["query"]?.toString() ?: "宮沢賢治"
                    val result = webScraper.searchAozoraWorks(query, 5)
                    result.fold(
                        onSuccess = { articles ->
                            mapOf(
                                "articles" to articles.map { article ->
                                    mapOf(
                                        "title" to article.title,
                                        "url" to article.url,
                                        "summary" to article.description
                                    )
                                }
                            )
                        },
                        onFailure = { mapOf("error" to "青空文库搜索失败: ${it.message}") }
                    )
                }
                "novel" -> {
                    // Syosetu 网络小说
                    val result = syosetuScraper.fetchRankingNovels(10)
                    result.fold(
                        onSuccess = { articles ->
                            mapOf(
                                "articles" to articles.map { article ->
                                    mapOf(
                                        "title" to article.title,
                                        "url" to article.url,
                                        "summary" to article.description
                                    )
                                }
                            )
                        },
                        onFailure = { mapOf("error" to "Syosetu 获取失败: ${it.message}") }
                    )
                }
                else -> {
                    // 默认：维基百科搜索（最稳定）
                    val result = wikipediaScraper.searchArticles(query, 10)
                    if (result.isSuccess) {
                        val articles = result.getOrNull()!!
                        mapOf(
                            "articles" to articles.map { article ->
                                mapOf(
                                    "title" to article.title,
                                    "url" to article.url,
                                    "summary" to article.description
                                )
                            }
                        )
                    } else {
                        // 回退到 NHK Easy
                        val nhkResult = webScraper.fetchNhkEasyArticleList()
                        if (nhkResult.isSuccess) {
                            val articles = nhkResult.getOrNull()!!
                            mapOf(
                                "articles" to articles.map { article ->
                                    mapOf(
                                        "title" to article.title,
                                        "url" to article.url,
                                        "summary" to article.description
                                    )
                                }
                            )
                        } else {
                            searchViaTavily(query, listOf("nhk.or.jp"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            mapOf("error" to "搜索失败: ${e.message}")
        }
    }

    private suspend fun searchViaTavily(query: String, domains: List<String>): Map<String, Any> {
        return try {
            val response = tavilyService.search(
                TavilySearchRequest(
                    api_key = tavilyApiKey,
                    query = "$query 日本語",
                    max_results = 5,
                    include_domains = domains
                )
            )
            mapOf(
                "articles" to response.results.map { result ->
                    mapOf(
                        "title" to result.title,
                        "url" to result.url,
                        "summary" to result.content.take(200)
                    )
                }
            )
        } catch (e: Exception) {
            mapOf("error" to "Tavily 搜索失败: ${e.message}")
        }
    }

    private suspend fun executeLookupWord(args: Map<String, Any>): Map<String, Any> {
        val word = args["word"]?.toString() ?: return mapOf("error" to "缺少 word 参数")
        return try {
            val response = jishoService.search(word)
            val entry = response.data.firstOrNull()
            if (entry != null) {
                val japanese = entry.japanese.firstOrNull()
                val sense = entry.senses.firstOrNull()
                mapOf(
                    "word" to (japanese?.word ?: word),
                    "reading" to (japanese?.reading ?: ""),
                    "meanings" to (sense?.english_definitions?.joinToString(", ") ?: ""),
                    "parts_of_speech" to (sense?.parts_of_speech?.joinToString(", ") ?: "")
                )
            } else {
                mapOf("error" to "未找到「$word」的词典结果")
            }
        } catch (e: Exception) {
            mapOf("error" to "Jisho 查询失败: ${e.message}")
        }
    }

    private suspend fun executeSaveVocabulary(args: Map<String, Any>): Map<String, Any> {
        val word = args["word"]?.toString() ?: return mapOf("error" to "缺少 word 参数")
        val reading = args["reading"]?.toString() ?: ""
        val meaning = args["meaning"]?.toString() ?: ""

        val existing = vocabularyRepository.getWordByText(word)
        if (existing != null) {
            return mapOf("status" to "already_exists", "word" to word)
        }

        val vocabularyWord = VocabularyWord(
            word = word,
            reading = reading,
            meaning = meaning,
            explanation = "",
            partOfSpeech = null,
            category = null,
            sourceArticleTitle = null,
            sourceArticleUrl = null,
            addedTime = System.currentTimeMillis(),
            isManuallyAdded = false,
            nextReviewTime = System.currentTimeMillis()
        )
        vocabularyRepository.insertWord(vocabularyWord)
        return mapOf("status" to "saved", "word" to word)
    }
}
