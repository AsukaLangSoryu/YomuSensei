package com.yomusensei.ui.reader

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val lineSpacing by viewModel.lineSpacing.collectAsState()
    val paddingHorizontal by viewModel.paddingHorizontal.collectAsState()
    val backgroundMode by viewModel.backgroundMode.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("阅读设置", fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))

            // 字体大小
            Text("字体大小: ${fontSize.toInt()}sp", fontSize = 14.sp)
            Slider(
                value = fontSize,
                onValueChange = { viewModel.adjustFontSize(it - fontSize) },
                valueRange = 14f..28f,
                steps = 6
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 行间距
            Text("行间距: ${"%.1f".format(lineSpacing)}x", fontSize = 14.sp)
            Slider(
                value = lineSpacing,
                onValueChange = { viewModel.adjustLineSpacing(it - lineSpacing) },
                valueRange = 1.0f..2.5f,
                steps = 14
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 页边距
            Text("页边距: ${paddingHorizontal}dp", fontSize = 14.sp)
            Slider(
                value = paddingHorizontal.toFloat(),
                onValueChange = { viewModel.adjustPadding(it.toInt() - paddingHorizontal) },
                valueRange = 12f..32f,
                steps = 4
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 背景模式
            Text("背景模式", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = backgroundMode == "light",
                    onClick = { viewModel.setBackgroundMode("light") },
                    label = { Text("浅色") }
                )
                FilterChip(
                    selected = backgroundMode == "sepia",
                    onClick = { viewModel.setBackgroundMode("sepia") },
                    label = { Text("护眼") }
                )
                FilterChip(
                    selected = backgroundMode == "dark",
                    onClick = { viewModel.setBackgroundMode("dark") },
                    label = { Text("深色") }
                )
            }
        }
    }
}
