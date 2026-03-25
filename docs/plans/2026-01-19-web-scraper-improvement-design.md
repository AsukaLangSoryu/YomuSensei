# 网页抓取改进设计方案

**日期**: 2026-01-19
**目标**: 改进 WebScraper，优化对更多日语网站的支持

## 需求概述

### 优先级
1. **日本主流新闻网站**（最高优先级）
2. 日语学习资源网站
3. 小说/文学网站

### 目标网站
- 朝日新闻 (asahi.com)
- 读卖新闻 (yomiuri.co.jp)
- 每日新闻 (mainichi.jp)
- 日本经济新闻 (nikkei.com)
- 产经新闻 (sankei.com)

### 约束条件
- 只处理免费内容，遇到付费墙返回错误
- 保持现有代码结构，渐进式改进
- 不破坏现有功能（NHK Easy、青空文库）

## 技术方案

### 方案选择
**方案A：渐进式改进**（已选择）
- 在现有 WebScraper 基础上扩展
- 添加专用提取方法
- 改进通用策略
- 优点：改动小，风险低，易于测试

## 详细设计

### 1. 整体架构

```kotlin
class WebScraper {
    // 现有方法保持不变
    suspend fun fetchArticle(url: String): Result<ScrapedArticle>
    suspend fun fetchNhkEasyArticles(limit: Int): Result<List<Article>>

    // 新增：网站类型检测
    private fun detectSiteType(url: String): SiteType

    // 改进：内容提取路由
    private fun extractContent(doc: Document, url: String): String {
        return when (detectSiteType(url)) {
            SiteType.NHK_EASY -> extractNhkEasyContent(doc)
            SiteType.ASAHI -> extractAsahiContent(doc)
            SiteType.YOMIURI -> extractYomiuriContent(doc)
            SiteType.MAINICHI -> extractMainichiContent(doc)
            SiteType.NIKKEI -> extractNikkeiContent(doc)
            SiteType.SANKEI -> extractSankeiContent(doc)
            SiteType.AOZORA -> extractAozoraContent(doc)
            SiteType.GENERIC -> extractGenericContent(doc)
        }
    }
}

enum class SiteType {
    NHK_EASY, ASAHI, YOMIURI, MAINICHI, NIKKEI, SANKEI, AOZORA, GENERIC
}
```

### 2. 新闻网站专用提取

每个新闻网站都有独特的 HTML 结构，需要专用方法：

```kotlin
// 朝日新闻 (asahi.com)
private fun extractAsahiContent(doc: Document): String {
    return doc.select("article .article-body").first()?.text()
        ?: doc.select(".article_body").first()?.text()
        ?: doc.select("#main-article").first()?.text()
        ?: ""
}

// 读卖新闻 (yomiuri.co.jp)
private fun extractYomiuriContent(doc: Document): String {
    return doc.select(".article-body").first()?.text()
        ?: doc.select(".p-main-contents").first()?.text()
        ?: ""
}

// 每日新闻 (mainichi.jp)
private fun extractMainichiContent(doc: Document): String {
    return doc.select(".main-text").first()?.text()
        ?: doc.select("article .article-body").first()?.text()
        ?: ""
}

// 日经新闻 (nikkei.com) - 可能有付费墙
private fun extractNikkeiContent(doc: Document): String {
    // 检测付费墙
    if (doc.select(".paywall, .subscription-required").isNotEmpty()) {
        return "" // 返回空，触发错误
    }
    return doc.select(".article-body").first()?.text()
        ?: doc.select(".cmn-article_text").first()?.text()
        ?: ""
}

// 产经新闻 (sankei.com)
private fun extractSankeiContent(doc: Document): String {
    return doc.select(".article-body").first()?.text()
        ?: doc.select(".post-content").first()?.text()
        ?: ""
}
```

**注意**: 这些选择器是基于常见模式的初步设计，实际实现时需要测试验证。

### 3. 改进通用提取策略

采用**两阶段策略**：先尝试选择器，失败后使用智能算法。

```kotlin
private fun extractGenericContent(doc: Document): String {
    // 第一步：尝试扩展的选择器列表
    val content = tryExtractBySelectors(doc)
    if (content.isNotBlank() && content.length > 200) {
        return content
    }

    // 第二步：使用智能算法
    return extractByContentDensity(doc)
}

// 扩展的选择器列表
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
    return ""
}

// 智能算法：基于内容密度
private fun extractByContentDensity(doc: Document): String {
    // 找到文本密度最高的元素
    val candidates = doc.select("div, article, section")
    var bestElement: org.jsoup.nodes.Element? = null
    var maxScore = 0.0

    for (element in candidates) {
        val textLength = element.ownText().length
        val linkLength = element.select("a").text().length
        val tagCount = element.children().size

        // 评分：文本长度高，链接少，标签少
        val score = textLength.toDouble() - linkLength * 0.5 - tagCount * 2
        if (score > maxScore) {
            maxScore = score
            bestElement = element
        }
    }

    return bestElement?.text() ?: ""
}
```

### 4. 错误处理和用户反馈

提供清晰的错误信息，帮助用户理解失败原因。

```kotlin
suspend fun fetchArticle(url: String): Result<ScrapedArticle> = withContext(Dispatchers.IO) {
    try {
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT)
            .get()

        val title = extractTitle(doc)
        val content = extractContent(doc, url)

        // 改进的错误处理
        when {
            content.isBlank() -> {
                Result.failure(Exception("无法提取文章内容，可能是付费内容或网站结构不支持"))
            }
            content.length < 100 -> {
                Result.failure(Exception("提取的内容过短（${content.length}字），可能是付费墙或抓取失败"))
            }
            detectPaywall(doc) -> {
                Result.failure(Exception("检测到付费内容，请选择免费文章"))
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
        Result.failure(Exception("网络超时，请检查网络连接"))
    } catch (e: java.net.UnknownHostException) {
        Result.failure(Exception("无法访问该网站，请检查URL"))
    } catch (e: Exception) {
        Result.failure(Exception("抓取失败: ${e.message}"))
    }
}

// 检测付费墙
private fun detectPaywall(doc: Document): Boolean {
    val paywallIndicators = listOf(
        ".paywall", ".subscription", ".premium-content",
        ".member-only", ".subscriber-only",
        ".有料会員", ".会員限定"
    )
    return paywallIndicators.any { doc.select(it).isNotEmpty() }
}
```

## 测试策略

### 1. 单元测试
- 为每个提取方法编写测试
- 准备示例 HTML 片段
- 验证能正确提取标题和正文
- 验证付费墙检测

### 2. 真实网站测试
- 准备测试 URL 列表（每个网站 2-3 个免费文章）
- 验证抓取结果的完整性和准确性
- 记录失败案例，调整选择器

### 3. 回归测试
- 测试 NHK Easy News 仍然正常
- 测试青空文库仍然正常
- 测试通用网站的基本功能

## 实现顺序

1. 添加 `SiteType` 枚举和 `detectSiteType()` 方法
2. 实现5个新闻网站的专用提取方法
3. 改进 `extractGenericContent()` - 扩展选择器
4. 实现 `extractByContentDensity()` 智能算法
5. 完善错误处理和付费墙检测
6. 测试和调整选择器

## 预期效果

- 支持5个主流日本新闻网站的文章抓取
- 通用网站抓取成功率提升 30-50%
- 提供清晰的错误提示，改善用户体验
- 不影响现有功能的稳定性

## 后续扩展

完成主流新闻网站后，可以继续添加：
- 日语学习资源网站（Satori Reader、Matcha 等）
- 小说网站（小説家になろう、カクヨム 等）
- Wikipedia 日语版
