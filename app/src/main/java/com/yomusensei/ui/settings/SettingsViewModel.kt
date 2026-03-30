package com.yomusensei.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.api.ProviderType
import com.yomusensei.data.api.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: com.yomusensei.data.local.DictionaryRepository? = null
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _providerType = MutableStateFlow(ProviderType.GEMINI)
    val providerType: StateFlow<ProviderType> = _providerType.asStateFlow()

    private val _openaiCompatApiKey = MutableStateFlow("")
    val openaiCompatApiKey: StateFlow<String> = _openaiCompatApiKey.asStateFlow()

    private val _openaiCompatBaseUrl = MutableStateFlow("https://api.openai.com/v1")
    val openaiCompatBaseUrl: StateFlow<String> = _openaiCompatBaseUrl.asStateFlow()

    private val _openaiCompatModel = MutableStateFlow("gpt-4o-mini")
    val openaiCompatModel: StateFlow<String> = _openaiCompatModel.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _dictionaryWordCount = MutableStateFlow(0)
    val dictionaryWordCount: StateFlow<Int> = _dictionaryWordCount.asStateFlow()

    init {
        viewModelScope.launch {
            _apiKey.value = settingsRepository.getGeminiApiKey()
            _providerType.value = settingsRepository.getProviderType()
            _openaiCompatApiKey.value = settingsRepository.getOpenAICompatApiKey()
            _openaiCompatBaseUrl.value = settingsRepository.getOpenAICompatBaseUrl()
            _openaiCompatModel.value = settingsRepository.getOpenAICompatModel()
            _dictionaryWordCount.value = dictionaryRepository?.getWordCount() ?: 0
        }
    }

    fun updateApiKey(key: String) { _apiKey.value = key }
    fun updateProviderType(type: ProviderType) { _providerType.value = type }
    fun updateOpenAICompatApiKey(key: String) { _openaiCompatApiKey.value = key }
    fun updateOpenAICompatBaseUrl(url: String) { _openaiCompatBaseUrl.value = url }
    fun updateOpenAICompatModel(model: String) { _openaiCompatModel.value = model }

    fun saveSettings() {
        viewModelScope.launch {
            _isSaving.value = true
            settingsRepository.setProviderType(_providerType.value)
            settingsRepository.setGeminiApiKey(_apiKey.value)
            settingsRepository.setOpenAICompatApiKey(_openaiCompatApiKey.value)
            settingsRepository.setOpenAICompatBaseUrl(_openaiCompatBaseUrl.value)
            settingsRepository.setOpenAICompatModel(_openaiCompatModel.value)
            _isSaving.value = false
            _saveSuccess.value = true
        }
    }

    // Legacy alias
    fun saveApiKey() = saveSettings()

    fun resetSaveSuccess() { _saveSuccess.value = false }
}
