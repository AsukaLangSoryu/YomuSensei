package com.yomusensei.ui.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yomusensei.data.model.ReviewResult
import com.yomusensei.data.vocabulary.VocabularyWord

@Composable
fun ReviewScreen(
    viewModel: VocabularyViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.reviewQuestions
    val currentIndex = uiState.currentReviewIndex
    val results = uiState.reviewResults

    if (uiState.isReviewLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无待复习单词 🎉", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onFinish) { Text("返回词库") }
            }
        }
        return
    }

    if (currentIndex >= questions.size) {
        ReviewResultScreen(results = results, onDone = {
            viewModel.resetReview()
            onFinish()
        })
        return
    }

    val question = questions[currentIndex]
    var selectedIndex by remember(currentIndex) { mutableIntStateOf(-1) }
    var showAnswer by remember(currentIndex) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            LinearProgressIndicator(
                progress = currentIndex.toFloat() / questions.size,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "$currentIndex / ${questions.size}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("下面哪个是", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    question.question,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text("的日语写法？", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                val isCorrect = index == question.correctIndex
                val isSelected = index == selectedIndex
                val buttonColors = when {
                    !showAnswer -> ButtonDefaults.buttonColors()
                    isCorrect -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    isSelected && !isCorrect -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                    else -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        if (!showAnswer) {
                            selectedIndex = index
                            showAnswer = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = buttonColors
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (showAnswer) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val isCorrect = selectedIndex == question.correctIndex
                    viewModel.submitReviewAnswer(question.word, isCorrect)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (currentIndex < questions.size - 1) "下一题" else "查看结果")
            }
        } else {
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
fun ReviewResultScreen(results: List<ReviewResult>, onDone: () -> Unit) {
    val correct = results.count { it.isCorrect }
    val total = results.size
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("复习完成！", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "$correct / $total",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text("答对", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        val pct = if (total > 0) correct * 100 / total else 0
        Text(
            when {
                pct >= 80 -> "太棒了！继续保持 🎊"
                pct >= 60 -> "不错，再接再厉！"
                else -> "还需多加练习，加油！"
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("返回词库")
        }
    }
}
