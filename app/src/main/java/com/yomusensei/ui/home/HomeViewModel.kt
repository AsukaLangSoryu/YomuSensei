package com.yomusensei.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yomusensei.data.api.AiProvider
import com.yomusensei.data.api.GeminiProvider
import com.yomusensei.data.api.ToolCapableProvider
import com.yomusensei.data.api.UserIntent
import com.yomusensei.data.model.Article
import com.yomusensei.data.model.ChatMessage
import com.yomusensei.data.model.GeminiContent
import com.yomusensei.data.model.GeminiPart
import com.yomusensei.data.model.GroundingChunk
import com.yomusensei.data.web.WebScraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ChatMode {
    AUTO,
    ARTICLE,
    FREE_CHAT
}

class HomeViewModel(
    private val aiProvider: AiProvider,
    private val webScraper: WebScraper,
    private val toolExecutor: com.yomusensei.data.api.tools.ToolExecutor? = null,
    private val memoryRepository: com.yomusensei.data.api.MemoryRepository? = null
) : ViewModel() {

    private val _chatMode = MutableStateFlow(ChatMode.AUTO)
    val chatMode: StateFlow<ChatMode> = _chatMode.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedArticle = MutableStateFlow<Article?>(null)
    val selectedArticle: StateFlow<Article?> = _selectedArticle.asStateFlow()

    private val _isLoadingArticle = MutableStateFlow(false)
    val isLoadingArticle: StateFlow<Boolean> = _isLoadingArticle.asStateFlow()

    private val _loadingMessage = MutableStateFlow<String?>(null)
    val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

    private val gson = Gson()

    // Circuit breaker for fetch failures
    private var consecutiveFetchFailures = 0
    private val maxConsecutiveFailures = 3

    init {
        _messages.value = listOf(getWelcomeMessage(ChatMode.AUTO))
    }

    fun setChatMode(mode: ChatMode) {
        _chatMode.value = mode
        _messages.value = listOf(getWelcomeMessage(mode))
    }

    private fun getWelcomeMessage(mode: ChatMode): ChatMessage {
        return when (mode) {
            ChatMode.AUTO -> ChatMessage(
                content = "🤖 智能模式\n\n我会自动理解你的需求：\n• 想找文章？我会搜索推荐\n• 有日语问题？我来解答\n• 随便聊聊？我也可以\n\n直接告诉我你想做什么吧！",
                isFromUser = false
            )
            ChatMode.ARTICLE -> ChatMessage(
                content = "📚 找文章模式\n\n专注帮你找日语阅读材料！\n\n试试这样说：\n• \"推荐一篇简单的新闻\"\n• \"我想看夏目漱石的小说\"\n• \"有关于日本文化的文章吗\"\n\n我会直接给你文章列表，点击即可阅读！",
                isFromUser = false
            )
            ChatMode.FREE_CHAT -> ChatMessage(
                content = "💬 聊天模式\n\n我是你的日语学习顾问！\n\n你可以问我：\n• 日语语法问题\n• 学习方法建议\n• 文化背景知识\n• 或者随便聊聊\n\n这个模式不会推荐文章，专注对话交流～",
                isFromUser = false
            )
        }
    }

    private fun getConversationHistory(): List<Pair<String, Boolean>> {
        return _messages.value
            .drop(1)
            .map { it.content to it.isFromUser }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            _messages.value = _messages.value + ChatMessage(content = text, isFromUser = true)
            _isLoading.value = true

            when (_chatMode.value) {
                ChatMode.AUTO -> handleAutoMode(text)
                ChatMode.ARTICLE -> handleArticleMode(text)
                ChatMode.FREE_CHAT -> handleFreeChatMode(text)
            }

            _isLoading.value = false
        }
    }

    private suspend fun handleAutoMode(text: String) {
        if (aiProvider is ToolCapableProvider && toolExecutor != null) {
            handleWithTools(text)
        } else {
            val intent = aiProvider.detectIntent(text)
            when (intent) {
                UserIntent.ARTICLE_REQUEST -> handleArticleMode(text)
                UserIntent.JAPANESE_QUESTION -> handleJapaneseQuestion(text)
                UserIntent.GENERAL_CHAT -> handleFreeChatMode(text)
            }
        }
    }

    private suspend fun handleWithTools(text: String) {
        val memory = memoryRepository?.getMemory() ?: com.yomusensei.data.model.UserMemory()
        val basePrompt = """
你是読む先生，一个友好的日语阅读AI助手。请用中文回复用户。

你可以使用工具帮助用户：
- search_japanese_articles: 搜索日语文章，返回文章列表供用户点击阅读
- lookup_word: 查询日语单词读音和释义
- save_vocabulary: 保存单词到词库

重要规则：
1. 使用 search_japanese_articles 搜索后，直接展示搜索结果，告诉用户"点击文章卡片即可开始阅读"
2. 不要使用 fetch_webpage 工具自动抓取文章内容
3. 用户是日语初学者（N5-N4水平），解释单词时提供读音（平假名）、词性和中文释义
""".trimIndent()

        val systemPrompt = memoryRepository?.buildSystemPrompt(basePrompt, memory) ?: basePrompt

        val history = buildGeminiHistory()

        (aiProvider as GeminiProvider).chatWithTools(
            history = history,
            systemPrompt = systemPrompt,
            toolExecutor = toolExecutor!!,
            onToolStatus = { status -> _loadingMessage.value = status }
        ).fold(
            onSuccess = { response ->
                _loadingMessage.value = null
                _messages.value = _messages.value + ChatMessage(
                    content = response.text,
                    isFromUser = false,
                    articles = response.articles.ifEmpty { null }
                )
            },
            onFailure = { error ->
                _loadingMessage.value = null
                _messages.value = _messages.value + ChatMessage(
                    content = "抱歉，出现了错误：${error.message}",
                    isFromUser = false
                )
            }
        )
    }

    private fun buildGeminiHistory(): List<GeminiContent> {
        return _messages.value.drop(1).map { msg ->
            GeminiContent(
                parts = listOf(GeminiPart(text = msg.content)),
                role = if (msg.isFromUser) "user" else "model"
            )
        }
    }

    private suspend fun handleArticleMode(text: String) {
        // Try NHK direct scraping first
        val nhkResult = webScraper.fetchNhkEasyArticleList()
        if (nhkResult.isSuccess) {
            val articles = nhkResult.getOrNull().orEmpty()
            if (articles.isNotEmpty()) {
                _messages.value = _messages.value + ChatMessage(
                    content = "📰 为你找到 NHK Web Easy 最新新闻 ${articles.size} 篇\n\n这些都是适合初学者的简单新闻，点击卡片即可开始阅读！",
                    isFromUser = false,
                    articles = articles
                )
                return
            }
        }

        // Fall back to AI search with article-focused prompt
        val memory = memoryRepository?.getMemory() ?: com.yomusensei.data.model.UserMemory()
        val systemPrompt = """
你是日语阅读助手，专门帮用户找适合的日语文章。

用户水平：${memory.userProfile.japaneseLevel}
偏好难度：${memory.preferences.preferredDifficulty}
偏好长度：${memory.preferences.preferredArticleLength}

请根据用户需求推荐文章，用简短友好的语气回复（1-2句话），然后提供文章列表。
如果搜索失败，给出具体建议（如"试试搜索'日本料理'或'夏目漱石'"）。
""".trimIndent()

        val result = aiProvider.chat(text, systemPrompt)
        result.fold(
            onSuccess = { response ->
                _messages.value = _messages.value + ChatMessage(
                    content = response,
                    isFromUser = false
                )
            },
            onFailure = { error ->
                _messages.value = _messages.value + ChatMessage(
                    content = "抱歉，搜索遇到问题：${error.message}\n\n💡 试试这样说：\n• \"推荐简单的新闻\"\n• \"我想看夏目漱石的作品\"\n• \"有关于日本文化的文章吗\"",
                    isFromUser = false
                )
            }
        )
    }

    private fun buildArticlesFromGrounding(chunks: List<GroundingChunk>): List<Article> {
        return chunks.mapNotNull { chunk ->
            val web = chunk.web
            if (web?.uri != null && web.title != null) {
                Article(
                    title = web.title,
                    url = web.uri,
                    description = "来自 Google 搜索结果",
                    source = web.uri.substringAfter("://").substringBefore("/")
                )
            } else null
        }.distinctBy { it.url }
    }

    private suspend fun handleJapaneseQuestion(text: String) {
        val result = aiProvider.askQuestion(text)
        result.fold(
            onSuccess = { response ->
                _messages.value = _messages.value + ChatMessage(content = response, isFromUser = false)
            },
            onFailure = { error ->
                _messages.value = _messages.value + ChatMessage(
                    content = "抱歉，出现了错误：${error.message}",
                    isFromUser = false
                )
            }
        )
    }

    private suspend fun handleFreeChatMode(text: String) {
        val memory = memoryRepository?.getMemory() ?: com.yomusensei.data.model.UserMemory()
        val basePrompt = """
你是読む先生，一个友好的日语学习助手。
你可以和用户聊天，回答问题，提供日语学习建议。
保持对话自然流畅，记住之前的对话内容。
用中文回复，必要时可以使用日语举例。
""".trimIndent()

        val systemPrompt = memoryRepository?.buildSystemPrompt(basePrompt, memory) ?: basePrompt

        val history = getConversationHistory()
        val result = aiProvider.chatWithHistory(history, systemPrompt)

        result.fold(
            onSuccess = { response ->
                _messages.value = _messages.value + ChatMessage(content = response, isFromUser = false)
            },
            onFailure = { error ->
                _messages.value = _messages.value + ChatMessage(
                    content = "抱歉，出现了错误：${error.message}",
                    isFromUser = false
                )
            }
        )
    }

    fun selectArticle(article: Article) {
        viewModelScope.launch {
            _isLoadingArticle.value = true
            val result = webScraper.fetchArticle(article.url)
            result.fold(
                onSuccess = { scraped ->
                    consecutiveFetchFailures = 0
                    _selectedArticle.value = article.copy(
                        content = scraped.content,
                        title = scraped.title.ifBlank { article.title }
                    )

                    // 更新记忆：阅读文章数 +1
                    memoryRepository?.let { repo ->
                        val memory = repo.getMemory()
                        val updatedMemory = memory.copy(
                            learningProgress = memory.learningProgress.copy(
                                articlesRead = memory.learningProgress.articlesRead + 1
                            )
                        )
                        repo.updateMemory(updatedMemory)
                    }
                },
                onFailure = { error ->
                    consecutiveFetchFailures++
                    _messages.value = _messages.value + ChatMessage(
                        content = "无法加载文章：${error.message}",
                        isFromUser = false
                    )
                }
            )
            _isLoadingArticle.value = false
        }
    }

    fun clearSelectedArticle() {
        _selectedArticle.value = null
    }

    fun loadArticleFromUrl(url: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            _isLoadingArticle.value = true
            val result = webScraper.fetchArticle(url)
            result.fold(
                onSuccess = { scraped ->
                    _selectedArticle.value = Article(
                        title = scraped.title,
                        url = url,
                        content = scraped.content,
                        source = url.substringAfter("://").substringBefore("/")
                    )
                },
                onFailure = { error ->
                    _messages.value = _messages.value + ChatMessage(
                        content = "无法加载文章：${error.message}\n\n请检查网址是否正确，或者该网站可能不支持抓取。",
                        isFromUser = false
                    )
                }
            )
            _isLoadingArticle.value = false
        }
    }

    private fun parseAiResponse(response: String): AiArticleResponse {
        return try {
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                gson.fromJson(jsonStr, AiArticleResponse::class.java)
                    ?: AiArticleResponse(message = response, articles = null)
            } else {
                AiArticleResponse(message = response, articles = null)
            }
        } catch (e: JsonSyntaxException) {
            AiArticleResponse(message = response, articles = null)
        } catch (e: Exception) {
            AiArticleResponse(message = response, articles = null)
        }
    }
}

data class AiArticleResponse(
    val message: String,
    val articles: List<Article>?
)
