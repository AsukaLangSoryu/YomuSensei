package com.yomusensei.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yomusensei.data.local.DictionaryRepository
import com.yomusensei.data.model.DictionaryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DictionaryBrowserViewModel(
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedKana = MutableStateFlow<String?>(null)
    val selectedKana: StateFlow<String?> = _selectedKana.asStateFlow()

    private val _words = MutableStateFlow<List<DictionaryEntry>>(emptyList())
    val words: StateFlow<List<DictionaryEntry>> = _words.asStateFlow()

    init {
        loadWords()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadWords()
    }

    fun selectKana(kana: String?) {
        _selectedKana.value = kana
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            val query = _searchQuery.value
            val kana = _selectedKana.value
            _words.value = dictionaryRepository.browseWords(query, kana)
        }
    }
}
