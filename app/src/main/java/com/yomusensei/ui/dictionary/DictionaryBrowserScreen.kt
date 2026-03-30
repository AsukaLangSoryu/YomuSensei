package com.yomusensei.ui.dictionary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yomusensei.data.model.DictionaryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryBrowserScreen(
    viewModel: DictionaryBrowserViewModel,
    onBack: () -> Unit,
    onWordClick: (DictionaryEntry) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val words by viewModel.words.collectAsState()
    val selectedKana by viewModel.selectedKana.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("词典") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索单词或读音") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            KanaSelector(
                selectedKana = selectedKana,
                onKanaSelected = { viewModel.selectKana(it) }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words) { word ->
                    DictionaryWordCard(
                        entry = word,
                        onClick = { onWordClick(word) }
                    )
                }
            }
        }
    }
}
