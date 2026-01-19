package com.yomusensei.data.web

import com.yomusensei.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

enum class SiteType {
    NHK_EASY,
    ASAHI,
    YOMIURI,
    MAINICHI,
    NIKKEI,
    SANKEI,
    AOZORA,
    GENERIC
}

class WebScraper {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val TIMEOUT = 15000
        private const val NHK_EASY_LIST_URL = "https://www3.nhk.or.jp/news/easy/news-list.json"
    }

    /**
     * 检测网站类型
     */
    private fun detectSiteType(url: String): SiteType {
        return when {
            url.contains("nhk.or.jp/news/easy") -> SiteType.NHK_EASY
            url.contains("asahi.com") -> SiteType.ASAHI
            url.contains("yomiuri.co.jp") -> SiteType.YOMIURI
            url.contains("mainichi.jp") -> SiteType.MAINICHI
            url.contains("nikkei.com") -> SiteType.NIKKEI
            url.contains("sankei.com") -> SiteType.SANKEI
            url.contains("aozora.gr.jp") -> SiteType.AOZORA
            else -> SiteType.GENERIC
        }
    }

    /**
     * 抓取网页并提取正文内容
     */
    suspend fun fetchArticle(url: String): Result<ScrapedArticle> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .get()

            val title = extractTitle(doc)
            val content = extractContent(doc, url)

            if (content.isBlank()) {
                Result.failure(Exception("无法提取文章内容"))
            } else {
                Result.success(ScrapedArticle(
                    title = title,
                    content = content,
                    url = url
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 提取标题
     */
    private fun extractTitle(doc: Document): String {
        // 尝试多种方式获取标题
        return doc.select("article h1").firstOrNull()?.text()
            ?: doc.select("h1.title").firstOrNull()?.text()
            ?: doc.select("h1").firstOrNull()?.text()
            ?: doc.select(".article-title").firstOrNull()?.text()
            ?: doc.title()
    }

    /**
     * 提取正文内容
     */
    private fun extractContent(doc: Document, url: String): String {
        // 移除不需要的元素
        doc.select("script, style, nav, header, footer, aside, .ad, .advertisement, .sidebar, .comment, .comments").remove()

        // 根据网站类型使用不同的提取策略
        val content = when (detectSiteType(url)) {
            SiteType.NHK_EASY -> extractNhkEasyContent(doc)
            SiteType.ASAHI -> extractAsahiContent(doc)
            SiteType.YOMIURI -> extractYomiuriContent(doc)
            SiteType.MAINICHI -> extractMainichiContent(doc)
            SiteType.NIKKEI -> extractNikkeiContent(doc)
            SiteType.SANKEI -> extractSankeiContent(doc)
            SiteType.AOZORA -> extractAozoraContent(doc)
            SiteType.GENERIC -> extractGenericContent(doc)
        }

        return cleanContent(content)
    }

    /**
     * NHK Easy News 专用提取
     */
    private fun extractNhkEasyContent(doc: Document): String {
        val article = doc.select("#js-article-body").first()
            ?: doc.select(".article-body").first()
            ?: doc.select("article").first()

        return article?.text() ?: ""
    }

    /**
     * 青空文库专用提取
     */
    private fun extractAozoraContent(doc: Document): String {
        val mainText = doc.select(".main_text").first()
            ?: doc.select("#contents").first()

        return mainText?.text() ?: ""
    }

    /**
     * 朝日新闻专用提取
     */
    private fun extractAsahiContent(doc: Document): String {
        return doc.select("article .article-body").first()?.text()
            ?: doc.select(".article_body").first()?.text()
            ?: doc.select("#main-article").first()?.text()
            ?: doc.select(".article-main").first()?.text()
            ?: ""
    }

    /**
     * 通用提取策略
     */
    private fun extractGenericContent(doc: Document): String {
        // 尝试常见的文章容器
        val selectors = listOf(
            "article",
            ".article-content",
            ".article-body",
            ".post-content",
            ".entry-content",
            ".content",
            "main",
            "#main"
        )

        for (selector in selectors) {
            val element = doc.select(selector).first()
            if (element != null) {
                val text = element.text()
                if (text.length > 100) {
                    return text
                }
            }
        }

        // 如果都找不到，返回body中最长的段落集合
        val paragraphs = doc.select("p")
        return paragraphs.joinToString("\n\n") { it.text() }
    }

    /**
     * 清理内容
     */
    private fun cleanContent(content: String): String {
        return content
            .replace(Regex("\\s+"), " ")  // 多个空白变一个
            .replace(Regex("。\\s*"), "。\n")  // 句号后换行
            .trim()
    }

    /**
     * 获取 NHK News Easy 最新文章列表
     */
    suspend fun fetchNhkEasyArticles(limit: Int = 10): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val json = Jsoup.connect(NHK_EASY_LIST_URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .ignoreContentType(true)
                .execute()
                .body()

            val articles = parseNhkJson(json, limit)
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 解析 NHK JSON 数据
     */
    private fun parseNhkJson(json: String, limit: Int): List<Article> {
        val articles = mutableListOf<Article>()
        // NHK JSON 格式: { "日期": [ {news_id, title, ...}, ... ], ... }
        val regex = """"news_id"\s*:\s*"([^"]+)".*?"title"\s*:\s*"([^"]+)"""".toRegex()

        regex.findAll(json).take(limit).forEach { match ->
            val newsId = match.groupValues[1]
            val title = match.groupValues[2]
                .replace("\\u003c", "<")
                .replace("\\u003e", ">")
                .replace(Regex("<[^>]+>"), "") // 移除HTML标签

            val url = "https://www3.nhk.or.jp/news/easy/$newsId/$newsId.html"
            articles.add(Article(
                title = title,
                url = url,
                description = "NHK简单日语新闻",
                source = "NHK News Easy"
            ))
        }
        return articles
    }
}

data class ScrapedArticle(
    val title: String,
    val content: String,
    val url: String
)
