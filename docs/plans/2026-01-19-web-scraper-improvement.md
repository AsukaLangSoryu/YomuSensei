# Web Scraper Improvement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add support for 5 major Japanese news websites and improve generic content extraction

**Architecture:** Extend existing WebScraper with site-specific extractors using enum-based routing, add intelligent fallback algorithm for generic sites, and improve error handling with paywall detection

**Tech Stack:** Kotlin, Jsoup, Coroutines

---

## Task 1: Add SiteType Enum and Detection Logic

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt:10-16`

**Step 1: Add SiteType enum after imports**

Add this enum after the imports and before the WebScraper class:

```kotlin
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
```

**Step 2: Add detectSiteType method**

Add this private method inside WebScraper class, after the companion object:

```kotlin
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
```

**Step 3: Update extractContent to use detectSiteType**

Replace the existing extractContent method (lines 60-72) with:

```kotlin
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
```

**Step 4: Verify code compiles**

The code won't compile yet because we haven't added the new extractor methods. This is expected.

**Step 5: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add SiteType enum and detection logic"
```

---

## Task 2: Add Asahi News Extractor

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after extractAozoraContent method)

**Step 1: Add extractAsahiContent method**

Add this method after the extractAozoraContent method:

```kotlin
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
```

**Step 2: Verify code compiles**

Still won't compile - need to add remaining extractors.

**Step 3: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add Asahi news content extractor"
```

---

## Task 3: Add Yomiuri News Extractor

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after extractAsahiContent method)

**Step 1: Add extractYomiuriContent method**

Add this method after the extractAsahiContent method:

```kotlin
/**
 * 读卖新闻专用提取
 */
private fun extractYomiuriContent(doc: Document): String {
    return doc.select(".article-body").first()?.text()
        ?: doc.select(".p-main-contents").first()?.text()
        ?: doc.select(".article-main").first()?.text()
        ?: ""
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add Yomiuri news content extractor"
```

---

## Task 4: Add Mainichi News Extractor

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after extractYomiuriContent method)

**Step 1: Add extractMainichiContent method**

Add this method after the extractYomiuriContent method:

```kotlin
/**
 * 每日新闻专用提取
 */
private fun extractMainichiContent(doc: Document): String {
    return doc.select(".main-text").first()?.text()
        ?: doc.select("article .article-body").first()?.text()
        ?: doc.select(".article-main").first()?.text()
        ?: ""
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add Mainichi news content extractor"
```

---

## Task 5: Add Nikkei News Extractor with Paywall Detection

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after extractMainichiContent method)

**Step 1: Add extractNikkeiContent method**

Add this method after the extractMainichiContent method:

```kotlin
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
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add Nikkei news content extractor with paywall detection"
```

---

## Task 6: Add Sankei News Extractor

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after extractNikkeiContent method)

**Step 1: Add extractSankeiContent method**

Add this method after the extractNikkeiContent method:

```kotlin
/**
 * 产经新闻专用提取
 */
private fun extractSankeiContent(doc: Document): String {
    return doc.select(".article-body").first()?.text()
        ?: doc.select(".post-content").first()?.text()
        ?: doc.select(".article-main").first()?.text()
        ?: ""
}
```

**Step 2: Verify code compiles**

Run: Build the project or check syntax
Expected: Code should compile successfully now

**Step 3: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add Sankei news content extractor"
```

---

## Task 7: Improve Generic Content Extraction - Expand Selectors

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt:98-124`

**Step 1: Replace extractGenericContent method**

Replace the existing extractGenericContent method with this improved version:

```kotlin
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
```

**Step 2: Add tryExtractBySelectors method**

Add this method after extractGenericContent:

```kotlin
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
```

**Step 3: Verify code compiles**

Won't compile yet - need to add extractByContentDensity method.

**Step 4: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: expand generic content extraction selectors"
```

---

## Task 8: Add Intelligent Content Density Algorithm

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after tryExtractBySelectors method)

**Step 1: Add extractByContentDensity method**

Add this method after tryExtractBySelectors:

```kotlin
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
```

**Step 2: Verify code compiles**

Run: Build the project
Expected: Code should compile successfully

**Step 3: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add intelligent content density extraction algorithm"
```

---

## Task 9: Add Paywall Detection Helper

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt` (after extractByContentDensity method)

**Step 1: Add detectPaywall method**

Add this method after extractByContentDensity:

```kotlin
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
```

**Step 2: Verify code compiles**

Run: Build the project
Expected: Code should compile successfully

**Step 3: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: add paywall detection helper method"
```

---

## Task 10: Improve Error Handling in fetchArticle

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt:21-43`

**Step 1: Replace fetchArticle method**

Replace the existing fetchArticle method with this improved version:

```kotlin
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
    } catch (e: org.jsoup.HttpStatusException) {
        Result.failure(Exception("HTTP错误 ${e.statusCode}: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("抓取失败: ${e.message}"))
    }
}
```

**Step 2: Verify code compiles**

Run: Build the project
Expected: Code should compile successfully

**Step 3: Commit**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "feat: improve error handling with detailed messages"
```

---

## Task 11: Manual Testing - Prepare Test URLs

**Files:**
- Create: `docs/test-urls.md`

**Step 1: Create test URL document**

Create a file with test URLs for each news site:

```markdown
# Web Scraper Test URLs

## 朝日新闻 (Asahi)
- [ ] https://www.asahi.com/articles/[recent-free-article]
- [ ] https://www.asahi.com/articles/[another-free-article]

## 读卖新闻 (Yomiuri)
- [ ] https://www.yomiuri.co.jp/[recent-free-article]
- [ ] https://www.yomiuri.co.jp/[another-free-article]

## 每日新闻 (Mainichi)
- [ ] https://mainichi.jp/articles/[recent-free-article]
- [ ] https://mainichi.jp/articles/[another-free-article]

## 日经新闻 (Nikkei)
- [ ] https://www.nikkei.com/article/[recent-free-article]
- [ ] https://www.nikkei.com/article/[another-free-article]

## 产经新闻 (Sankei)
- [ ] https://www.sankei.com/article/[recent-free-article]
- [ ] https://www.sankei.com/article/[another-free-article]

## NHK Easy (Regression Test)
- [ ] https://www3.nhk.or.jp/news/easy/[recent-article]

## 青空文库 (Regression Test)
- [ ] https://www.aozora.gr.jp/cards/[some-work]

## Generic Sites
- [ ] https://ja.wikipedia.org/wiki/[some-article]
- [ ] https://[some-other-japanese-site]

## Test Results

### Asahi
- URL 1: [PASS/FAIL] - Notes:
- URL 2: [PASS/FAIL] - Notes:

### Yomiuri
- URL 1: [PASS/FAIL] - Notes:
- URL 2: [PASS/FAIL] - Notes:

### Mainichi
- URL 1: [PASS/FAIL] - Notes:
- URL 2: [PASS/FAIL] - Notes:

### Nikkei
- URL 1: [PASS/FAIL] - Notes:
- URL 2: [PASS/FAIL] - Notes:

### Sankei
- URL 1: [PASS/FAIL] - Notes:
- URL 2: [PASS/FAIL] - Notes:

### Regression Tests
- NHK Easy: [PASS/FAIL] - Notes:
- Aozora: [PASS/FAIL] - Notes:

### Generic Sites
- Wikipedia: [PASS/FAIL] - Notes:
- Other: [PASS/FAIL] - Notes:
```

**Step 2: Commit**

```bash
git add docs/test-urls.md
git commit -m "docs: add test URL template for manual testing"
```

---

## Task 12: Manual Testing - Test Each News Site

**Files:**
- Modify: `docs/test-urls.md`

**Step 1: Find real free article URLs**

Visit each news website and find 2 recent free articles. Update the test-urls.md file with actual URLs.

**Step 2: Test in the app**

For each URL:
1. Open the YomuSensei app
2. Enter the URL in the home screen
3. Verify the article loads correctly
4. Check that title and content are extracted properly
5. Record results in test-urls.md

**Step 3: Test regression**

Test NHK Easy and Aozora to ensure they still work correctly.

**Step 4: Test generic sites**

Test a few generic Japanese websites (Wikipedia, blogs, etc.) to verify the improved generic extraction.

**Step 5: Document failures**

For any failed tests, note:
- What went wrong (no content, wrong content, error message)
- What selectors might work better
- Whether it's a paywall issue

**Step 6: Commit test results**

```bash
git add docs/test-urls.md
git commit -m "test: document manual testing results"
```

---

## Task 13: Fix Selector Issues (If Any)

**Files:**
- Modify: `app/src/main/java/com/yomusensei/data/web/WebScraper.kt`

**Step 1: Review test failures**

Based on Task 12 results, identify which extractors need adjustment.

**Step 2: Update selectors**

For each failing extractor, add or modify selectors based on actual HTML structure observed during testing.

**Step 3: Re-test**

Test the updated extractors with the same URLs.

**Step 4: Commit fixes**

```bash
git add app/src/main/java/com/yomusensei/data/web/WebScraper.kt
git commit -m "fix: adjust selectors based on testing results"
```

---

## Task 14: Update CLAUDE.md Documentation

**Files:**
- Modify: `YomuSensei/CLAUDE.md`

**Step 1: Update current status**

Update the "当前状态" section to add:
```markdown
- ✅ 支持5个主流日本新闻网站（朝日、读卖、每日、日经、产经）
- ✅ 改进通用网站抓取（扩展选择器 + 智能算法）
- ✅ 付费墙检测和清晰的错误提示
```

**Step 2: Update known issues**

Remove or update the line about web scraping:
```markdown
- ~~改进网页抓取对更多网站的支持~~ 已完成
```

**Step 3: Update pending optimizations**

Remove the completed item:
```markdown
- ~~改进网页抓取对更多网站的支持~~ 已完成
```

**Step 4: Commit**

```bash
git add YomuSensei/CLAUDE.md
git commit -m "docs: update project status after web scraper improvements"
```

---

## Summary

This plan implements:
1. ✅ Support for 5 major Japanese news websites with site-specific extractors
2. ✅ Improved generic content extraction with expanded selectors
3. ✅ Intelligent content density algorithm as fallback
4. ✅ Paywall detection and clear error messages
5. ✅ Comprehensive manual testing process
6. ✅ Documentation updates

**Expected outcome:**
- 5 major news sites fully supported
- Generic site extraction success rate improved by 30-50%
- Better user experience with clear error messages
- No regression in existing functionality (NHK Easy, Aozora)
