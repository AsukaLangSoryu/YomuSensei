package com.yomusensei.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yomusensei.data.model.Article
import com.yomusensei.data.model.ChatMessage
import com.yomusensei.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToReader: (Article) -> Unit,
    onNavigateToVocabulary: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedArticle by viewModel.selectedArticle.collectAsState()
    val isLoadingArticle by viewModel.isLoadingArticle.collectAsState()
    val chatMode by viewModel.chatMode.collectAsState()

    var showUrlDialog by remember { mutableStateOf(false) }

    // 当文章加载完成时，导航到阅读页
    LaunchedEffect(selectedArticle) {
        selectedArticle?.let { article ->
            if (article.content.isNotBlank()) {
                onNavigateToReader(article)
                viewModel.clearSelectedArticle()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "読む先生",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    // 直接输入URL按钮
                    IconButton(onClick = { showUrlDialog = true }) {
                        Icon(Icons.Default.Link, contentDescription = "输入网址")
                    }
                    // 词库按钮
                    IconButton(onClick = onNavigateToVocabulary) {
                        Icon(Icons.Default.MenuBook, contentDescription = "词库")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = OnSurface
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 模式切换按钮
            ChatModeSelector(
                currentMode = chatMode,
                onModeChange = { viewModel.setChatMode(it) }
            )

            // 消息列表
            val listState = rememberLazyListState()
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onArticleClick = { article ->
                            viewModel.selectArticle(article)
                        }
                    )
                }

                if (isLoading) {
                    item {
                        LoadingIndicator()
                    }
                }
            }

            // 加载文章提示
            if (isLoadingArticle) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary
                )
            }

            // 输入框
            ChatInput(
                onSend = { viewModel.sendMessage(it) },
                enabled = !isLoading && !isLoadingArticle
            )
        }

        // URL输入对话框
        if (showUrlDialog) {
            UrlInputDialog(
                onDismiss = { showUrlDialog = false },
                onConfirm = { url ->
                    showUrlDialog = false
                    viewModel.loadArticleFromUrl(url)
                }
            )
        }
    }
}

@Composable
fun UrlInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入日语文章网址") },
        text = {
            Column {
                Text(
                    "直接粘贴你想阅读的日语网页URL",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "推荐网站：\n• NHK Easy News\n• 青空文库\n• 日语维基百科",
                    fontSize = 12.sp,
                    color = TextHint
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank() && url.startsWith("http")
            ) {
                Text("打开")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onArticleClick: (Article) -> Unit
) {
    val context = LocalContext.current
    val annotatedText = buildAnnotatedStringWithUrls(message.content, message.isFromUser)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    )
                )
                .background(if (message.isFromUser) Primary else Surface)
                .padding(12.dp)
        ) {
            ClickableText(
                text = annotatedText,
                style = LocalTextStyle.current.copy(
                    color = if (message.isFromUser) OnPrimary else OnSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                onClick = { offset ->
                    annotatedText.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                }
            )
        }

        // 文章列表
        message.articles?.let { articles ->
            if (articles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                articles.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = article.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Primary
            )
            if (article.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (article.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.source,
                    fontSize = 11.sp,
                    color = TextHint
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun ChatInput(
    onSend: (String) -> Unit,
    enabled: Boolean
) {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("告诉我你想读什么...") },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onSend(text)
                            text = ""
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (text.isNotBlank() && enabled) Primary else Color.LightGray)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "发送",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatModeSelector(
    currentMode: ChatMode,
    onModeChange: (ChatMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeChip(
            label = "智能",
            icon = Icons.Default.AutoAwesome,
            selected = currentMode == ChatMode.AUTO,
            onClick = { onModeChange(ChatMode.AUTO) }
        )
        ModeChip(
            label = "找文章",
            icon = Icons.Default.Article,
            selected = currentMode == ChatMode.ARTICLE,
            onClick = { onModeChange(ChatMode.ARTICLE) }
        )
        ModeChip(
            label = "聊天",
            icon = Icons.Default.Chat,
            selected = currentMode == ChatMode.FREE_CHAT,
            onClick = { onModeChange(ChatMode.FREE_CHAT) }
        )
    }
}

@Composable
fun ModeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Primary else Surface,
        contentColor = if (selected) OnPrimary else OnSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp
            )
        }
    }
}

private fun buildAnnotatedStringWithUrls(text: String, isFromUser: Boolean): AnnotatedString {
    val urlPattern = "https?://[^\\s]+".toRegex()
    return buildAnnotatedString {
        var lastIndex = 0
        urlPattern.findAll(text).forEach { match ->
            append(text.substring(lastIndex, match.range.first))
            pushStyle(SpanStyle(color = if (isFromUser) Color(0xFFBBDEFB) else Color(0xFF2196F3), textDecoration = TextDecoration.Underline))
            addStringAnnotation("URL", match.value, match.range.first, match.range.last + 1)
            append(match.value)
            pop()
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }
}
