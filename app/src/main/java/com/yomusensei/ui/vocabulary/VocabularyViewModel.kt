package com.yomusensei.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.model.ReviewQuestion
import com.yomusensei.data.model.ReviewResult
import com.yomusensei.data.model.VocabularyStats
import com.yomusensei.data.vocabulary.VocabularyRepository
import com.yomusensei.data.vocabulary.VocabularyWord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VocabularyUiState(
    val reviewQuestions: List<ReviewQuestion> = emptyList(),
    val currentReviewIndex: Int = 0,
    val reviewResults: List<ReviewResult> = emptyList(),
    val isReviewLoading: Boolean = false
)

class VocabularyViewModel(
    private val repository: VocabularyRepository
) : ViewModel() {

    val allWords: StateFlow<List<VocabularyWord>> = repository.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<VocabularyWord>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllWords()
            else repository.searchWords(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<VocabularyStats> = combine(
        repository.getTotalCount(),
        repository.getTodayAddedCount(),
        repository.getPendingReviewCount(),
        repository.getMasteredCount()
    ) { total, today, pending, mastered ->
        VocabularyStats(total, today, pending, mastered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VocabularyStats())

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedWordIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedWordIds: StateFlow<Set<Long>> = _selectedWordIds.asStateFlow()

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun toggleFavorite(word: VocabularyWord) {
        viewModelScope.launch {
            repository.updateWord(word.copy(isFavorite = !word.isFavorite))
        }
    }

    fun deleteWord(word: VocabularyWord) {
        viewModelScope.launch { repository.deleteWord(word) }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
        _selectedWordIds.value = emptySet()
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedWordIds.value = emptySet()
    }

    fun toggleWordSelection(wordId: Long) {
        val current = _selectedWordIds.value
        _selectedWordIds.value = if (wordId in current) current - wordId else current + wordId
    }

    fun deleteSelectedWords() {
        viewModelScope.launch {
            repository.deleteWords(_selectedWordIds.value.toList())
            exitSelectionMode()
        }
    }

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

    // ========== Review ==========

    fun startReview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewLoading = true) }
            val wordsToReview = repository.getWordsForReview()
            val questions = wordsToReview.mapNotNull { word ->
                repository.generateReviewQuestion(word)
            }.shuffled()
            _uiState.update {
                it.copy(
                    reviewQuestions = questions,
                    currentReviewIndex = 0,
                    reviewResults = emptyList(),
                    isReviewLoading = false
                )
            }
        }
    }

    fun submitReviewAnswer(word: VocabularyWord, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.updateReviewResult(word, isCorrect)
            _uiState.update { state ->
                state.copy(
                    currentReviewIndex = state.currentReviewIndex + 1,
                    reviewResults = state.reviewResults + ReviewResult(word.word, isCorrect)
                )
            }
        }
    }

    fun resetReview() {
        _uiState.update {
            it.copy(reviewQuestions = emptyList(), currentReviewIndex = 0, reviewResults = emptyList())
        }
    }
}
