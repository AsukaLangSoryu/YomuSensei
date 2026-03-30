package com.yomusensei.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.yomusensei.data.model.UserMemory
import kotlinx.coroutines.flow.first

private val Context.memoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "memory")

class MemoryRepository(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val USER_MEMORY = stringPreferencesKey("user_memory")
    }

    suspend fun getMemory(): UserMemory {
        val prefs = context.memoryDataStore.data.first()
        val json = prefs[USER_MEMORY]
        return if (json != null) {
            try {
                gson.fromJson(json, UserMemory::class.java)
            } catch (e: Exception) {
                UserMemory()
            }
        } else {
            UserMemory()
        }
    }

    suspend fun updateMemory(memory: UserMemory) {
        context.memoryDataStore.edit { prefs ->
            prefs[USER_MEMORY] = gson.toJson(memory.copy(lastUpdated = System.currentTimeMillis()))
        }
    }

    fun buildSystemPrompt(basePrompt: String, memory: UserMemory): String {
        return """
$basePrompt

用户信息：
- 日语水平：${memory.userProfile.japaneseLevel}
- 学习目标：${memory.userProfile.learningGoals.joinToString("、")}
${if (memory.userProfile.interests.isNotEmpty()) "- 感兴趣的主题：${memory.userProfile.interests.joinToString("、")}" else ""}
- 已阅读文章：${memory.learningProgress.articlesRead} 篇
- 词库单词：${memory.learningProgress.vocabularySize} 个
${if (memory.learningProgress.favoriteAuthors.isNotEmpty()) "- 喜欢的作者：${memory.learningProgress.favoriteAuthors.joinToString("、")}" else ""}
${if (memory.learningProgress.recentTopics.isNotEmpty()) "- 最近阅读主题：${memory.learningProgress.recentTopics.joinToString("、")}" else ""}
- 偏好文章长度：${memory.preferences.preferredArticleLength}
- 偏好难度：${memory.preferences.preferredDifficulty}
${if (memory.preferences.avoidTopics.isNotEmpty()) "- 不感兴趣的主题：${memory.preferences.avoidTopics.joinToString("、")}" else ""}
        """.trimIndent()
    }
}
