package com.yomusensei.data.api

import com.yomusensei.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GeminiProvider(private val settingsRepository: SettingsRepository) : AiProvider, ToolCapableProvider {

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

    override suspend fun chat(userMessage: String, systemPrompt: String?): Result<String> {
        val apiKey = settingsRepository.getGeminiApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置 Gemini API Key"))
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

    override suspend fun chatWithHistory(
        history: List<Pair<String, Boolean>>,
        systemPrompt: String
    ): Result<String> {
        val apiKey = settingsRepository.getGeminiApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置 Gemini API Key"))
        }

        return try {
            val contents = mutableListOf<GeminiContent>()

            contents.add(GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt)),
                role = "user"
            ))
            contents.add(GeminiContent(
                parts = listOf(GeminiPart(text = "好的，我明白了。")),
                role = "model"
            ))

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

    override suspend fun detectIntent(userMessage: String): UserIntent {
        val apiKey = settingsRepository.getGeminiApiKey()
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

    override suspend fun requestArticles(userRequest: String): Result<ArticleSearchResult> {
        val apiKey = settingsRepository.getGeminiApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置 Gemini API Key"))
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

    override suspend fun explainText(text: String, context: String): Result<String> {
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

    override suspend fun askQuestion(question: String, articleContext: String): Result<String> {
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

    suspend fun chatWithTools(
        history: List<GeminiContent>,
        systemPrompt: String,
        toolExecutor: com.yomusensei.data.api.tools.ToolExecutor,
        onToolStatus: (String) -> Unit
    ): Result<ToolChatResponse> {
        val apiKey = settingsRepository.getGeminiApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("请先在设置中配置 Gemini API Key"))
        }

        return try {
            val contents = mutableListOf<GeminiContent>()

            contents.add(GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt)),
                role = "user"
            ))
            contents.add(GeminiContent(
                parts = listOf(GeminiPart(text = "好的，我明白了。")),
                role = "model"
            ))

            contents.addAll(history)

            val tools = listOf(
                GeminiTool(
                    function_declarations = com.yomusensei.data.api.tools.ToolDefinitions.AUTO_MODE
                )
            )

            val collectedArticles = mutableListOf<Article>()
            var roundCount = 0
            val maxRounds = 5

            while (roundCount < maxRounds) {
                roundCount++

                val request = GeminiRequest(contents = contents, tools = tools)
                val response = apiService.generateContent(apiKey, request)

                if (response.error != null) {
                    return Result.failure(Exception(response.error.message ?: "API调用失败"))
                }

                val candidate = response.candidates?.firstOrNull()
                val parts = candidate?.content?.parts ?: emptyList()

                val functionCallPart = parts.firstOrNull { it.functionCall != null }

                if (functionCallPart != null) {
                    val functionCall = functionCallPart.functionCall!!

                    val statusMsg = when (functionCall.name) {
                        "search_japanese_articles" -> "正在搜索日语文章..."
                        "search_nhk_easy" -> "正在搜索 NHK 简单新闻..."
                        "search_aozora" -> "正在搜索青空文库..."
                        "fetch_webpage" -> "正在加载网页..."
                        "lookup_word" -> "正在查询词典..."
                        "save_vocabulary" -> "正在保存单词..."
                        else -> "正在处理..."
                    }
                    onToolStatus(statusMsg)

                    contents.add(GeminiContent(
                        parts = listOf(GeminiPart(functionCall = functionCall)),
                        role = "model"
                    ))

                    val functionResponse = toolExecutor.execute(functionCall)

                    if (functionCall.name == "search_japanese_articles" || functionCall.name == "search_nhk_easy" || functionCall.name == "search_aozora") {
                        @Suppress("UNCHECKED_CAST")
                        val articlesList = functionResponse.response["articles"] as? List<Map<String, Any>>
                        articlesList?.forEach { articleMap ->
                            val title = articleMap["title"] as? String ?: ""
                            val url = articleMap["url"] as? String ?: ""
                            val source = articleMap["source"] as? String ?: articleMap["summary"] as? String ?: ""
                            if (title.isNotBlank() && url.isNotBlank()) {
                                collectedArticles.add(Article(title = title, url = url, source = source))
                            }
                        }
                    }

                    contents.add(GeminiContent(
                        parts = listOf(GeminiPart(functionResponse = functionResponse)),
                        role = "user"
                    ))

                    continue
                }

                val text = parts.firstOrNull { it.text != null }?.text ?: ""
                return Result.success(ToolChatResponse(text = text, articles = collectedArticles))
            }

            Result.success(ToolChatResponse(
                text = "抱歉，处理您的请求时遇到了问题，请重试。",
                articles = collectedArticles
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
