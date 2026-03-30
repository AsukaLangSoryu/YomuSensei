package com.yomusensei.ui.vocabulary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yomusensei.data.vocabulary.ReviewScheduler
import com.yomusensei.data.vocabulary.VocabularyWord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListTab(
    viewModel: VocabularyViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onGoToArticles: () -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val words by viewModel.searchResults.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedWordIds.collectAsState()

    var detailWord by remember { mutableStateOf<com.yomusensei.data.vocabulary.VocabularyWord?>(null) }

    detailWord?.let { word ->
        WordDetailDialog(
            word = word,
            onDismiss = { detailWord = null },
            onDelete = { viewModel.deleteWord(it) }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索框
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // 单词列表
        if (words.isEmpty()) {
            if (searchQuery.isEmpty()) {
                EmptyVocabularyState(onGoToArticles = onGoToArticles)
            } else {
                EmptyState()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    WordCard(
                        word = word,
                        isSelected = word.id in selectedIds,
                        isSelectionMode = isSelectionMode,
                        onToggleFavorite = { viewModel.toggleFavorite(word) },
                        onClick = {
                            if (isSelectionMode) {
                                viewModel.toggleWordSelection(word.id)
                            } else {
                                detailWord = word
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                viewModel.enterSelectionMode()
                                viewModel.toggleWordSelection(word.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索单词或意思") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "清除")
                }
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WordCard(
    word: VocabularyWord,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择框或收藏图标
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            } else {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (word.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "收藏",
                        tint = if (word.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 单词信息
            Column(modifier = Modifier.weight(1f)) {
                // 单词和读音
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (word.reading != null) {
                        Text(
                            text = " (${word.reading})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 意思
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 底部信息
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 来源
                    if (word.sourceArticleTitle != null) {
                        Text(
                            text = word.sourceArticleTitle!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text("|", style = MaterialTheme.typography.bodySmall)
                    }

                    // 等级
                    Text(
                        text = ReviewScheduler.getLevelDescription(word.reviewLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 上次复习时间
                    if (word.lastReviewTime != null) {
                        Text("|", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = formatTime(word.lastReviewTime!!),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 复习进度圆环
            CircularProgressIndicator(
                progress = word.reviewLevel / 5f,
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "还没有单词",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "开始阅读文章，查询生词会自动保存到这里",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (24 * 3600 * 1000)

    return when {
        days == 0L -> "今天"
        days == 1L -> "昨天"
        days < 7 -> "${days}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
