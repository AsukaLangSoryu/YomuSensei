package com.yomusensei.data.web

import android.content.Context
import com.yomusensei.data.local.LocalArticleManager
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
    SYOSETU,
    GENERIC
}

class WebScraper(private val context: Context? = null) {

    private val localArticleManager: LocalArticleManager? = context?.let { LocalArticleManager(it) }

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
            url.contains("syosetu.com") || url.contains("ncode.syosetu.com") -> SiteType.SYOSETU
            else -> SiteType.GENERIC
        }
    }

    /**
     * 抓取网页并提取正文内容（优先从本地读取）
     */
    suspend fun fetchArticle(url: String, maxRetries: Int = 2): Result<ScrapedArticle> = withContext(Dispatchers.IO) {
        // 检查是否为青空文库文章，如果是则尝试从本地读取
        if (url.contains("aozora.gr.jp") && localArticleManager != null) {
            val match = Regex("cards/(\\d+)/card(\\d+)\\.html").find(url)
            if (match != null) {
                val authorId = match.groupValues[1]
                val workId = match.groupValues[2]

                val localResult = localArticleManager.readArticle(authorId, workId)
                if (localResult.isSuccess) {
                    val content = localResult.getOrNull()!!
                    val lines = content.lines()
                    val title = lines.firstOrNull { it.startsWith("# ") }?.removePrefix("# ") ?: "青空文庫"
                    val text = lines.drop(3).joinToString("\n")

                    return@withContext Result.success(ScrapedArticle(
                        title = title,
                        content = text,
                        url = url
                    ))
                }
            }
        }

        // 本地没有，从网络抓取
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                val doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get()

                val title = extractTitle(doc)
                val content = extractContent(doc, url)

                return@withContext when {
                    content.isBlank() -> {
                        Result.failure(Exception("无法解析文章内容，可能是付费内容或网站格式不支持"))
                    }
                    content.length < 100 -> {
                        Result.failure(Exception("内容过短（${content.length}字），可能遇到付费墙"))
                    }
                    detectPaywall(doc) -> {
                        Result.failure(Exception("检测到付费内容，请选择免费文章"))
                    }
                    isProductOrIndexPage(content) -> {
                        Result.failure(Exception("这是商品介绍页或目录页，无法阅读正文"))
                    }
                    else -> {
                        Result.success(ScrapedArticle(
                            title = title,
                            content = content,
                            url = url
                        ))
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                lastException = Exception("网络超时")
                if (attempt < maxRetries) {
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext Result.failure(Exception("无法访问该网站"))
            } catch (e: org.jsoup.HttpStatusException) {
                return@withContext Result.failure(Exception("HTTP ${e.statusCode} 错误"))
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }

        Result.failure(lastException ?: Exception("抓取失败"))
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
            SiteType.SYOSETU -> extractSyosetuContent(doc)
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
     * 读卖新闻专用提取
     */
    private fun extractYomiuriContent(doc: Document): String {
        return doc.select(".article-body").first()?.text()
            ?: doc.select(".p-main-contents").first()?.text()
            ?: doc.select(".article-main").first()?.text()
            ?: ""
    }

    /**
     * 每日新闻专用提取
     */
    private fun extractMainichiContent(doc: Document): String {
        return doc.select(".main-text").first()?.text()
            ?: doc.select("article .article-body").first()?.text()
            ?: doc.select(".article-main").first()?.text()
            ?: ""
    }

    /**
     * 日经新闻专用提取（含付费墙检测）
     */
    private fun extractNikkeiContent(doc: Document): String {
        // 检测付费墙
        if (doc.select(".paywall, .subscription-required, .cmn-pr_list").isNotEmpty()) {
            return ""
        }
        return doc.select(".article-body").first()?.text()
            ?: doc.select(".cmn-article_text").first()?.text()
            ?: doc.select(".article-main").first()?.text()
            ?: ""
    }

    /**
     * 产经新闻专用提取
     */
    private fun extractSankeiContent(doc: Document): String {
        return doc.select(".article-body").first()?.text()
            ?: doc.select(".post-content").first()?.text()
            ?: doc.select(".article-main").first()?.text()
            ?: ""
    }

    /**
     * 小説を読もう！(Syosetu) 专用提取
     */
    private fun extractSyosetuContent(doc: Document): String {
        return doc.select("#novel_honbun").first()?.text()
            ?: doc.select(".novel_view").first()?.text()
            ?: ""
    }

    /**
     * 通用提取策略（两阶段：选择器 + 智能算法）
     */
    private fun extractGenericContent(doc: Document): String {
        // 第一步：尝试扩展的选择器列表
        val content = tryExtractBySelectors(doc)
        if (content.isNotBlank() && content.length > 200) {
            return content
        }

        // 第二步：使用智能算法
        return extractByContentDensity(doc)
    }

    /**
     * 使用扩展的选择器列表提取内容
     */
    private fun tryExtractBySelectors(doc: Document): String {
        val selectors = listOf(
            // 文章容器
            "article", ".article", "#article",
            ".article-content", ".article-body", ".article-text",
            ".post-content", ".post-body", ".post-text",
            ".entry-content", ".entry-body",
            ".content", ".main-content", "#content",
            "main", "#main", ".main",
            // 日语网站常见
            ".honbun", ".body", ".text",
            ".kiji", ".article_body"
        )

        for (selector in selectors) {
            val element = doc.select(selector).first()
            if (element != null) {
                val text = element.text()
                if (text.length > 200) {
                    return text
                }
            }
        }

        // 如果都找不到，返回body中的段落集合
        val paragraphs = doc.select("p")
        val paragraphText = paragraphs.joinToString("\n\n") { it.text() }
        return if (paragraphText.length > 200) paragraphText else ""
    }

    /**
     * 智能算法：基于内容密度提取正文
     */
    private fun extractByContentDensity(doc: Document): String {
        val candidates = doc.select("div, article, section, main")
        var bestElement: org.jsoup.nodes.Element? = null
        var maxScore = 0.0

        for (element in candidates) {
            val textLength = element.ownText().length
            val linkLength = element.select("a").text().length
            val tagCount = element.children().size

            // 评分：文本长度高，链接少，标签少
            val score = textLength.toDouble() - linkLength * 0.5 - tagCount * 2.0
            if (score > maxScore && textLength > 100) {
                maxScore = score
                bestElement = element
            }
        }

        return bestElement?.text() ?: ""
    }

    /**
     * 检测是否为商品页或目录页
     */
    private fun isProductOrIndexPage(content: String): Boolean {
        val indicators = listOf(
            "ISBN", "出版社", "発行日", "定価", "購入", "カート",
            "在庫", "品切れ", "この本の内容", "目次", "著者略歴",
            "フォーマット", "ページ数"
        )
        val matchCount = indicators.count { content.contains(it) }
        return matchCount >= 3 && content.length < 1000
    }

    /**
     * 检测付费墙
     */
    private fun detectPaywall(doc: Document): Boolean {
        val paywallIndicators = listOf(
            ".paywall", ".subscription", ".premium-content",
            ".member-only", ".subscriber-only",
            ".有料会員", ".会員限定", ".有料記事"
        )
        return paywallIndicators.any { doc.select(it).isNotEmpty() }
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

    /**
     * 直接抓取 NHK Web Easy 最新文章列表（HTML + JSON 双通道）
     * 优先 HTML 抓取，失败则回退 JSON
     */
    suspend fun fetchNhkEasyArticleList(): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://www3.nhk.or.jp/news/easy/")
                .userAgent("Mozilla/5.0 (Android 14; Mobile) AppleWebKit/537.36")
                .timeout(TIMEOUT)
                .get()

            val articles = doc.select("article.news-list-item, .news-easy-list li, li[class*=news]")
                .take(10)
                .mapNotNull { element ->
                    val link = element.selectFirst("a") ?: return@mapNotNull null
                    val title = element.selectFirst("em, .title, h2, h3")?.text()
                        ?: link.text()
                    val path = link.attr("href")
                    val url = if (path.startsWith("http")) path
                              else "https://www3.nhk.or.jp$path"
                    if (title.isBlank() || url.isBlank()) null
                    else Article(
                        title = title,
                        url = url,
                        description = "NHK Web Easy - 简单日语新闻",
                        source = "NHK Web Easy"
                    )
                }

            if (articles.isEmpty()) {
                fetchNhkEasyViaJson()
            } else {
                Result.success(articles)
            }
        } catch (e: Exception) {
            // HTML scraping failed, try JSON
            fetchNhkEasyViaJson()
        }
    }

    private suspend fun fetchNhkEasyViaJson(): Result<List<Article>> = withContext(Dispatchers.IO) {
        // Reuse existing fetchNhkEasyArticles which reads from NHK_EASY_LIST_URL
        fetchNhkEasyArticles(10)
    }

    /**
     * 青空文庫搜索作品（使用预设索引）
     */
    suspend fun searchAozoraWorks(query: String, count: Int = 5): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val results = AozoraIndex.search(query)

            if (results.isEmpty()) {
                return@withContext fetchAozoraRandomWorks(count)
            }

            val articles = results.take(count).map { work ->
                Article(
                    title = "${work.author} - ${work.title}",
                    url = work.fileUrl,
                    description = "青空文庫 - ${work.author}",
                    source = "青空文庫"
                )
            }
            Result.success(articles)
        } catch (e: Exception) {
            fetchAozoraRandomWorks(count)
        }
    }

    /**
     * 青空文庫随机作品推荐（返回实际可阅读的文本链接）
     */
    suspend fun fetchAozoraRandomWorks(count: Int = 5): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://www.aozora.gr.jp/index_pages/person_all.html")
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .get()
            val cardLinks = doc.select("a[href*=cards]").shuffled().take(count * 2)

            val articles = mutableListOf<Article>()
            cardLinks.forEach { link ->
                if (articles.size >= count) return@forEach

                val title = link.text()
                if (title.isBlank()) return@forEach

                val cardUrl = link.attr("abs:href")

                // 访问作品页，提取实际文本链接
                val textUrl = extractAozoraTextUrl(cardUrl)
                if (textUrl != null) {
                    articles.add(Article(
                        title = title,
                        url = textUrl,
                        description = "青空文庫 - 日本文学",
                        source = "青空文庫"
                    ))
                }
            }
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun extractAozoraTextUrl(cardUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(cardUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .get()

            // 查找 HTML 格式的文本链接
            doc.select("a[href*=files]").firstOrNull { link ->
                val href = link.attr("href")
                href.endsWith(".html") && !href.contains("card")
            }?.attr("abs:href")
        } catch (e: Exception) {
            null
        }
    }
}

data class ScrapedArticle(
    val title: String,
    val content: String,
    val url: String
)
