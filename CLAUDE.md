# YomuSensei - 日语阅读AI助手

## 项目概述
一个 Android 应用，帮助日语学习者阅读日语文章。用户可以让 AI 推荐文章或直接输入网址，阅读时可以查询生词和语法，并自动保存到词库进行智能复习。

## 技术栈
- Kotlin + Jetpack Compose
- Gemini API（需要用户配置 API Key）
- Jsoup 网页抓取
- Retrofit 网络请求
- Room Database（词库数据持久化）
- Material3 UI

## 项目结构
```
YomuSensei/app/src/main/java/com/yomusensei/
├── MainActivity.kt          # 主入口
├── ui/
│   ├── Navigation.kt        # 页面导航
│   ├── home/                # 首页（AI对话 + 文章推荐）
│   ├── reader/              # 阅读页面（选词解释 + 提问）
│   ├── vocabulary/          # 词库页面（单词列表 + 复习模式）
│   ├── settings/            # 设置页面（API Key配置）
│   └── theme/               # 主题配置
└── data/
    ├── api/                 # Gemini API 调用
    ├── web/                 # 网页抓取（WebScraper）
    ├── vocabulary/          # 词库数据层（Room Database）
    └── model/               # 数据模型
```

## 当前状态

### 核心功能
- ✅ 基础框架完成
- ✅ 首页 AI 对话功能
- ✅ 直接输入 URL 功能
- ✅ 阅读页面 + 词句解释
- ✅ 设置页面（Gemini API Key）
- ✅ 已切换到 Gemini API
- ✅ 智能意图识别（自动判断用户是要推荐文章/问日语问题/闲聊）
- ✅ 对话模式切换（智能/找文章/聊天 三种模式）
- ✅ 多轮对话历史支持
- ✅ Google Search Grounding（解决AI推荐URL不准确问题）
- ✅ NHK News Easy 真实文章列表抓取
- ✅ 修复：AI 搜索文章时正确返回结果（不再只说"我会帮你寻找"）

### 网页抓取
- ✅ 支持5个主流日本新闻网站（朝日、读卖、每日、日经、产经）
- ✅ 改进通用网站抓取（扩展选择器 + 内容密度算法）
- ✅ 付费墙检测和清晰的错误提示
- ✅ NHK Easy 和青空文庫专用抓取器

### 词库功能（2026-01-19 完成）
- ✅ **自动保存** - 阅读时查询的生词自动保存到词库
- ✅ **数据持久化** - Room数据库存储（VocabularyWord, WordTag, ReviewSession, CachedQuestion）
- ✅ **单词列表** - 显示所有单词，支持搜索和筛选
- ✅ **统计信息** - 总单词数、今日新增、待复习、掌握率
- ✅ **批量操作** - 长按进入选择模式，支持批量删除、标记为已掌握
- ✅ **收藏功能** - 单词卡片支持收藏/取消收藏
- ✅ **复习模式入口** - 显示待复习数量，准备开始复习
- ✅ **间隔重复算法** - 6级复习系统（0: 立即, 1: 1天, 2: 3天, 3: 7天, 4: 30天, 5: 90天）
- ✅ **智能干扰项** - 4层回退策略（同词性同类别 → 同词性 → 相似长度 → 随机）
- ✅ **导航集成** - 词库页面已集成到底部导航栏

### 词库技术实现
- **数据层**：
  - `VocabularyEntities.kt` - Room实体定义
  - `VocabularyDao.kt` - 数据访问接口（CRUD、搜索、统计、智能查询）
  - `VocabularyDatabase.kt` - Room数据库配置
  - `VocabularyRepository.kt` - 业务逻辑封装
  - `ReviewScheduler.kt` - 间隔重复算法实现

- **UI层**：
  - `VocabularyViewModel.kt` - 状态管理（搜索、批量选择、统计）
  - `VocabularyScreen.kt` - 主页面（Tab切换、统计卡片）
  - `WordListTab.kt` - 单词列表（搜索框、单词卡片、批量操作）
  - `ReviewModeTab.kt` - 复习模式入口

- **集成**：
  - `ReaderViewModel.kt` - 添加自动保存功能
  - `MainActivity.kt` - 初始化VocabularyRepository
  - `Navigation.kt` - 添加词库路由

## 待实现功能

### 词库功能（下一步）
- ⏳ 单词详情页面（完整信息、复习历史、编辑/删除）
- ⏳ 复习页面（选择题界面、答题反馈、结果统计）
- ⏳ 手动添加单词对话框（输入单词，AI自动获取信息）
- ⏳ 预生成服务（后台批量生成干扰项并缓存，解决AI延迟问题）
- ⏳ 标签管理（添加/删除标签、按标签筛选）

### 其他优化
- ⏳ 阅读历史记录
- ⏳ 更多网站支持
- ⏳ 离线模式

## 已知问题
- ~~AI 推荐的文章 URL 可能不准确（AI 幻觉问题）~~ 已通过 Google Search 解决
- 建议用户直接输入确定的网址（作为备选方案）

## 设计文档
- `docs/plans/2026-01-19-vocabulary-feature-design.md` - 词库功能完整设计方案
- `docs/plans/2026-01-19-vocabulary-feature-implementation.md` - 数据层实现计划
- `docs/plans/2026-01-19-vocabulary-ui-implementation.md` - UI层实现计划

## 用户信息
- 日语水平：初级（N5-N4）
- 学习目标：提升阅读能力，阅读日语文学作品
