package com.yomusensei.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.model.VocabularyStats
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.vocabulary.VocabularyWord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VocabularyViewModel(
    private val repository: VocabularyRepository
) : ViewModel() {

    // 所有单词列表
    val allWords: StateFlow<List<VocabularyWord>> = repository.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 搜索结果
    val searchResults: StateFlow<List<VocabularyWord>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllWords()
            } else {
                repository.searchWords(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 统计信息
    val stats: StateFlow<VocabularyStats> = combine(
        repository.getTotalCount(),
        repository.getTodayAddedCount(),
        repository.getPendingReviewCount(),
        repository.getMasteredCount()
    ) { total, today, pending, mastered ->
        VocabularyStats(total, today, pending, mastered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VocabularyStats())

    // 批量选择模式
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedWordIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedWordIds: StateFlow<Set<Long>> = _selectedWordIds.asStateFlow()

    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(word: VocabularyWord) {
        viewModelScope.launch {
            repository.updateWord(word.copy(isFavorite = !word.isFavorite))
        }
    }

    /**
     * 删除单词
     */
    fun deleteWord(word: VocabularyWord) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }

    /**
     * 进入批量选择模式
     */
    fun enterSelectionMode() {
        _isSelectionMode.value = true
        _selectedWordIds.value = emptySet()
    }

    /**
     * 退出批量选择模式
     */
    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedWordIds.value = emptySet()
    }

    /**
     * 切换单词选择状态
     */
    fun toggleWordSelection(wordId: Long) {
        val current = _selectedWordIds.value
        _selectedWordIds.value = if (wordId in current) {
            current - wordId
        } else {
            current + wordId
        }
    }

    /**
     * 批量删除选中的单词
     */
    fun deleteSelectedWords() {
        viewModelScope.launch {
            repository.deleteWords(_selectedWordIds.value.toList())
            exitSelectionMode()
        }
    }

    /**
     * 批量标记为已掌握
     */
    fun markSelectedAsMastered() {
        viewModelScope.launch {
            val words = _selectedWordIds.value.mapNotNull { id ->
                allWords.value.find { it.id == id }
            }
            words.forEach { word ->
                repository.updateWord(
                    word.copy(
                        reviewLevel = 5,
                        nextReviewTime = System.currentTimeMillis() + 90L * 24 * 3600 * 1000
                    )
                )
            }
            exitSelectionMode()
        }
    }
}
