package com.yomusensei.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 本地文章管理器 - 从 assets 读取离线文章
 */
class LocalArticleManager(private val context: Context) {

    /**
     * 读取本地文章内容
     */
    suspend fun readArticle(authorId: String, workId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val filename = "${authorId}_${workId}.txt"
            val inputStream = context.assets.open("aozora/$filename")
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val content = reader.readText()
            reader.close()
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查文章是否存在
     */
    suspend fun hasArticle(authorId: String, workId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filename = "${authorId}_${workId}.txt"
            context.assets.list("aozora")?.contains(filename) ?: false
        } catch (e: Exception) {
            false
        }
    }
}
