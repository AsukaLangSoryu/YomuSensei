package com.yomusensei.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yomusensei.data.api.PRESET_BASE_URLS
import com.yomusensei.data.api.ProviderType
import com.yomusensei.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val providerType by viewModel.providerType.collectAsState()
    val openaiCompatApiKey by viewModel.openaiCompatApiKey.collectAsState()
    val openaiCompatBaseUrl by viewModel.openaiCompatBaseUrl.collectAsState()
    val openaiCompatModel by viewModel.openaiCompatModel.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showGeminiKey by remember { mutableStateOf(false) }
    var showOpenAIKey by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetSaveSuccess()
        }
    }

    val isOpenClaw = openaiCompatBaseUrl.contains("18789")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Provider 选择卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "AI 提供商",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProviderType.values().forEach { type ->
                            val label = if (type == ProviderType.GEMINI) "Gemini" else "兼容 OpenAI"
                            FilterChip(
                                selected = providerType == type,
                                onClick = { viewModel.updateProviderType(type) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Gemini 配置
            if (providerType == ProviderType.GEMINI) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Gemini API Key",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "请输入你的 Google Gemini API Key",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { viewModel.updateApiKey(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("API Key") },
                            placeholder = { Text("AIza...") },
                            visualTransformation = if (showGeminiKey) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                    Icon(
                                        if (showGeminiKey) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "获取：aistudio.google.com → Get API Key",
                            fontSize = 13.sp,
                            color = TextHint
                        )
                    }
                }
            }

            // OpenAI 兼容配置
            if (providerType == ProviderType.OPENAI_COMPAT) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "OpenAI 兼容配置",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Base URL with preset dropdown
                        Text("Base URL", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = openaiCompatBaseUrl,
                                onValueChange = { viewModel.updateOpenAICompatBaseUrl(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("https://api.openai.com/v1") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Box {
                                OutlinedButton(onClick = { showPresetMenu = true }) {
                                    Text("预设")
                                }
                                DropdownMenu(
                                    expanded = showPresetMenu,
                                    onDismissRequest = { showPresetMenu = false }
                                ) {
                                    PRESET_BASE_URLS.forEach { (name, url) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                viewModel.updateOpenAICompatBaseUrl(url)
                                                showPresetMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // API Key
                        Text("API Key", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = openaiCompatApiKey,
                            onValueChange = { viewModel.updateOpenAICompatApiKey(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("sk-...") },
                            visualTransformation = if (showOpenAIKey) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showOpenAIKey = !showOpenAIKey }) {
                                    Icon(
                                        if (showOpenAIKey) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Model name
                        Text("模型名称", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = openaiCompatModel,
                            onValueChange = { viewModel.updateOpenAICompatModel(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("gpt-4o-mini / deepseek-chat / openclaw") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // OpenClaw tip
                        if (isOpenClaw) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "OpenClaw 模式：请确保电脑上的 OpenClaw 正在运行，且手机和电脑处于同一 WiFi 网络。",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存设置")
                }
            }

            if (saveSuccess) {
                Text(
                    text = "保存成功！",
                    color = Success,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "読む先生 v1.0",
                fontSize = 12.sp,
                color = TextHint,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
