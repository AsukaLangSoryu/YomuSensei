package com.yomusensei.data.api.tools

import com.yomusensei.data.model.FunctionDeclaration
import com.yomusensei.data.model.JsonSchema
import com.yomusensei.data.model.JsonSchemaProperty

object ToolDefinitions {

    val fetchWebpage = FunctionDeclaration(
        name = "fetch_webpage",
        description = "抓取日语网页的标题和正文内容。当用户提供了URL，或者你需要加载某篇文章时使用。",
        parameters = JsonSchema(
            type = "object",
            properties = mapOf(
                "url" to JsonSchemaProperty("string", "要抓取的网页URL")
            ),
            required = listOf("url")
        )
    )

    val searchJapaneseArticles = FunctionDeclaration(
        name = "search_japanese_articles",
        description = "搜索日语文章和新闻，返回文章列表供用户选择。重要：如果用户输入的是中文关键词（如'银河铁道之夜'、'川端康成'），你必须先将其翻译成日语（如'銀河鉄道の夜'、'川端康成'）后再传入query参数。",
        parameters = JsonSchema(
            type = "object",
            properties = mapOf(
                "query" to JsonSchemaProperty("string", "搜索关键词（必须是日语），例如：'日本経済ニュース'、'銀河鉄道の夜'、'川端康成'"),
                "topic" to JsonSchemaProperty("string", "可选：文章类型，可选值：news（新闻）、literature（文学）、easy（简单日语）、novel（网络小说）")
            ),
            required = listOf("query")
        )
    )

    val lookupWord = FunctionDeclaration(
        name = "lookup_word",
        description = "在Jisho词典中查询日语单词，获取读音、词性和中文含义。当用户询问某个日语单词的意思时使用。",
        parameters = JsonSchema(
            type = "object",
            properties = mapOf(
                "word" to JsonSchemaProperty("string", "要查询的日语单词")
            ),
            required = listOf("word")
        )
    )

    val saveVocabulary = FunctionDeclaration(
        name = "save_vocabulary",
        description = "将日语单词保存到用户的词库以便日后复习。解释单词后，主动询问用户是否要保存，或根据上下文判断。",
        parameters = JsonSchema(
            type = "object",
            properties = mapOf(
                "word" to JsonSchemaProperty("string", "日语单词"),
                "reading" to JsonSchemaProperty("string", "平假名读音"),
                "meaning" to JsonSchemaProperty("string", "中文释义")
            ),
            required = listOf("word", "reading", "meaning")
        )
    )

    val ALL: List<FunctionDeclaration> = listOf(
        fetchWebpage, searchJapaneseArticles, lookupWord, saveVocabulary
    )

    // AUTO mode tools - excludes fetch_webpage to prevent auto-fetching failures
    val AUTO_MODE: List<FunctionDeclaration> = listOf(
        searchJapaneseArticles, lookupWord, saveVocabulary
    )
}
