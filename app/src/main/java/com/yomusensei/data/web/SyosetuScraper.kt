package com.yomusensei.data.web

import com.yomusensei.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * 小説を読もう！(Syosetu) 网络小说抓取器
 */
class SyosetuScraper {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val TIMEOUT = 15000
        private const val RANKING_URL = "https://yomou.syosetu.com/rank/list/type/daily_total/"
    }

    /**
     * 获取日排行榜小说列表
     */
    suspend fun fetchRankingNovels(count: Int = 10): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(RANKING_URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .get()

            val articles = doc.select(".rank_list .rank_h").take(count).mapNotNull { element ->
                val link = element.selectFirst("a") ?: return@mapNotNull null
                val title = link.text().ifBlank { return@mapNotNull null }
                val href = link.attr("abs:href")

                Article(
                    title = title,
                    url = href,
                    description = "小説を読もう - 网络小说",
                    source = "Syosetu"
                )
            }
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
