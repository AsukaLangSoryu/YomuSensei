package com.yomusensei.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yomusensei.data.api.AiProvider
import com.yomusensei.data.api.UserIntent
import com.yomusensei.data.model.Article
import com.yomusensei.data.model.ChatMessage
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
    private val webScraper: WebScraper
) : ViewModel() {

    private val _chatMode = MutableStateFlow(ChatMode.AUTO)
    val chatMode: StateFlow<ChatMode> = _chatMode.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "你好！我是読む先生，你的日语阅读助手。\n\n你可以：\n• 让我推荐日语文章\n• 问我日语学习问题\n• 或者随便聊聊\n\n我会自动理解你的需求！",
                isFromUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedArticle = MutableStateFlow<Article?>(null)
    val selectedArticle: StateFlow<Article?> = _selectedArticle.asStateFlow()

    private val _isLoadingArticle = MutableStateFlow(false)
    val isLoadingArticle: StateFlow<Boolean> = _isLoadingArticle.asStateFlow()

    private val gson = Gson()

    fun setChatMode(mode: ChatMode) {
        _chatMode.value = mode
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
        val intent = aiProvider.detectIntent(text)
        when (intent) {
            UserIntent.ARTICLE_REQUEST -> handleArticleMode(text)
            UserIntent.JAPANESE_QUESTION -> handleJapaneseQuestion(text)
            UserIntent.GENERAL_CHAT -> handleFreeChatMode(text)
        }
    }

    private suspend fun handleArticleMode(text: String) {
        // Try NHK direct scraping first
        val nhkResult = webScraper.fetchNhkEasyArticleList()
        if (nhkResult.isSuccess) {
            val articles = nhkResult.getOrNull().orEmpty()
            if (articles.isNotEmpty()) {
                _messages.value = _messages.value + ChatMessage(
                    content = "已为你获取最新 NHK Web Easy 新闻 ${articles.size} 篇，点击即可开始阅读：",
                    isFromUser = false,
                    articles = articles
                )
                return
            }
        }

        // Fall back to AI search
        val result = aiProvider.requestArticles(text)
        result.fold(
            onSuccess = { searchResult ->
                val parsedResponse = parseAiResponse(searchResult.text)

                val articles = if (parsedResponse.articles.isNullOrEmpty() && !searchResult.groundingChunks.isNullOrEmpty()) {
                    buildArticlesFromGrounding(searchResult.groundingChunks)
                } else {
                    parsedResponse.articles ?: emptyList()
                }

                val message = if (articles.isEmpty()) {
                    if (parsedResponse.message.contains("我会") || parsedResponse.message.contains("帮你")) {
                        "抱歉，搜索暂时没有返回结果。请尝试更具体的描述，例如：\n• 我想看关于日本料理的简单新闻\n• 推荐一篇青空文库的短篇小说"
                    } else {
                        parsedResponse.message
                    }
                } else {
                    if (parsedResponse.message.isBlank() || parsedResponse.message.contains("我会")) {
                        "为你找到了以下日语文章，点击即可开始阅读："
                    } else {
                        parsedResponse.message
                    }
                }

                _messages.value = _messages.value + ChatMessage(
                    content = message,
                    isFromUser = false,
                    articles = articles
                )
            },
            onFailure = { error ->
                _messages.value = _messages.value + ChatMessage(
                    content = "抱歉，出现了错误：${error.message}",
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
        val systemPrompt = """
你是読む先生，一个友好的日语学习助手。
你可以和用户聊天，回答问题，提供日语学习建议。
保持对话自然流畅，记住之前的对话内容。
用中文回复，必要时可以使用日语举例。
""".trimIndent()

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
                    _selectedArticle.value = article.copy(
                        content = scraped.content,
                        title = scraped.title.ifBlank { article.title }
                    )
                },
                onFailure = { error ->
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
