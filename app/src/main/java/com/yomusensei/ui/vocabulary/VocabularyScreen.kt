package com.yomusensei.ui.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yomusensei.data.model.VocabularyStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToHome: () -> Unit = {}
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
                    onNavigateToDetail = onNavigateToDetail,
                    onGoToArticles = onNavigateToHome
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
