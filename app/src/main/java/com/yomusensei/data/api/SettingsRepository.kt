package com.yomusensei.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val FONT_SIZE = floatPreferencesKey("font_size")
    }

    // API Key
    suspend fun getApiKey(): String {
        return context.dataStore.data.first()[API_KEY] ?: ""
    }

    suspend fun setApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    fun getApiKeyFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[API_KEY] ?: ""
        }
    }

    // 字体大小
    suspend fun getFontSize(): Float {
        return context.dataStore.data.first()[FONT_SIZE] ?: 18f
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    fun getFontSizeFlow(): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[FONT_SIZE] ?: 18f
        }
    }
}
