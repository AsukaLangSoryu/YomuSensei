# YomuSensei 多平台 API + OpenClaw 升级实施方案

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 YomuSensei 的 AI 后端从 Gemini 单一依赖升级为多平台可配置（Gemini / OpenAI / DeepSeek / GLM / Kimi / OpenClaw），并改善文章搜索质量，补全词库复习 UI。

**Architecture:**
- 提取 `AiProvider` 接口，`GeminiProvider` 和 `OpenAICompatProvider` 分别实现。OpenClaw 通过 `OpenAICompatProvider` 接入（base URL 指向本地 18789 端口）。
- 文章搜索改为"NHK Easy RSS 直接抓取"优先，AI 生成描述为辅，彻底解决幻觉 URL 问题。
- 词库复习页面补全为完整的多选题界面。

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit, OkHttp, Room, DataStore, Jsoup

---

## 任务分工（3个并行 Agent）

本方案设计为 3 个独立任务，可由 3 个 Agent 在各自 git worktree 中并行执行：

| Agent | 任务 | 主要涉及文件 |
|---|---|---|
| **Agent-A** | AI Provider 抽象层 + 多平台设置 | `data/api/`, `ui/settings/`, ViewModels |
| **Agent-B** | 文章搜索质量改进 + OpenClaw Skill | `data/web/WebScraper.kt`, `ui/home/HomeViewModel.kt`, `openclaw-skill/` |
| **Agent-C** | 词库复习 UI 补全 | `ui/vocabulary/` |

合并时唯一潜在冲突点：`HomeViewModel.kt`（Agent-A 换 provider，Agent-B 改文章来源），合并时按逻辑整合即可。

---

## Agent-A 任务：AI Provider 抽象层 + 多平台设置

### A-1: 创建 AiProvider 接口

**创建文件：** `app/src/main/java/com/yomusensei/data/api/AiProvider.kt`

```kotlin
package com.yomusensei.data.api

import com.yomusensei.data.model.ArticleSearchResult

interface AiProvider {
    suspend fun chat(userMessage: String, systemPrompt: String? = null): Result<String>
    suspend fun chatWithHistory(history: List<Pair<String, Boolean>>, systemPrompt: String): Result<String>
    suspend fun detectIntent(userMessage: String): UserIntent
    suspend fun requestArticles(userRequest: String): Result<ArticleSearchResult>
    suspend fun explainText(text: String, context: String = ""): Result<String>
    suspend fun askQuestion(question: String, articleContext: String = ""): Result<String>
}
```

**注意：** `ArticleSearchResult` 已存在于 `GeminiRepository.kt` 中，将其移到 `data/model/Models.kt` 作为顶级数据类。

### A-2: 将 GeminiRepository 重构为 GeminiProvider

**创建文件：** `app/src/main/java/com/yomusensei/data/api/GeminiProvider.kt`

- 复制 `GeminiRepository.kt` 的全部逻辑
- 改名为 `GeminiProvider`，实现 `AiProvider` 接口
- 保留 `GeminiRepository.kt` 作为类型别名或删除（ViewModels 目前直接引用它，在 A-4 步骤中统一替换）

### A-3: 创建 OpenAICompatProvider

**创建文件：** `app/src/main/java/com/yomusensei/data/api/OpenAICompatProvider.kt`

```kotlin
package com.yomusensei.data.api

import com.yomusensei.data.model.ArticleSearchResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// OpenAI-compatible request/response models
data class OpenAIMessage(val role: String, val content: String)
data class OpenAIRequest(val model: String, val messages: List<OpenAIMessage>)
data class OpenAIChoice(val message: OpenAIMessage)
data class OpenAIResponse(val choices: List<OpenAIChoice>?, val error: OpenAIError?)
data class OpenAIError(val message: String)

interface OpenAIApiService {
    @POST
    suspend fun chat(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

class OpenAICompatProvider(
    private val apiKey: String,
    private val baseUrl: String,   // e.g. "https://api.deepseek.com/v1" 或 "http://192.168.1.100:18789"
    private val modelName: String  // e.g. "deepseek-chat" 或 "openclaw"
) : AiProvider {

    private val service: OpenAIApiService = Retrofit.Builder()
        .baseUrl("https://placeholder.com/")  // 实际 URL 在请求时传入
        .client(OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAIApiService::class.java)

    private val chatEndpoint get() = "$baseUrl/chat/completions"

    private suspend fun rawChat(messages: List<OpenAIMessage>): Result<String> {
        return try {
            val response = service.chat(
                url = chatEndpoint,
                auth = "Bearer $apiKey",
                request = OpenAIRequest(model = modelName, messages = messages)
            )
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                Result.success(response.choices?.firstOrNull()?.message?.content ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun chat(userMessage: String, systemPrompt: String?): Result<String> {
        val messages = buildList {
            if (!systemPrompt.isNullOrBlank()) add(OpenAIMessage("system", systemPrompt))
            add(OpenAIMessage("user", userMessage))
        }
        return rawChat(messages)
    }

    override suspend fun chatWithHistory(
        history: List<Pair<String, Boolean>>,
        systemPrompt: String
    ): Result<String> {
        val messages = buildList {
            add(OpenAIMessage("system", systemPrompt))
            history.forEach { (msg, isUser) ->
                add(OpenAIMessage(if (isUser) "user" else "assistant", msg))
            }
        }
        return rawChat(messages)
    }

    override suspend fun detectIntent(userMessage: String): UserIntent {
        val prompt = """
分析用户消息的意图，只返回一个数字：
1 = 想要推荐日语文章/新闻/阅读材料
2 = 日语学习问题（语法、词汇、发音等）
3 = 普通闲聊或其他

用户消息：$userMessage

只返回数字1、2或3，不要其他内容。
""".trimIndent()
        val result = chat(prompt)
        val text = result.getOrDefault("3").trim()
        return when {
            text.contains("1") -> UserIntent.ARTICLE_REQUEST
            text.contains("2") -> UserIntent.JAPANESE_QUESTION
            else -> UserIntent.GENERAL_CHAT
        }
    }

    override suspend fun requestArticles(userRequest: String): Result<ArticleSearchResult> {
        // OpenAI-compatible providers 没有 Google Search Grounding
        // 改为生成搜索建议 + 依赖 Agent-B 提供的 NHK RSS 真实文章
        val prompt = """
你是一个日语阅读助手。用户需求：$userRequest

请用以下JSON格式返回3篇推荐文章（使用你知道的真实日语网站）：
{
  "message": "根据你的需求，推荐以下文章：",
  "articles": [
    {
      "title": "文章标题",
      "url": "https://...",
      "description": "内容描述",
      "source": "来源"
    }
  ]
}
优先推荐NHK Web Easy、青空文庫。只返回JSON。
""".trimIndent()
        val result = chat(prompt)
        return result.map { text -> ArticleSearchResult(text, null) }
    }

    override suspend fun explainText(text: String, context: String): Result<String> {
        val systemPrompt = "你是专业日语教师，请解释日语文本的读音、词性、释义和例句。格式简洁清晰。"
        val userMessage = if (context.isNotBlank()) "请解释：「$text」\n上下文：$context" else "请解释：「$text」"
        return chat(userMessage, systemPrompt)
    }

    override suspend fun askQuestion(question: String, articleContext: String): Result<String> {
        val systemPrompt = "你是耐心的日语老师，用通俗易懂的方式解答日语学习问题。"
        val userMessage = if (articleContext.isNotBlank()) "文章内容：\n$articleContext\n\n问题：$question" else question
        return chat(userMessage, systemPrompt)
    }
}
```

### A-4: 扩展 SettingsRepository 支持多 Provider

**修改文件：** `app/src/main/java/com/yomusensei/data/api/SettingsRepository.kt`

新增以下 DataStore key 和方法：

```kotlin
// 新增 key
private val PROVIDER_TYPE = stringPreferencesKey("provider_type")           // "GEMINI" | "OPENAI_COMPAT"
private val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")         // 原 API_KEY 改名
private val OPENAI_COMPAT_API_KEY = stringPreferencesKey("openai_compat_api_key")
private val OPENAI_COMPAT_BASE_URL = stringPreferencesKey("openai_compat_base_url")
private val OPENAI_COMPAT_MODEL = stringPreferencesKey("openai_compat_model")

// Provider 类型
enum class ProviderType { GEMINI, OPENAI_COMPAT }

// 预设 base URL 列表（用于设置页面下拉）
val PRESET_BASE_URLS = mapOf(
    "OpenAI" to "https://api.openai.com/v1",
    "DeepSeek" to "https://api.deepseek.com/v1",
    "智谱 GLM" to "https://open.bigmodel.cn/api/paas/v4",
    "Kimi" to "https://api.moonshot.cn/v1",
    "OpenClaw (本地)" to "http://192.168.x.x:18789"  // 用户需替换 IP
)

// 新增方法
suspend fun getProviderType(): ProviderType
suspend fun setProviderType(type: ProviderType)
suspend fun getGeminiApiKey(): String  // 原 getApiKey()
suspend fun setGeminiApiKey(key: String)
suspend fun getOpenAICompatApiKey(): String
suspend fun setOpenAICompatApiKey(key: String)
suspend fun getOpenAICompatBaseUrl(): String  // 默认 "https://api.openai.com/v1"
suspend fun setOpenAICompatBaseUrl(url: String)
suspend fun getOpenAICompatModel(): String    // 默认 "gpt-4o-mini"
suspend fun setOpenAICompatModel(model: String)

// 工厂方法 - 根据当前设置构造对应 AiProvider
suspend fun buildAiProvider(): AiProvider {
    return when (getProviderType()) {
        ProviderType.GEMINI -> GeminiProvider(this)
        ProviderType.OPENAI_COMPAT -> OpenAICompatProvider(
            apiKey = getOpenAICompatApiKey(),
            baseUrl = getOpenAICompatBaseUrl(),
            modelName = getOpenAICompatModel()
        )
    }
}
```

**同时保留原 `getApiKey()` / `setApiKey()` 作为 `getGeminiApiKey()` 的别名，避免 SettingsViewModel 报错。**

### A-5: 更新 SettingsScreen 和 SettingsViewModel

**修改文件：** `app/src/main/java/com/yomusensei/ui/settings/SettingsViewModel.kt`

- 添加 `providerType: StateFlow<ProviderType>`
- 添加 `openaiCompatApiKey`, `openaiCompatBaseUrl`, `openaiCompatModel` 状态
- 添加 `setProviderType()`, `setOpenAICompatApiKey()` 等方法

**修改文件：** `app/src/main/java/com/yomusensei/ui/settings/SettingsScreen.kt`

在 Gemini API Key 输入框上方添加：
1. **Provider 选择器**（两个选项卡或 DropdownMenu）：Gemini / 兼容 OpenAI
2. **Gemini 区域**（仅在选 Gemini 时显示）：API Key 输入
3. **OpenAI 兼容区域**（仅在选该模式时显示）：
   - Base URL（带预设下拉：OpenAI / DeepSeek / GLM / Kimi / OpenClaw）
   - API Key 输入
   - 模型名称输入（带提示：deepseek-chat / glm-4-flash / moonshot-v1-8k / gpt-4o-mini / openclaw）
4. **OpenClaw 专属提示**（当 Base URL 含 "18789" 或用户选 OpenClaw 预设时显示）：
   > "OpenClaw 模式：请确保电脑上的 OpenClaw 正在运行，且手机和电脑处于同一 WiFi 网络。"

### A-6: 更新 ViewModels 使用 AiProvider 接口

**修改文件：** `app/src/main/java/com/yomusensei/ui/home/HomeViewModel.kt`

- 构造函数接收 `AiProvider`（而非 `GeminiRepository`）
- 删除对 `GeminiRepository` 的直接引用

**修改文件：** `app/src/main/java/com/yomusensei/ui/reader/ReaderViewModel.kt`

- 同上，改为接收 `AiProvider`

**修改文件：** `app/src/main/java/com/yomusensei/MainActivity.kt`

```kotlin
// 在 onCreate 中
val settingsRepo = SettingsRepository(this)
val aiProvider = runBlocking { settingsRepo.buildAiProvider() }

// 将 aiProvider 传给 ViewModelFactory
```

**注意：** 每次用户在设置中切换 Provider 后，需要重建 `AiProvider` 实例。可通过在 `MainActivity` 监听 `settingsRepo.getProviderTypeFlow()` 来实现热切换，或简单地让用户重启 App 生效（更简单，课设可接受）。

---

## Agent-B 任务：文章搜索质量 + OpenClaw Skill 文件

### B-1: NHK Web Easy 文章列表直接抓取

**修改文件：** `app/src/main/java/com/yomusensei/data/web/WebScraper.kt`

在现有代码中新增方法 `fetchNhkEasyArticleList()`：

```kotlin
/**
 * 直接抓取 NHK Web Easy 最新文章列表（不依赖 AI）
 * 返回最近 10 篇文章
 */
suspend fun fetchNhkEasyArticleList(): Result<List<Article>> = withContext(Dispatchers.IO) {
    try {
        // NHK Web Easy 主页列出最近新闻
        val doc = Jsoup.connect("https://www3.nhk.or.jp/news/easy/")
            .userAgent("Mozilla/5.0 (Android 14; Mobile) AppleWebKit/537.36")
            .timeout(15000)
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
            // 备用：尝试 JSON API
            fetchNhkEasyViaJson()
        } else {
            Result.success(articles)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 备用方案：通过 NHK Easy JSON 接口获取文章列表
 */
private suspend fun fetchNhkEasyViaJson(): Result<List<Article>> {
    return try {
        val response = OkHttpClient().newCall(
            okhttp3.Request.Builder()
                .url("https://www3.nhk.or.jp/news/easy/top-list-items.json")
                .header("Referer", "https://www3.nhk.or.jp/news/easy/")
                .build()
        ).execute()
        val json = response.body?.string() ?: return Result.success(emptyList())
        // 解析 JSON，提取 news_id 和标题
        // NHK JSON 结构：[{"news_id":"k10001234","title":"..."}]
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
        val items: List<Map<String, String>> = gson.fromJson(json, type)
        val articles = items.take(10).mapNotNull { item ->
            val id = item["news_id"] ?: return@mapNotNull null
            val title = item["title"] ?: return@mapNotNull null
            Article(
                title = title,
                url = "https://www3.nhk.or.jp/news/easy/$id/$id.html",
                description = "NHK Web Easy - 简单日语新闻",
                source = "NHK Web Easy"
            )
        }
        Result.success(articles)
    } catch (e: Exception) {
        Result.success(emptyList())
    }
}
```

### B-2: 更新 HomeViewModel 优先使用真实文章列表

**修改文件：** `app/src/main/java/com/yomusensei/ui/home/HomeViewModel.kt`

在 `handleArticleRequest()` 方法中：

1. **先尝试** 调用 `webScraper.fetchNhkEasyArticleList()`
2. 如果成功获取到 ≥ 3 篇文章，直接展示（跳过 AI 搜索）
3. 如果失败或结果为空，回退到 AI 搜索
4. UI 上用一个 tag 区分：真实文章列表 vs AI 推荐

```kotlin
// 在 HomeViewModel 中注入 WebScraper（目前可能未注入，需要在 MainActivity 传入）
private val webScraper = WebScraper()

private suspend fun handleArticleRequest(userRequest: String) {
    // 1. 先尝试直接抓取
    if (userRequest.contains("nhk") || userRequest.contains("新闻") || userRequest.isEmpty()) {
        val scraped = webScraper.fetchNhkEasyArticleList()
        if (scraped.isSuccess && scraped.getOrNull()!!.isNotEmpty()) {
            // 展示真实文章，用 AI 生成一句推荐语
            val articles = scraped.getOrNull()!!
            val intro = aiProvider.chat("用一句话说：已为你找到最新NHK简单日语新闻${articles.size}篇。").getOrDefault("已为你找到最新NHK简单日语新闻。")
            addBotMessage(intro, articles)
            return
        }
    }
    // 2. 回退到 AI 搜索
    val result = aiProvider.requestArticles(userRequest)
    // ... 现有逻辑
}
```

**注意：** 如果 `HomeViewModel` 目前没有接收 `WebScraper`，在 A-6 步骤调整 `MainActivity` 时一并处理。

### B-3: 青空文庫随机推荐（可选，若时间允许）

**修改文件：** `app/src/main/java/com/yomusensei/data/web/WebScraper.kt`

```kotlin
/**
 * 青空文庫随机推荐 - 从作品列表中随机抽取
 */
suspend fun fetchAozoraRandomWorks(count: Int = 5): Result<List<Article>> = withContext(Dispatchers.IO) {
    try {
        val doc = Jsoup.connect("https://www.aozora.gr.jp/index_pages/person_all.html")
            .timeout(15000).get()
        // 从作品链接中随机抽取
        val links = doc.select("a[href*=cards]").shuffled().take(count)
        val articles = links.mapNotNull { link ->
            val title = link.text().ifBlank { null } ?: return@mapNotNull null
            val href = link.attr("abs:href")
            Article(title = title, url = href, description = "青空文庫 - 日本文学", source = "青空文庫")
        }
        Result.success(articles)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### B-4: 创建 OpenClaw YomuSensei Skill 文件

**创建目录和文件：** `openclaw-skill/SKILL.md`

```markdown
---
name: yomusensei
description: 日语阅读助手技能 - 帮助日语学习者搜索文章、解释词汇和语法
version: 1.0.0
author: YomuSensei
---

# YomuSensei 日语阅读助手

你是一个专业的日语阅读学习助手，帮助用户：
1. 找到适合其水平的日语阅读材料
2. 解释日语词汇、语法和句子结构
3. 推荐学习策略

## 文章搜索

当用户请求日语文章推荐时：
1. 使用浏览器工具打开 https://www3.nhk.or.jp/news/easy/
2. 提取当前页面上的文章标题和 URL 列表
3. 根据用户需求（难度、主题）筛选并推荐 3-5 篇
4. 以 JSON 格式返回（title, url, description, source 字段）

## 词汇解释

解释格式：
- 读音（平假名）
- 词性
- 中文释义
- 例句
- 记忆技巧（可选）

## 与 YomuSensei App 集成

YomuSensei Android 应用会通过 OpenClaw 的 HTTP API 调用你。
请求格式遵循 OpenAI Chat Completions 格式。
应用配置：OpenClaw 本地地址 http://127.0.0.1:18789

## 注意事项

- 用户日语水平为 N5-N4 初级
- 优先推荐 NHK Web Easy（最适合初学者）和青空文庫（文学作品）
- 解释要简洁，不要过于学术化
- 始终用中文解释，日语原文保留
```

**同时创建：** `openclaw-skill/README.md`

```markdown
# YomuSensei OpenClaw Skill

将此目录放入 OpenClaw 的 skills 文件夹即可使用。

## 安装方法

1. 找到你的 OpenClaw skills 目录（通常在 `~/.openclaw/skills/` 或 OpenClaw 安装目录下的 `skills/`）
2. 将 `yomusensei` 文件夹复制进去
3. 重启 OpenClaw

## 在 YomuSensei App 中配置

1. 打开 YomuSensei → 设置
2. 选择 AI 提供商：**兼容 OpenAI**
3. Base URL：`http://你的电脑IP:18789`
4. API Key：OpenClaw 的 Bearer token（在 OpenClaw 设置中查看）
5. 模型名称：`openclaw` 或 `openclaw:main`

## 使用要求

- 手机和电脑处于同一 WiFi 网络
- OpenClaw 服务正在运行
- 已在 OpenClaw config 中开启 HTTP endpoint：
  ```json
  { "gateway": { "http": { "endpoints": { "responses": { "enabled": true } } } } }
  ```
```

---

## Agent-C 任务：词库复习 UI 补全

### C-1: 创建复习主屏幕 ReviewScreen

**创建文件：** `app/src/main/java/com/yomusensei/ui/vocabulary/ReviewScreen.kt`

这是一个多选题界面，展示单词的中文释义，让用户从 4 个选项中选出正确的日语写法（或反向：展示日语，选中文）。

```kotlin
package com.yomusensei.ui.vocabulary

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yomusensei.data.model.ReviewQuestion
import com.yomusensei.data.model.ReviewResult

@Composable
fun ReviewScreen(
    viewModel: VocabularyViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.reviewQuestions
    val currentIndex = uiState.currentReviewIndex
    val results = uiState.reviewResults

    if (questions.isEmpty()) {
        // 没有待复习单词
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无待复习单词 🎉", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onFinish) { Text("返回词库") }
            }
        }
        return
    }

    if (currentIndex >= questions.size) {
        // 复习完成 - 显示结果
        ReviewResultScreen(results = results, onDone = {
            viewModel.resetReview()
            onFinish()
        })
        return
    }

    val question = questions[currentIndex]
    var selectedIndex by remember(currentIndex) { mutableStateOf(-1) }
    var showAnswer by remember(currentIndex) { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 进度条
        LinearProgressIndicator(
            progress = { currentIndex.toFloat() / questions.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "$currentIndex / ${questions.size}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )

        // 题目
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("下面哪个是", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    question.question,  // 中文释义
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text("的日语写法？", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // 4个选项
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                val isCorrect = index == question.correctIndex
                val isSelected = index == selectedIndex
                val buttonColors = when {
                    !showAnswer -> ButtonDefaults.buttonColors()
                    isCorrect -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    isSelected && !isCorrect -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                    else -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Button(
                    onClick = {
                        if (!showAnswer) {
                            selectedIndex = index
                            showAnswer = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = buttonColors
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // 下一题按钮
        if (showAnswer) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val isCorrect = selectedIndex == question.correctIndex
                    viewModel.submitReviewAnswer(question.word, isCorrect)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (currentIndex < questions.size - 1) "下一题" else "查看结果")
            }
        }
    }
}

@Composable
fun ReviewResultScreen(results: List<ReviewResult>, onDone: () -> Unit) {
    val correct = results.count { it.isCorrect }
    val total = results.size
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("复习完成！", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text("$correct / $total", style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("答对", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        val pct = if (total > 0) correct * 100 / total else 0
        Text(
            when {
                pct >= 80 -> "太棒了！继续保持 🎊"
                pct >= 60 -> "不错，再接再厉！"
                else -> "还需多加练习，加油！"
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("返回词库") }
    }
}
```

### C-2: 更新 VocabularyViewModel 支持复习流程

**修改文件：** `app/src/main/java/com/yomusensei/ui/vocabulary/VocabularyViewModel.kt`

在 `VocabularyUiState` 中新增：
```kotlin
val reviewQuestions: List<ReviewQuestion> = emptyList(),
val currentReviewIndex: Int = 0,
val reviewResults: List<ReviewResult> = emptyList(),
val isReviewLoading: Boolean = false
```

新增方法：
```kotlin
/**
 * 开始复习 - 从数据库加载待复习单词并生成题目
 */
fun startReview() {
    viewModelScope.launch {
        _uiState.update { it.copy(isReviewLoading = true) }
        val wordsToReview = vocabularyRepository.getWordsForReview()
        val questions = wordsToReview.mapNotNull { word ->
            vocabularyRepository.generateReviewQuestion(word)
        }.shuffled()
        _uiState.update { it.copy(
            reviewQuestions = questions,
            currentReviewIndex = 0,
            reviewResults = emptyList(),
            isReviewLoading = false
        )}
    }
}

/**
 * 提交答题结果并更新复习进度
 */
fun submitReviewAnswer(word: VocabularyWord, isCorrect: Boolean) {
    viewModelScope.launch {
        vocabularyRepository.updateReviewResult(word, isCorrect)
        _uiState.update { state ->
            state.copy(
                currentReviewIndex = state.currentReviewIndex + 1,
                reviewResults = state.reviewResults + ReviewResult(word.word, isCorrect)
            )
        }
    }
}

fun resetReview() {
    _uiState.update { it.copy(reviewQuestions = emptyList(), currentReviewIndex = 0, reviewResults = emptyList()) }
}
```

**同时检查 `VocabularyRepository`** 是否已有 `getWordsForReview()`、`generateReviewQuestion()`、`updateReviewResult()` 方法。如果没有，根据 `ReviewScheduler.kt` 的逻辑补全：

```kotlin
// VocabularyRepository.kt 中新增（如果缺失）
suspend fun getWordsForReview(): List<VocabularyWord> {
    return dao.getWordsForReview(System.currentTimeMillis())
}

suspend fun updateReviewResult(word: VocabularyWord, isCorrect: Boolean) {
    val newLevel = if (isCorrect) (word.reviewLevel + 1).coerceAtMost(5)
                   else maxOf(0, word.reviewLevel - 1)
    val nextReviewTime = ReviewScheduler.calculateNextReviewTime(newLevel)
    dao.updateWordReviewStatus(word.id, newLevel, nextReviewTime, isCorrect)
}
```

### C-3: 更新 ReviewModeTab 作为入口

**修改文件：** `app/src/main/java/com/yomusensei/ui/vocabulary/ReviewModeTab.kt`

- 显示待复习数量（从 `uiState.stats.pendingReview` 获取）
- "开始复习"按钮调用 `viewModel.startReview()` 并导航到 `ReviewScreen`
- 复习完成后返回此页面

### C-4: 添加 ReviewScreen 到导航

**修改文件：** `app/src/main/java/com/yomusensei/ui/Navigation.kt`

新增路由：
```kotlin
composable("review") {
    ReviewScreen(
        viewModel = vocabularyViewModel,
        onFinish = { navController.popBackStack() }
    )
}
```

在 `ReviewModeTab` 的"开始复习"按钮中：
```kotlin
navController.navigate("review")
```

### C-5: 创建单词详情对话框

**创建文件：** `app/src/main/java/com/yomusensei/ui/vocabulary/WordDetailDialog.kt`

```kotlin
@Composable
fun WordDetailDialog(
    word: VocabularyWord,
    onDismiss: () -> Unit,
    onDelete: (VocabularyWord) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(word.word, style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (word.reading.isNotBlank()) {
                    Text("读音：${word.reading}", style = MaterialTheme.typography.bodyLarge)
                }
                Text("释义：${word.meaning}", style = MaterialTheme.typography.bodyMedium)
                if (word.partOfSpeech.isNotBlank()) {
                    Text("词性：${word.partOfSpeech}", style = MaterialTheme.typography.bodySmall)
                }
                Divider()
                Text("复习级别：${word.reviewLevel} / 5", style = MaterialTheme.typography.bodySmall)
                Text("加入时间：${formatDate(word.createdAt)}", style = MaterialTheme.typography.bodySmall)
                if (word.sourceArticleUrl.isNotBlank()) {
                    Text("来源：${word.sourceArticleUrl}", style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        dismissButton = {
            TextButton(onClick = { onDelete(word); onDismiss() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("删除") }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}
```

在 `WordListTab.kt` 中，单词卡片点击时显示此对话框。

---

## 合并指南

三个 Agent 完成后，在主分支执行：

```bash
# 先合并 Agent-A（改动最广）
git merge feature/agent-a-api-layer

# 合并 Agent-C（独立，无冲突）
git merge feature/agent-c-review-ui

# 合并 Agent-B（注意 HomeViewModel 冲突）
git merge feature/agent-b-article-search
# 如有冲突：手动整合 HomeViewModel - 保留两边的改动
# Agent-A 改了 provider 引用，Agent-B 加了 scraper 调用，都需要保留

# 最终构建验证
./gradlew assembleDebug
```

---

## 课设报告提示

本项目答辩时可重点强调以下创新点（对应成绩单"概念正确有创新"档次）：

1. **OpenClaw 集成**：将 2026 年最热开源 AI Agent 项目引入移动端，将本地 AI Agent 作为 Android App 的 AI 后端
2. **多平台 AI 支持**：通过统一接口抽象，一行配置切换不同国内外 AI 服务商
3. **反幻觉设计**：真实 RSS 抓取优先于 AI 生成，解决 AI 幻觉 URL 问题
4. **间隔重复算法**：SRS（Spaced Repetition System）在词库复习中的实现
