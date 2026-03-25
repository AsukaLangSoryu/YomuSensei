# YomuSensei - 日语阅读AI助手

## 项目概述
一个 Android 应用，帮助日语学习者阅读日语文章。用户可以让 AI 推荐文章或直接输入网址，阅读时可以查询生词和语法，并自动保存到词库进行智能复习。

## 技术栈
- Kotlin + Jetpack Compose
- 多平台 AI 支持（Gemini / OpenAI 兼容 / OpenClaw 本地）
- Jsoup 网页抓取
- Retrofit + OkHttp 网络请求
- Room Database（词库数据持久化）
- Material3 UI

## 项目结构
```
YomuSensei/app/src/main/java/com/yomusensei/
├── MainActivity.kt          # 主入口（使用 settingsRepo.buildAiProvider()）
├── ui/
│   ├── Navigation.kt        # 页面导航（VocabularyViewModel 在此提升）
│   ├── home/                # 首页（AI对话 + 文章推荐）
│   ├── reader/              # 阅读页面（选词解释 + 提问）
│   ├── vocabulary/          # 词库页面（单词列表 + 复习模式）
│   ├── settings/            # 设置页面（多平台 AI 配置）
│   └── theme/               # 主题配置
└── data/
    ├── api/                 # AI Provider 抽象层
    │   ├── AiProvider.kt        # 接口定义 + UserIntent 枚举
    │   ├── GeminiProvider.kt    # Gemini 实现
    │   ├── OpenAICompatProvider.kt  # OpenAI 兼容实现（DeepSeek/GLM/Kimi/OpenClaw）
    │   └── SettingsRepository.kt    # 多 provider 配置 + buildAiProvider() 工厂
    ├── web/                 # 网页抓取（WebScraper）
    ├── vocabulary/          # 词库数据层（Room Database）
    └── model/               # 数据模型（含 ArticleSearchResult）
```

## 当前状态（2026-03-25 更新）

### 核心功能
- ✅ 基础框架完成
- ✅ 首页 AI 对话功能
- ✅ 直接输入 URL 功能
- ✅ 阅读页面 + 词句解释
- ✅ 多平台 AI 支持（Gemini / OpenAI 兼容 / OpenClaw）
- ✅ 智能意图识别（自动判断用户是要推荐文章/问日语问题/闲聊）
- ✅ 对话模式切换（智能/找文章/聊天 三种模式）
- ✅ 多轮对话历史支持
- ✅ Google Search Grounding（仅 Gemini，解决AI推荐URL不准确问题）

### 网页抓取
- ✅ 支持5个主流日本新闻网站（朝日、读卖、每日、日经、产经）
- ✅ NHK Easy 直接抓取（HTML + JSON 双通道，优先于 AI 搜索）
- ✅ 青空文庫随机作品推荐
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

### OpenClaw 集成
- ✅ `openclaw-skill/SKILL.md` - 日语阅读助手技能定义
- ✅ `openclaw-skill/README.md` - 安装与配置说明
- ✅ App 通过 OpenAI 兼容接口对接本地 OpenClaw

## 待实现功能
- ⏳ 手动添加单词（输入单词，AI自动获取信息）
- ⏳ 标签管理（添加/删除标签、按标签筛选）
- ⏳ 阅读历史记录
- ⏳ 离线模式

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
| OpenClaw | `http://<Tailscale IP>:18789` | `openclaw` |

## OpenClaw 远程访问配置（Tailscale）

> 详见 `docs/guides/openclaw-tailscale-setup.md`

核心流程：
1. 电脑和手机都安装 Tailscale，登同一账号
2. 在 App 设置中填写 Tailscale 分配的电脑 IP（`100.x.x.x:18789`）
3. 使用前确保电脑上 OpenClaw 正在运行

## 设计文档
- `docs/plans/2026-01-19-vocabulary-feature-design.md` - 词库功能设计方案
- `docs/plans/2026-03-25-multi-provider-openclaw-upgrade.md` - 多平台 AI 升级方案
- `docs/guides/openclaw-tailscale-setup.md` - OpenClaw + Tailscale 配置指南

## 用户信息
- 日语水平：初级（N5-N4）
- 学习目标：提升阅读能力，阅读日语文学作品
