package com.yomusensei.ui.dictionary

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanaSelector(
    selectedKana: String?,
    onKanaSelected: (String?) -> Unit
) {
    val kanaRows = listOf(
        listOf("あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ"),
        listOf("い", "き", "し", "ち", "に", "ひ", "み", "", "り", ""),
        listOf("う", "く", "す", "つ", "ぬ", "ふ", "む", "ゆ", "る", ""),
        listOf("え", "け", "せ", "て", "ね", "へ", "め", "", "れ", ""),
        listOf("お", "こ", "そ", "と", "の", "ほ", "も", "よ", "ろ", "を")
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = selectedKana == null,
                onClick = { onKanaSelected(null) },
                label = { Text("全部") }
            )

            kanaRows.flatten().filter { it.isNotEmpty() }.forEach { kana ->
                FilterChip(
                    selected = selectedKana == kana,
                    onClick = { onKanaSelected(kana) },
                    label = { Text(kana) }
                )
            }
        }
    }
}
