# YomuSensei - 日语阅读AI助手

> 一款专为日语学习者设计的智能阅读辅助应用

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)

## 📱 项目简介

YomuSensei（読む先生）通过AI技术帮助用户轻松阅读日语文章，积累词汇，提升阅读能力。

## ✨ 核心功能

### 📚 智能阅读
- AI 推荐日语文章（NHK、朝日新闻等）
- 44篇青空文库经典文学作品离线阅读
- 直接输入 URL 阅读任意日语网页
- 可调节字体、行间距、页边距、背景模式

### 🔍 即时查词
- 固定搜索框，随时查询生词
- 混合词典（4931个JLPT N5-N3词条 + 在线Jisho API）
- 五十音浏览，按假名筛选
- AI 解释词句和语法

### 💬 AI 对话
- 支持多平台AI（Gemini / OpenAI 兼容）
- 智能意图识别（找文章/问问题/闲聊）
- 阅读中提问，AI 结合上下文回答
- 提问结果可保存到词库

### 📖 智能词库
- 查词自动保存
- 间隔重复复习算法（6级系统）
- 多选题复习模式
- 批量管理、收藏、搜索

## 🛠 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **数据库**: Room
- **网络**: Retrofit + OkHttp
- **异步**: Kotlin Coroutines + Flow
- **UI设计**: Material3

## 📦 项目结构

```
YomuSensei/
├── app/src/main/java/com/yomusensei/
│   ├── ui/              # 表示层
│   │   ├── home/        # 首页（AI对话）
│   │   ├── reader/      # 阅读页面
│   │   ├── vocabulary/  # 词库管理
│   │   ├── dictionary/  # 词典浏览
│   │   └── settings/    # 设置
│   ├── data/            # 数据层
│   │   ├── api/         # AI服务
│   │   ├── vocabulary/  # 词库数据
│   │   ├── local/       # 本地词典
│   │   └── web/         # 网页抓取
│   └── model/           # 数据模型
└── docs/                # 文档
    ├── 课程设计报告.md
    ├── UML图设计.md
    └── images/          # UML图片
```

## ⚙️ 配置说明

### Gemini
1. 访问 [Google AI Studio](https://aistudio.google.com)
2. 获取 API Key
3. 在应用设置页填入

### OpenAI 兼容
支持 OpenAI、DeepSeek、智谱GLM、Kimi 等平台：
- 填入 Base URL
- 填入 API Key
- 填入模型名称

## 📚 文档

- [CLAUDE.md](CLAUDE.md) - 详细的项目文档
- [课程设计报告](docs/课程设计报告.md) - 完整的课程设计报告
- [UML图设计](docs/UML图设计.md) - UML图详细说明

## 🎯 开发进度

- ✅ 核心功能完成
- ✅ 性能优化完成
- ✅ 课程设计报告完成
- ✅ 代码质量优化完成

## 👨‍💻 开发者

课程设计项目 - 2026

## 📄 License

本项目仅用于学习交流。
