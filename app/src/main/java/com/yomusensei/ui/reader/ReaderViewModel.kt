package com.yomusensei.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.api.AiProvider
import com.yomusensei.data.api.SettingsRepository
import com.yomusensei.data.model.Article
import com.yomusensei.data.model.TextExplanation
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.vocabulary.VocabularyWord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val geminiRepository: AiProvider,
    private val settingsRepository: SettingsRepository,
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _selectedText = MutableStateFlow<String?>(null)
    val selectedText: StateFlow<String?> = _selectedText.asStateFlow()

    private val _explanation = MutableStateFlow<TextExplanation?>(null)
    val explanation: StateFlow<TextExplanation?> = _explanation.asStateFlow()

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

    /**
     * 选中文本
     */
    fun onTextSelected(text: String) {
        if (text.isBlank()) return

        _selectedText.value = text
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
     * 关闭解释面板
     */
    fun dismissExplanation() {
        _selectedText.value = null
        _explanation.value = null
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
