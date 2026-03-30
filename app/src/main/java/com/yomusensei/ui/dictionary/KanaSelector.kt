package com.yomusensei.ui.dictionary

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val KANA_LIST = listOf(
    "あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ",
    "い", "き", "し", "ち", "に", "ひ", "み", "り",
    "う", "く", "す", "つ", "ぬ", "ふ", "む", "ゆ", "る",
    "え", "け", "せ", "て", "ね", "へ", "め", "れ",
    "お", "こ", "そ", "と", "の", "ほ", "も", "よ", "ろ", "を"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanaSelector(
    selectedKana: String?,
    onKanaSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterChip(
            selected = selectedKana == null,
            onClick = { onKanaSelected(null) },
            label = { Text("全部") }
        )

        KANA_LIST.forEach { kana ->
            FilterChip(
                selected = selectedKana == kana,
                onClick = { onKanaSelected(kana) },
                label = { Text(kana) }
            )
        }
    }
}
