package com.yomusensei.ui.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yomusensei.data.vocabulary.VocabularyWord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WordDetailDialog(
    word: VocabularyWord,
    onDismiss: () -> Unit,
    onDelete: (VocabularyWord) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(word.word, style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!word.reading.isNullOrBlank()) {
                    Text("读音：${word.reading}", style = MaterialTheme.typography.bodyLarge)
                }
                Text("释义：${word.meaning}", style = MaterialTheme.typography.bodyMedium)
                if (!word.partOfSpeech.isNullOrBlank()) {
                    Text("词性：${word.partOfSpeech}", style = MaterialTheme.typography.bodySmall)
                }
                Divider()
                Text(
                    "复习级别：${word.reviewLevel} / 5",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "加入时间：${formatDate(word.addedTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!word.sourceArticleUrl.isNullOrBlank()) {
                    Text(
                        "来源：${word.sourceArticleUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
        dismissButton = {
            TextButton(
                onClick = { onDelete(word); onDismiss() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("删除") }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}
