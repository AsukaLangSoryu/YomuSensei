package com.yomusensei.data.model

import com.yomusensei.data.vocabulary.VocabularyWord

/**
 * 聊天消息
 */
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val articles: List<Article>? = null  // AI推荐的文章列表
)

/**
 * 文章
 */
data class Article(
    val title: String,
    val url: String,
    val description: String = "",
    val content: String = "",  // 正文内容
    val source: String = ""    // 来源网站名
)

/**
 * 选中的文本及其解释
 */
data class TextExplanation(
    val selectedText: String,
    val explanation: String,
    val isLoading: Boolean = false
)

/**
 * Gemini API 请求体
 */
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig = GenerationConfig(),
    val tools: List<GeminiTool>? = null
)

/**
 * Gemini 工具定义
 */
data class GeminiTool(
    val google_search: GoogleSearchTool? = null
)

/**
 * Google Search 工具（空对象即可启用）
 */
class GoogleSearchTool

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 4096
)

/**
 * Gemini API 响应体
 */
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val error: GeminiError?
)

data class GeminiCandidate(
    val content: GeminiContent?,
    val groundingMetadata: GroundingMetadata? = null
)

/**
 * Google Search Grounding 元数据
 */
data class GroundingMetadata(
    val searchEntryPoint: SearchEntryPoint? = null,
    val groundingChunks: List<GroundingChunk>? = null,
    val webSearchQueries: List<String>? = null
)

data class SearchEntryPoint(
    val renderedContent: String? = null
)

data class GroundingChunk(
    val web: WebChunk? = null
)

data class WebChunk(
    val uri: String? = null,
    val title: String? = null
)

data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?
)

/**
 * 文章搜索结果（包含AI回复和grounding数据）
 */
data class ArticleSearchResult(
    val text: String,
    val groundingChunks: List<GroundingChunk>?
)

/**
 * 复习题目
 */
data class ReviewQuestion(
    val word: VocabularyWord,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

/**
 * 复习结果
 */
data class ReviewResult(
    val word: String,
    val isCorrect: Boolean
)

/**
 * 词库统计信息
 */
data class VocabularyStats(
    val totalCount: Int = 0,
    val todayAdded: Int = 0,
    val pendingReview: Int = 0,
    val masteredCount: Int = 0
) {
    val masteryRate: Float
        get() = if (totalCount > 0) masteredCount.toFloat() / totalCount else 0f
}
