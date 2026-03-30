# YomuSensei - 日语阅读AI助手

## 项目概述
一个 Android 应用，帮助日语学习者阅读日语文章。用户可以让 AI 推荐文章或直接输入网址，阅读时可以查询生词和语法，并自动保存到词库进行智能复习。

## 技术栈
- Kotlin + Jetpack Compose
- 多平台 AI 支持（Gemini / OpenAI 兼容）
- Jsoup 网页抓取
- Retrofit + OkHttp 网络请求
- Room Database（词库数据持久化）
- Material3 UI

## 项目结构
```
YomuSensei/app/src/main/java/com/yomusensei/
├── MainActivity.kt          # 主入口（创建 JishoApiService + ToolExecutor，传给 HomeViewModel）
├── ui/
│   ├── Navigation.kt        # 页面导航（VocabularyViewModel 在此提升）
│   ├── home/                # 首页（AI对话 + 文章推荐）
│   ├── reader/              # 阅读页面（选词解释 + 提问）
│   ├── vocabulary/          # 词库页面（单词列表 + 复习模式）
│   ├── settings/            # 设置页面（多平台 AI 配置）
│   └── theme/               # 主题配置
└── data/
    ├── api/                 # AI Provider 抽象层
    │   ├── AiProvider.kt        # 接口定义 + UserIntent 枚举 + ToolCapableProvider 标记接口
    │   ├── GeminiProvider.kt    # Gemini 实现（含 chatWithTools() 工具调用循环）
    │   ├── OpenAICompatProvider.kt  # OpenAI 兼容实现（DeepSeek/GLM/Kimi 等）
    │   ├── JishoApiService.kt   # Jisho 词典 API（Retrofit）
    │   ├── TavilyApiService.kt  # Tavily 搜索 API（Retrofit）
    │   ├── SettingsRepository.kt    # 多 provider 配置 + 阅读设置持久化
    │   └── tools/
    │       ├── ToolDefinitions.kt   # 4个工具定义（search_japanese_articles/fetch_webpage/lookup_word/save_vocabulary）
    │       └── ToolExecutor.kt      # 工具执行器（调用 Tavily/WebScraper/VocabularyRepository/JishoApiService）
    ├── web/                 # 网页抓取（WebScraper）
    ├── vocabulary/          # 词库数据层（Room Database）
    └── model/               # 数据模型（含 ArticleSearchResult、FunctionCall/Response 等工具模型）
```

## 当前状态（2026-03-30 最终版）

### 核心功能
- ✅ 基础框架完成
- ✅ 首页 AI 对话功能
- ✅ 直接输入 URL 功能
- ✅ 阅读页面 + 词句解释
- ✅ 多平台 AI 支持（Gemini / OpenAI 兼容）
- ✅ 智能意图识别（自动判断用户是要推荐文章/问日语问题/闲聊）
- ✅ 对话模式切换（智能/找文章/聊天 三种模式）
- ✅ 多轮对话历史支持
- ✅ **消息中 URL 可点击**（自动识别并在浏览器打开）
- ✅ **阅读设置面板**（字体/行间距/页边距/背景模式）
- ✅ **固定搜索框**（阅读页顶部，随时可查词）
- ✅ **AI提问保存词库**（提问对话框支持保存单词）

### Function Calling 工具调用（Gemini 专属）
当使用 Gemini 且 AUTO 模式时，HomeViewModel 会走 `handleWithTools()` 路径，AI 可主动调用工具：
- `search_japanese_articles` - **Tavily API 搜索日语文章**（替代原有的 search_nhk_easy/search_aozora）
- `fetch_webpage` - 抓取指定 URL 的网页正文
- `lookup_word` - 通过 Jisho API 查询日语单词读音/词性/释义
- `save_vocabulary` - 将单词保存到词库

**改进**：
- ✅ 集成 Tavily Search API（免费额度 1000次/月）
- ✅ 域名白名单过滤（只搜索 NHK、朝日、每日等可靠网站）
- ✅ 搜索后只返回文章列表，用户点击卡片才抓取内容
- ✅ 避免自动抓取导致的多次失败提示

### 网页抓取与离线阅读（2026-03-30 新增）
- ✅ 支持5个主流日本新闻网站（朝日、读卖、每日、日经、产经）
- ✅ NHK Easy 直接抓取（HTML + JSON 双通道）
- ✅ **青空文庫离线阅读** - 44篇经典文学作品预下载到 assets
  - 夏目漱石 9篇（こころ、坊っちゃん、吾輩は猫である等）
  - 宮沢賢治 8篇（銀河鉄道の夜、注文の多い料理店等）
  - 芥川龍之介 14篇（羅生門、蜘蛛の糸、河童等）
  - 太宰治 6篇（人間失格、走れメロス、斜陽等）
  - 森鴎外、梶井基次郎、坂口安吾等 7篇
- ✅ **本地优先策略** - WebScraper 优先从 assets 读取，网络失败时自动回退
- ✅ **AozoraIndex** - 预设索引支持按标题/作者搜索
- ✅ 付费墙检测和清晰的错误提示

### 词库功能
- ✅ **自动保存** - 阅读时查询的生词自动保存到词库
- ✅ **数据持久化** - Room数据库存储
- ✅ **单词列表** - 显示所有单词，支持搜索和筛选
- ✅ **单词详情** - 点击单词卡片弹出详情对话框（WordDetailDialog）
- ✅ **统计信息** - 总单词数、今日新增、待复习、掌握率
- ✅ **批量操作** - 长按进入选择模式，支持批量删除、标记为已掌握
- ✅ **收藏功能** - 单词卡片支持收藏/取消收藏
- ✅ **复习模式** - 多选题界面 + 答题结果统计（ReviewScreen）
- ✅ **间隔重复算法** - 6级复习系统（ReviewScheduler）
- ✅ **智能干扰项** - 4层回退策略，已修复 bug（返回 word 而非 meaning）

### 词典功能（2026-03-30 新增）
- ✅ **离线词典** - 4931个JLPT N5-N3词条预装到assets
- ✅ **五十音浏览** - 按假名行（あ、か、さ...）筛选词汇
- ✅ **搜索功能** - 支持按单词/读音搜索
- ✅ **混合查询** - 本地词典优先，在线Jisho API补充，自动缓存到数据库
- ✅ **词典统计** - 设置页显示已缓存词条数量
- ✅ **DictionaryBrowserScreen** - 独立词典浏览页面
- ✅ **KanaSelector** - 五十音筛选组件

### 阅读体验优化
- ✅ **字体大小调整** - 14-28sp，步长 2sp
- ✅ **行间距调整** - 1.0x-2.5x，步长 0.2x
- ✅ **页边距调整** - 12-32dp，步长 4dp
- ✅ **背景模式切换** - 浅色/护眼（米黄色）/深色
- ✅ **设置持久化** - DataStore 保存，重启后保持
- ✅ **统一设置面板** - BottomSheet 集中管理所有阅读设置
- ✅ **固定搜索框** - 搜索框固定在文章顶部，无需滚动即可查词
- ✅ **AI提问增强** - 提问对话框支持直接保存单词到词库

### 代码质量优化（2026-03-30）
- ✅ **搜索防抖** - 词典浏览添加300ms防抖，减少数据库查询
- ✅ **性能优化** - 五十音列表改为常量，避免重复计算
- ✅ **资源复用** - Gson实例共享，减少JSON解析开销
- ✅ **代码简化** - 移除冗余注释和不必要的组件嵌套

## 待实现/待优化功能
- ⏳ **AI 记忆系统** - 让 AI 记住用户偏好、学习进度
- ⏳ 手动添加单词（输入单词，AI自动获取信息）
- ⏳ 标签管理（添加/删除标签、按标签筛选）
- ⏳ 阅读历史记录
- ⏳ 导出/导入词库数据

## AI Provider 配置说明

### 切换方式
App 设置页 → 选择「AI 提供商」：
- **Gemini**：填入 Google AI Studio 的 API Key（`aistudio.google.com`）
- **兼容 OpenAI**：填入 Base URL + API Key + 模型名称

### OpenAI 兼容预设
| 平台 | Base URL | 模型示例 |
|------|----------|---------|
| OpenAI | `https://api.openai.com/v1` | `gpt-4o-mini` |
| DeepSeek | `https://api.deepseek.com/v1` | `deepseek-chat` |
| 智谱 GLM | `https://open.bigmodel.cn/api/paas/v4` | `glm-4-flash` |
| Kimi | `https://api.moonshot.cn/v1` | `moonshot-v1-8k` |

## 设计文档
- `docs/plans/2026-01-19-vocabulary-feature-design.md` - 词库功能设计方案
- `docs/plans/2026-03-25-multi-provider-openclaw-upgrade.md` - 多平台 AI 升级方案

## 用户信息
- 日语水平：初级（N5-N4）
- 学习目标：提升阅读能力，阅读日语文学作品
