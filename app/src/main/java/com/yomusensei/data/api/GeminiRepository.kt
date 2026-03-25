package com.yomusensei.data.api

import com.yomusensei.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 用户意图类型
 */
enum class UserIntent {
    ARTICLE_REQUEST,    // 请求推荐文章
    JAPANESE_QUESTION,  // 日语学习问题
    GENERAL_CHAT        // 普通闲聊
}

class GeminiRepository(private val settingsRepository: SettingsRepository) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(GeminiApiService::class.java)

    /**
     * 发送消息给Gemini并获取回复（单轮）
     */
    suspend fun chat(userMessage: String, systemPrompt: String? = null): Result<String> {
        val apiKey = settingsRepository.getApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置API Key"))
        }

        return try {
            val fullMessage = if (!systemPrompt.isNullOrBlank()) {
                "$systemPrompt\n\n用户请求：$userMessage"
            } else {
                userMessage
            }

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = fullMessage)),
                        role = "user"
                    )
                )
            )

            val response = apiService.generateContent(apiKey, request)

            if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "API调用失败"))
            } else {
                val text = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text ?: ""
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 多轮对话（支持对话历史）
     */
    suspend fun chatWithHistory(
        history: List<Pair<String, Boolean>>,
        systemPrompt: String
    ): Result<String> {
        val apiKey = settingsRepository.getApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置API Key"))
        }

        return try {
            val contents = mutableListOf<GeminiContent>()

            // 添加系统提示作为第一条用户消息
            contents.add(GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt)),
                role = "user"
            ))
            contents.add(GeminiContent(
                parts = listOf(GeminiPart(text = "好的，我明白了。")),
                role = "model"
            ))

            // 添加对话历史
            history.forEach { (message, isFromUser) ->
                contents.add(GeminiContent(
                    parts = listOf(GeminiPart(text = message)),
                    role = if (isFromUser) "user" else "model"
                ))
            }

            val request = GeminiRequest(contents = contents)
            val response = apiService.generateContent(apiKey, request)

            if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "API调用失败"))
            } else {
                val text = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text ?: ""
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 识别用户意图
     */
    suspend fun detectIntent(userMessage: String): UserIntent {
        val apiKey = settingsRepository.getApiKey()
        if (apiKey.isBlank()) {
            return UserIntent.GENERAL_CHAT
        }

        return try {
            val prompt = """
分析用户消息的意图，只返回一个数字：
1 = 想要推荐日语文章/新闻/阅读材料
2 = 日语学习问题（语法、词汇、发音等）
3 = 普通闲聊或其他

用户消息：$userMessage

只返回数字1、2或3，不要其他内容。
""".trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt)),
                        role = "user"
                    )
                )
            )

            val response = apiService.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text?.trim() ?: "3"

            when {
                text.contains("1") -> UserIntent.ARTICLE_REQUEST
                text.contains("2") -> UserIntent.JAPANESE_QUESTION
                else -> UserIntent.GENERAL_CHAT
            }
        } catch (e: Exception) {
            UserIntent.GENERAL_CHAT
        }
    }

    /**
     * 文章搜索结果（包含AI回复和grounding数据）
     */
    data class ArticleSearchResult(
        val text: String,
        val groundingChunks: List<GroundingChunk>?
    )

    /**
     * 请求AI推荐日语文章（使用 Google Search 获取真实URL）
     */
    suspend fun requestArticles(userRequest: String): Result<ArticleSearchResult> {
        val apiKey = settingsRepository.getApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置API Key"))
        }

        return try {
            val prompt = """
你是一个日语阅读助手。用户想要阅读日语文章。

用户需求：$userRequest

请使用Google搜索功能搜索真实存在的日语文章。
搜索后，请直接用以下JSON格式返回搜索到的结果（不要说"我会帮你搜索"这类话，直接给出搜索结果）：

{
  "message": "根据你的需求，我为你找到了以下日语文章：",
  "articles": [
    {
      "title": "文章标题（日语原标题）",
      "url": "搜索到的真实URL",
      "description": "简短描述这篇文章的内容（中文）",
      "source": "来源网站名"
    }
  ]
}

优先推荐这些来源的文章：
- NHK News Web Easy（简单日语新闻，适合初学者）
- 青空文库（日本文学经典，免费阅读）
- 日语维基百科

注意：必须返回JSON格式，不要返回其他内容。
""".trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt)),
                        role = "user"
                    )
                ),
                tools = listOf(GeminiTool(google_search = GoogleSearchTool()))
            )

            val response = apiService.generateContent(apiKey, request)

            if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "API调用失败"))
            } else {
                val candidate = response.candidates?.firstOrNull()
                val text = candidate?.content?.parts?.firstOrNull()?.text ?: ""
                val groundingChunks = candidate?.groundingMetadata?.groundingChunks
                Result.success(ArticleSearchResult(text, groundingChunks))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 解释选中的日语文本
     */
    suspend fun explainText(text: String, context: String = ""): Result<String> {
        val systemPrompt = """
你是一个专业的日语教师，帮助学习者理解日语文本。

用户会给你一段日语文本，请提供详细的解释：

如果是单词/短语：
- 读音（平假名）
- 词性
- 中文释义
- 1-2个例句
- 相关词或同义词

如果是句子：
- 逐词分析，解释每个词的意思和作用
- 语法点说明
- 整句翻译

请用简洁清晰的格式回复，便于阅读。
""".trimIndent()

        val userMessage = if (context.isNotBlank()) {
            "请解释这段文本：「$text」\n\n上下文：$context"
        } else {
            "请解释这段文本：「$text」"
        }

        return chat(userMessage, systemPrompt)
    }

    /**
     * 自由提问关于日语的问题
     */
    suspend fun askQuestion(question: String, articleContext: String = ""): Result<String> {
        val systemPrompt = """
你是一个耐心的日语老师，专门帮助学习者解答日语学习中的疑问。
请用通俗易懂的方式解释，必要时举例说明。
回答要简洁实用，避免过于学术化。
""".trimIndent()

        val userMessage = if (articleContext.isNotBlank()) {
            "正在阅读的文章内容：\n$articleContext\n\n我的问题：$question"
        } else {
            question
        }

        return chat(userMessage, systemPrompt)
    }
}
