package com.yomusensei.ui.reader

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yomusensei.data.model.Article
import com.yomusensei.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    article: Article,
    onBack: () -> Unit
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val lineSpacing by viewModel.lineSpacing.collectAsState()
    val paddingHorizontal by viewModel.paddingHorizontal.collectAsState()
    val backgroundMode by viewModel.backgroundMode.collectAsState()
    val explanation by viewModel.explanation.collectAsState()
    val dictionaryEntry by viewModel.dictionaryEntry.collectAsState()
    val showQuestionDialog by viewModel.showQuestionDialog.collectAsState()
    val questionAnswer by viewModel.questionAnswer.collectAsState()
    val isAskingQuestion by viewModel.isAskingQuestion.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(article) {
        viewModel.setArticle(article)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        article.title,
                        maxLines = 1,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showQuestionDialog() },
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = "提问")
            }
        },
        containerColor = when (backgroundMode) {
            "dark" -> Color(0xFF1E1E1E)
            "sepia" -> Color(0xFFF4ECD8)
            else -> Background
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 文章内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = paddingHorizontal.dp, vertical = 16.dp)
            ) {
                // 标题
                Text(
                    text = article.title,
                    fontSize = (fontSize + 4).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (backgroundMode == "dark") Color.White else OnBackground,
                    lineHeight = (fontSize * lineSpacing + 4).sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 正文（可选择文本）
                SelectableText(
                    text = article.content,
                    fontSize = fontSize,
                    lineSpacing = lineSpacing,
                    textColor = if (backgroundMode == "dark") Color.White else OnSurface,
                    onTextSelected = { selectedText ->
                        viewModel.onTextSelected(selectedText)
                    }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }

            // 词典面板
            dictionaryEntry?.let { entry ->
                WordLookupSheet(
                    entry = entry,
                    onSave = {
                        viewModel.saveDictionaryEntryToVocabulary()
                        viewModel.dismissExplanation()
                    },
                    onDismiss = { viewModel.dismissExplanation() }
                )
            }

            // 解释面板
            explanation?.let { exp ->
                ExplanationPanel(
                    explanation = exp,
                    onDismiss = { viewModel.dismissExplanation() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 提问对话框
            if (showQuestionDialog) {
                QuestionDialog(
                    answer = questionAnswer,
                    isLoading = isAskingQuestion,
                    onAsk = { viewModel.askQuestion(it) },
                    onDismiss = { viewModel.hideQuestionDialog() }
                )
            }

            if (showSettings) {
                ReaderSettingsSheet(
                    viewModel = viewModel,
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

@Composable
fun SelectableText(
    text: String,
    fontSize: Float,
    lineSpacing: Float = 1.8f,
    textColor: Color = OnSurface,
    onTextSelected: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    SelectionContainer {
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = textColor,
            lineHeight = (fontSize * lineSpacing).sp,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // 长按时会触发系统选择
                    }
                )
            }
        )
    }

    // 提示用户如何使用
    Text(
        text = "长按选择文字，然后点击下方\"解释\"按钮",
        fontSize = 12.sp,
        color = TextHint,
        modifier = Modifier.padding(top = 8.dp)
    )

    // 解释按钮
    var inputText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        label = { Text("输入要查询的词句") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        trailingIcon = {
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onTextSelected(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "查询",
                    tint = if (inputText.isNotBlank()) Primary else TextHint
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ExplanationPanel(
    explanation: com.yomusensei.data.model.TextExplanation,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        color = Surface,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "「${explanation.selectedText}」",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容
            if (explanation.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                Text(
                    text = explanation.explanation,
                    fontSize = 15.sp,
                    color = OnSurface,
                    lineHeight = 24.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
fun QuestionDialog(
    answer: String?,
    isLoading: Boolean,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var question by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("向AI提问") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("你的问题") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 2,
                    maxLines = 4
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                answer?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = SurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(12.dp)
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAsk(question) },
                enabled = question.isNotBlank() && !isLoading
            ) {
                Text("提问")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
