package com.yomusensei.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.local.DictionaryRepository
import com.yomusensei.data.model.DictionaryEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DictionaryBrowserViewModel(
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedKana = MutableStateFlow<String?>(null)
    val selectedKana: StateFlow<String?> = _selectedKana.asStateFlow()

    val words: StateFlow<List<DictionaryEntry>> = combine(
        _searchQuery.debounce(300),
        _selectedKana
    ) { query, kana ->
        dictionaryRepository.browseWords(query, kana)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectKana(kana: String?) {
        _selectedKana.value = kana
    }
}
