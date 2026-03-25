# 词库功能UI层实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现词库功能的完整UI层，包括词库页面、单词详情、复习模式和导航集成

**Architecture:** 使用Jetpack Compose构建UI，MVVM架构，集成到现有导航系统

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Navigation Compose, ViewModel

---

## Task 1: 创建VocabularyViewModel

**Files:**
- Create: `app/src/main/java/com/yomusensei/ui/vocabulary/VocabularyViewModel.kt`

**Step 1: 创建ViewModel文件**

```kotlin
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
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/ui/vocabulary/VocabularyViewModel.kt
git commit -m "feat: add vocabulary view model"
```

---

## Task 2: 创建词库主页面

**Files:**
- Create: `app/src/main/java/com/yomusensei/ui/vocabulary/VocabularyScreen.kt`

**Step 1: 创建页面文件**

```kotlin
package com.yomusensei.ui.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToReview: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val stats by viewModel.stats.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    viewModel = viewModel,
                    onExitSelection = { viewModel.exitSelectionMode() }
                )
            } else {
                TopAppBar(
                    title = { Text("词库") },
                    actions = {
                        IconButton(onClick = { /* TODO: 添加单词 */ }) {
                            Icon(Icons.Default.Add, "添加单词")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 统计卡片
            StatisticsCard(stats = stats)

            // Tab切换
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("单词列表") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("复习模式") }
                )
            }

            // Tab内容
            when (selectedTab) {
                0 -> WordListTab(
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail
                )
                1 -> ReviewModeTab(
                    stats = stats,
                    onStartReview = onNavigateToReview
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(stats: VocabularyStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("总单词", stats.totalCount.toString())
            StatItem("今日新增", stats.todayAdded.toString())
            StatItem("待复习", stats.pendingReview.toString())
            StatItem("掌握率", "${(stats.masteryRate * 100).toInt()}%")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    viewModel: VocabularyViewModel,
    onExitSelection: () -> Unit
) {
    val selectedCount by viewModel.selectedWordIds.collectAsState()

    TopAppBar(
        title = { Text("已选择 ${selectedCount.size} 个") },
        navigationIcon = {
            IconButton(onClick = onExitSelection) {
                Icon(Icons.Default.Close, "取消")
            }
        },
        actions = {
            IconButton(onClick = { viewModel.markSelectedAsMastered() }) {
                Icon(Icons.Default.Check, "标记为已掌握")
            }
            IconButton(onClick = { viewModel.deleteSelectedWords() }) {
                Icon(Icons.Default.Delete, "删除")
            }
        }
    )
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/ui/vocabulary/VocabularyScreen.kt
git commit -m "feat: add vocabulary main screen with tabs"
```

---

## Task 3: 创建单词列表Tab

**Files:**
- Create: `app/src/main/java/com/yomusensei/ui/vocabulary/WordListTab.kt`

**Step 1: 创建Tab文件**

```kotlin
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
    onNavigateToDetail: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val words by viewModel.searchResults.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedWordIds.collectAsState()

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
            EmptyState()
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
                                onNavigateToDetail(word.id)
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
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/ui/vocabulary/WordListTab.kt
git commit -m "feat: add word list tab with search and selection"
```

---

## Task 4: 创建复习模式Tab

**Files:**
- Create: `app/src/main/java/com/yomusensei/ui/vocabulary/ReviewModeTab.kt`

**Step 1: 创建Tab文件**

```kotlin
package com.yomusensei.ui.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yomusensei.data.model.VocabularyStats

@Composable
fun ReviewModeTab(
    stats: VocabularyStats,
    onStartReview: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // 待复习数量
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stats.pendingReview}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "个单词待复习",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 开始复习按钮
            Button(
                onClick = onStartReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = stats.pendingReview > 0
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始复习", style = MaterialTheme.typography.titleMedium)
            }

            // 提示信息
            if (stats.pendingReview == 0) {
                Text(
                    text = "太棒了！暂时没有需要复习的单词",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "复习模式使用选择题测试，帮助你巩固记忆",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/yomusensei/ui/vocabulary/ReviewModeTab.kt
git commit -m "feat: add review mode tab"
```

---

## Task 5: 更新导航添加词库路由

**Files:**
- Modify: `app/src/main/java/com/yomusensei/ui/Navigation.kt`

**Step 1: 添加词库路由**

在sealed class Screen中添加：
```kotlin
object Vocabulary : Screen("vocabulary")
object VocabularyDetail : Screen("vocabulary/{wordId}") {
    fun createRoute(wordId: Long) = "vocabulary/$wordId"
}
object Review : Screen("review")
```

**Step 2: 在NavHost中添加词库导航**

```kotlin
composable(Screen.Vocabulary.route) {
    val viewModel: VocabularyViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = VocabularyDatabase.getDatabase(context)
                val repository = VocabularyRepository(database.vocabularyDao())
                @Suppress("UNCHECKED_CAST")
                return VocabularyViewModel(repository) as T
            }
        }
    )
    VocabularyScreen(
        viewModel = viewModel,
        onNavigateToDetail = { wordId ->
            navController.navigate(Screen.VocabularyDetail.createRoute(wordId))
        },
        onNavigateToReview = {
            navController.navigate(Screen.Review.route)
        }
    )
}
```

**Step 3: 在底部导航栏添加词库入口**

在BottomNavigationBar中添加词库图标。

**Step 4: 添加必要的imports**

```kotlin
import com.yomusensei.ui.vocabulary.VocabularyScreen
import com.yomusensei.ui.vocabulary.VocabularyViewModel
import com.yomusensei.data.vocabulary.VocabularyDatabase
import com.yomusensei.data.vocabulary.VocabularyRepository
```

**Step 5: Commit**

```bash
git add app/src/main/java/com/yomusensei/ui/Navigation.kt
git commit -m "feat: add vocabulary navigation routes"
```

---

## 总结

这个计划实现了词库功能的基础UI层：

1. ✅ VocabularyViewModel - 状态管理和业务逻辑
2. ✅ VocabularyScreen - 主页面框架
3. ✅ WordListTab - 单词列表、搜索、批量操作
4. ✅ ReviewModeTab - 复习模式入口
5. ✅ Navigation - 路由集成

**下一步需要实现：**
- 单词详情页面
- 复习页面（选择题界面）
- 手动添加单词对话框
- 预生成服务

由于计划已经比较长，这些功能将在下一个计划中实现。
