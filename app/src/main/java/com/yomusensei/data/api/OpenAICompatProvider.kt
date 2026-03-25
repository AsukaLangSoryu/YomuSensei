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
    private val baseUrl: String,
    private val modelName: String
) : AiProvider {

    private val service: OpenAIApiService = Retrofit.Builder()
        .baseUrl("https://placeholder.com/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
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
        val prompt = """
你是一个日语阅读助手。用户需求：$userRequest

请用以下JSON格式返回3篇推荐文章：
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
