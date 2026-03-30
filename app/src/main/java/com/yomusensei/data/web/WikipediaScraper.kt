package com.yomusensei.data.web

import com.yomusensei.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 日语维基百科搜索和抓取
 */
class WikipediaScraper {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val TIMEOUT = 15000
        private const val SEARCH_API = "https://ja.wikipedia.org/w/api.php"
    }

    /**
     * 搜索日语维基百科文章
     */
    suspend fun searchArticles(query: String, limit: Int = 10): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val url = "$SEARCH_API?action=opensearch&search=$query&limit=$limit&format=json"
            val response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .ignoreContentType(true)
                .execute()
                .body()

            val articles = parseSearchResults(response)
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseSearchResults(json: String): List<Article> {
        // OpenSearch API 返回格式: ["query", ["title1", "title2"], ["desc1", "desc2"], ["url1", "url2"]]
        val regex = """"(https://ja\.wikipedia\.org/wiki/[^"]+)"""".toRegex()
        val urls = regex.findAll(json).map { it.groupValues[1] }.toList()

        val titleRegex = """"([^"]{2,}?)"""".toRegex()
        val titles = titleRegex.findAll(json).drop(1).map { it.groupValues[1] }.toList()

        return urls.zip(titles).map { (url, title) ->
            Article(
                title = title,
                url = url,
                description = "日本語版ウィキペディア",
                source = "Wikipedia"
            )
        }
    }
}
