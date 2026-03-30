package com.yomusensei.data.api

import com.yomusensei.data.model.ArticleSearchResult

enum class UserIntent {
    ARTICLE_REQUEST,
    JAPANESE_QUESTION,
    GENERAL_CHAT
}

interface AiProvider {
    suspend fun chat(userMessage: String, systemPrompt: String? = null): Result<String>
    suspend fun chatWithHistory(history: List<Pair<String, Boolean>>, systemPrompt: String): Result<String>
    suspend fun detectIntent(userMessage: String): UserIntent
    suspend fun requestArticles(userRequest: String): Result<ArticleSearchResult>
    suspend fun explainText(text: String, context: String = ""): Result<String>
    suspend fun askQuestion(question: String, articleContext: String = ""): Result<String>
}

/**
 * Marker interface for providers that support tool/function calling
 */
interface ToolCapableProvider
