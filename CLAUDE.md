# YomuSensei - 日语阅读AI助手

## 项目概述
一个 Android 应用，帮助日语学习者阅读日语文章。用户可以让 AI 推荐文章或直接输入网址，阅读时可以查询生词和语法。

## 技术栈
- Kotlin + Jetpack Compose
- Gemini API（需要用户配置 API Key）
- Jsoup 网页抓取
- Retrofit 网络请求

## 项目结构
```
YomuSensei/app/src/main/java/com/yomusensei/
├── MainActivity.kt          # 主入口
├── ui/
│   ├── Navigation.kt        # 页面导航
│   ├── home/                # 首页（AI对话 + 文章推荐）
│   ├── reader/              # 阅读页面（选词解释 + 提问）
│   ├── settings/            # 设置页面（API Key配置）
│   └── theme/               # 主题配置
└── data/
    ├── api/                 # Gemini API 调用
    ├── web/                 # 网页抓取（WebScraper）
    └── model/               # 数据模型
```

## 当前状态
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
- ✅ 支持5个主流日本新闻网站（朝日、读卖、每日、日经、产经）
- ✅ 改进通用网站抓取（扩展选择器 + 智能算法）
- ✅ 付费墙检测和清晰的错误提示

## 已知问题
- ~~AI 推荐的文章 URL 可能不准确（AI 幻觉问题）~~ 已通过 Google Search 解决
- 建议用户直接输入确定的网址（作为备选方案）

## 待优化
- ~~改进网页抓取对更多网站的支持~~ 已完成
- 添加阅读历史记录
- ~~优化 AI 提示词，减少 URL 错误~~ 已完成

## 用户信息
- 日语水平：初级（N5-N4）
- 学习目标：提升阅读能力，阅读日语文学作品
