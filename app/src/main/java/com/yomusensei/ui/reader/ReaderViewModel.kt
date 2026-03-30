package com.yomusensei.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.api.AiProvider
import com.yomusensei.data.api.JishoApiService
import com.yomusensei.data.api.SettingsRepository
import com.yomusensei.data.model.Article
import com.yomusensei.data.model.DictionaryEntry
import com.yomusensei.data.model.TextExplanation
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.vocabulary.VocabularyWord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val geminiRepository: AiProvider,
    private val settingsRepository: SettingsRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val jishoService: JishoApiService? = null,
    private val memoryRepository: com.yomusensei.data.api.MemoryRepository? = null,
    private val dictionaryRepository: com.yomusensei.data.local.DictionaryRepository? = null
) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _lineSpacing = MutableStateFlow(1.8f)
    val lineSpacing: StateFlow<Float> = _lineSpacing.asStateFlow()

    private val _paddingHorizontal = MutableStateFlow(20)
    val paddingHorizontal: StateFlow<Int> = _paddingHorizontal.asStateFlow()

    private val _backgroundMode = MutableStateFlow("light")
    val backgroundMode: StateFlow<String> = _backgroundMode.asStateFlow()

    private val _selectedText = MutableStateFlow<String?>(null)
    val selectedText: StateFlow<String?> = _selectedText.asStateFlow()

    private val _explanation = MutableStateFlow<TextExplanation?>(null)
    val explanation: StateFlow<TextExplanation?> = _explanation.asStateFlow()

    private val _dictionaryEntry = MutableStateFlow<DictionaryEntry?>(null)
    val dictionaryEntry: StateFlow<DictionaryEntry?> = _dictionaryEntry.asStateFlow()

    private val _showQuestionDialog = MutableStateFlow(false)
    val showQuestionDialog: StateFlow<Boolean> = _showQuestionDialog.asStateFlow()

    private val _questionAnswer = MutableStateFlow<String?>(null)
    val questionAnswer: StateFlow<String?> = _questionAnswer.asStateFlow()

    private val _isAskingQuestion = MutableStateFlow(false)
    val isAskingQuestion: StateFlow<Boolean> = _isAskingQuestion.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getFontSizeFlow().collect { size ->
                _fontSize.value = size
            }
        }
        viewModelScope.launch {
            settingsRepository.getLineSpacingFlow().collect { spacing ->
                _lineSpacing.value = spacing
            }
        }
        viewModelScope.launch {
            settingsRepository.getPaddingHorizontalFlow().collect { padding ->
                _paddingHorizontal.value = padding
            }
        }
        viewModelScope.launch {
            settingsRepository.getBackgroundModeFlow().collect { mode ->
                _backgroundMode.value = mode
            }
        }
    }

    /**
     * 设置当前文章
     */
    fun setArticle(article: Article) {
        _article.value = article
    }

    /**
     * 调整字体大小
     */
    fun adjustFontSize(delta: Float) {
        viewModelScope.launch {
            val newSize = (_fontSize.value + delta).coerceIn(14f, 28f)
            _fontSize.value = newSize
            settingsRepository.setFontSize(newSize)
        }
    }

    fun adjustLineSpacing(delta: Float) {
        viewModelScope.launch {
            val newSpacing = (_lineSpacing.value + delta).coerceIn(1.0f, 2.5f)
            _lineSpacing.value = newSpacing
            settingsRepository.setLineSpacing(newSpacing)
        }
    }

    fun adjustPadding(delta: Int) {
        viewModelScope.launch {
            val newPadding = (_paddingHorizontal.value + delta).coerceIn(12, 32)
            _paddingHorizontal.value = newPadding
            settingsRepository.setPaddingHorizontal(newPadding)
        }
    }

    fun setBackgroundMode(mode: String) {
        viewModelScope.launch {
            _backgroundMode.value = mode
            settingsRepository.setBackgroundMode(mode)
        }
    }

    /**
     * 选中文本
     */
    fun onTextSelected(text: String) {
        if (text.isBlank()) return

        _selectedText.value = text

        // 优先使用词典仓库（本地+在线）
        if (dictionaryRepository != null) {
            viewModelScope.launch {
                val entry = dictionaryRepository.lookup(text)
                if (entry != null) {
                    _dictionaryEntry.value = entry
                } else {
                    fetchAiExplanation(text)
                }
            }
        } else if (jishoService != null) {
            // 回退到纯在线查询
            viewModelScope.launch {
                val entry = JishoApiService.searchWithCache(jishoService, text)
                if (entry != null) {
                    _dictionaryEntry.value = entry
                } else {
                    fetchAiExplanation(text)
                }
            }
        } else {
            fetchAiExplanation(text)
        }
    }

    private fun fetchAiExplanation(text: String) {
        _explanation.value = TextExplanation(
            selectedText = text,
            explanation = "",
            isLoading = true
        )

        viewModelScope.launch {
            val context = _article.value?.content?.take(500) ?: ""
            val result = geminiRepository.explainText(text, context)

            result.fold(
                onSuccess = { explanation ->
                    _explanation.value = TextExplanation(
                        selectedText = text,
                        explanation = explanation,
                        isLoading = false
                    )

                    // 自动保存到词库
                    viewModelScope.launch {
                        saveToVocabulary(text, explanation)
                    }
                },
                onFailure = { error ->
                    _explanation.value = TextExplanation(
                        selectedText = text,
                        explanation = "获取解释失败：${error.message}",
                        isLoading = false
                    )
                }
            )
        }
    }

    /**
     * 从词典保存到词库
     */
    fun saveDictionaryEntryToVocabulary() {
        val entry = _dictionaryEntry.value ?: return
        viewModelScope.launch {
            val existing = vocabularyRepository.getWordByText(entry.word)
            if (existing != null) return@launch

            val vocabularyWord = VocabularyWord(
                word = entry.word,
                reading = entry.reading,
                meaning = entry.meanings.firstOrNull()?.definitions?.firstOrNull() ?: "",
                explanation = entry.meanings.joinToString("\n") { meaning ->
                    "${meaning.partOfSpeech}: ${meaning.definitions.joinToString(", ")}"
                },
                partOfSpeech = entry.meanings.firstOrNull()?.partOfSpeech,
                category = entry.jlptLevel,
                sourceArticleTitle = _article.value?.title,
                sourceArticleUrl = _article.value?.url,
                addedTime = System.currentTimeMillis(),
                isManuallyAdded = false,
                nextReviewTime = System.currentTimeMillis()
            )

            vocabularyRepository.insertWord(vocabularyWord)
            updateVocabularyMemory()
        }
    }

    /**
     * 关闭解释面板
     */
    fun dismissExplanation() {
        _selectedText.value = null
        _explanation.value = null
        _dictionaryEntry.value = null
    }

    /**
     * 显示提问对话框
     */
    fun showQuestionDialog() {
        _showQuestionDialog.value = true
    }

    /**
     * 隐藏提问对话框
     */
    fun hideQuestionDialog() {
        _showQuestionDialog.value = false
        _questionAnswer.value = null
    }

    /**
     * 提问
     */
    fun askQuestion(question: String) {
        if (question.isBlank()) return

        _isAskingQuestion.value = true

        viewModelScope.launch {
            val articleContext = _article.value?.content?.take(1000) ?: ""
            val result = geminiRepository.askQuestion(question, articleContext)

            result.fold(
                onSuccess = { answer ->
                    _questionAnswer.value = answer
                },
                onFailure = { error ->
                    _questionAnswer.value = "获取回答失败：${error.message}"
                }
            )

            _isAskingQuestion.value = false
        }
    }

    /**
     * 保存单词到词库
     */
    private suspend fun saveToVocabulary(word: String, explanation: String) {
        // 检查是否已存在
        val existing = vocabularyRepository.getWordByText(word)
        if (existing != null) return

        // 解析AI返回的解释
        val parsedInfo = parseExplanation(explanation)

        val vocabularyWord = VocabularyWord(
            word = word,
            reading = parsedInfo.reading,
            meaning = parsedInfo.meaning,
            explanation = explanation,
            partOfSpeech = parsedInfo.partOfSpeech,
            category = parsedInfo.category,
            sourceArticleTitle = _article.value?.title,
            sourceArticleUrl = _article.value?.url,
            addedTime = System.currentTimeMillis(),
            isManuallyAdded = false,
            nextReviewTime = System.currentTimeMillis()
        )

        vocabularyRepository.insertWord(vocabularyWord)
        updateVocabularyMemory()
    }

    private suspend fun updateVocabularyMemory() {
        memoryRepository?.let { repo ->
            val allWords = vocabularyRepository.getAllWords().first()
            val memory = repo.getMemory()
            val updatedMemory = memory.copy(
                learningProgress = memory.learningProgress.copy(
                    vocabularySize = allWords.size
                )
            )
            repo.updateMemory(updatedMemory)
        }
    }

    /**
     * 解析AI返回的解释
     */
    private data class ParsedInfo(
        val reading: String?,
        val meaning: String,
        val partOfSpeech: String?,
        val category: String?
    )

    private fun parseExplanation(explanation: String): ParsedInfo {
        val lines = explanation.lines()
        var reading: String? = null
        var meaning = explanation
        var partOfSpeech: String? = null
        var category: String? = null

        for (line in lines) {
            when {
                line.startsWith("読み：") || line.startsWith("读音：") -> {
                    reading = line.substringAfter("：").trim()
                }
                line.startsWith("意味：") || line.startsWith("意思：") -> {
                    meaning = line.substringAfter("：").trim()
                }
                line.startsWith("词性：") || line.startsWith("品詞：") -> {
                    partOfSpeech = line.substringAfter("：").trim()
                }
                line.startsWith("类别：") || line.startsWith("カテゴリ：") -> {
                    category = line.substringAfter("：").trim()
                }
            }
        }

        return ParsedInfo(reading, meaning, partOfSpeech, category)
    }
}
