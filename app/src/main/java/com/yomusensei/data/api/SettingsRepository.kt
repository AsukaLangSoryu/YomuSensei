package com.yomusensei.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ProviderType { GEMINI, OPENAI_COMPAT }

val PRESET_BASE_URLS = mapOf(
    "OpenAI" to "https://api.openai.com/v1",
    "DeepSeek" to "https://api.deepseek.com/v1",
    "智谱 GLM" to "https://open.bigmodel.cn/api/paas/v4",
    "Kimi" to "https://api.moonshot.cn/v1"
)

class SettingsRepository(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")           // 兼容旧版
        private val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        private val FONT_SIZE = floatPreferencesKey("font_size")
        private val LINE_SPACING = floatPreferencesKey("line_spacing")
        private val PADDING_HORIZONTAL = intPreferencesKey("padding_horizontal")
        private val BACKGROUND_MODE = stringPreferencesKey("background_mode")
        private val PROVIDER_TYPE = stringPreferencesKey("provider_type")
        private val OPENAI_COMPAT_API_KEY = stringPreferencesKey("openai_compat_api_key")
        private val OPENAI_COMPAT_BASE_URL = stringPreferencesKey("openai_compat_base_url")
        private val OPENAI_COMPAT_MODEL = stringPreferencesKey("openai_compat_model")
    }

    // ========== Gemini API Key ==========

    suspend fun getGeminiApiKey(): String {
        val prefs = context.dataStore.data.first()
        return prefs[GEMINI_API_KEY] ?: prefs[API_KEY] ?: ""
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY] = key
            preferences[API_KEY] = key  // keep legacy key in sync
        }
    }

    // Legacy alias
    suspend fun getApiKey(): String = getGeminiApiKey()
    suspend fun setApiKey(key: String) = setGeminiApiKey(key)
    fun getApiKeyFlow(): Flow<String> = getGeminiApiKeyFlow()

    fun getGeminiApiKeyFlow(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[GEMINI_API_KEY] ?: prefs[API_KEY] ?: ""
        }
    }

    // ========== Provider Type ==========

    suspend fun getProviderType(): ProviderType {
        val raw = context.dataStore.data.first()[PROVIDER_TYPE] ?: "GEMINI"
        return try { ProviderType.valueOf(raw) } catch (e: Exception) { ProviderType.GEMINI }
    }

    suspend fun setProviderType(type: ProviderType) {
        context.dataStore.edit { it[PROVIDER_TYPE] = type.name }
    }

    fun getProviderTypeFlow(): Flow<ProviderType> {
        return context.dataStore.data.map { prefs ->
            val raw = prefs[PROVIDER_TYPE] ?: "GEMINI"
            try { ProviderType.valueOf(raw) } catch (e: Exception) { ProviderType.GEMINI }
        }
    }

    // ========== OpenAI-compatible settings ==========

    suspend fun getOpenAICompatApiKey(): String {
        return context.dataStore.data.first()[OPENAI_COMPAT_API_KEY] ?: ""
    }

    suspend fun setOpenAICompatApiKey(key: String) {
        context.dataStore.edit { it[OPENAI_COMPAT_API_KEY] = key }
    }

    suspend fun getOpenAICompatBaseUrl(): String {
        return context.dataStore.data.first()[OPENAI_COMPAT_BASE_URL] ?: "https://api.openai.com/v1"
    }

    suspend fun setOpenAICompatBaseUrl(url: String) {
        context.dataStore.edit { it[OPENAI_COMPAT_BASE_URL] = url }
    }

    suspend fun getOpenAICompatModel(): String {
        return context.dataStore.data.first()[OPENAI_COMPAT_MODEL] ?: "gpt-4o-mini"
    }

    suspend fun setOpenAICompatModel(model: String) {
        context.dataStore.edit { it[OPENAI_COMPAT_MODEL] = model }
    }

    // ========== Factory ==========

    suspend fun buildAiProvider(): AiProvider {
        return when (getProviderType()) {
            ProviderType.GEMINI -> GeminiProvider(this)
            ProviderType.OPENAI_COMPAT -> OpenAICompatProvider(
                apiKey = getOpenAICompatApiKey(),
                baseUrl = getOpenAICompatBaseUrl(),
                modelName = getOpenAICompatModel()
            )
        }
    }

    // ========== Font Size ==========

    suspend fun getFontSize(): Float {
        return context.dataStore.data.first()[FONT_SIZE] ?: 18f
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { it[FONT_SIZE] = size }
    }

    fun getFontSizeFlow(): Flow<Float> {
        return context.dataStore.data.map { it[FONT_SIZE] ?: 18f }
    }

    // ========== Line Spacing ==========

    fun getLineSpacingFlow(): Flow<Float> {
        return context.dataStore.data.map { it[LINE_SPACING] ?: 1.8f }
    }

    suspend fun setLineSpacing(spacing: Float) {
        context.dataStore.edit { it[LINE_SPACING] = spacing.coerceIn(1.0f, 2.5f) }
    }

    // ========== Padding ==========

    fun getPaddingHorizontalFlow(): Flow<Int> {
        return context.dataStore.data.map { it[PADDING_HORIZONTAL] ?: 20 }
    }

    suspend fun setPaddingHorizontal(padding: Int) {
        context.dataStore.edit { it[PADDING_HORIZONTAL] = padding.coerceIn(12, 32) }
    }

    // ========== Background Mode ==========

    fun getBackgroundModeFlow(): Flow<String> {
        return context.dataStore.data.map { it[BACKGROUND_MODE] ?: "light" }
    }

    suspend fun setBackgroundMode(mode: String) {
        context.dataStore.edit { it[BACKGROUND_MODE] = mode }
    }
}
