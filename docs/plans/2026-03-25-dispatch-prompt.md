# YomuSensei 升级 - 多 Agent 并行执行 Prompt

将以下内容完整粘贴到一个新的 Claude Code 会话中，作为第一条消息发送：

---

## DISPATCH PROMPT（复制以下内容）

```
请阅读 docs/plans/2026-03-25-multi-provider-openclaw-upgrade.md，然后使用 superpowers:dispatching-parallel-agents skill 将以下 3 个独立任务分发给 3 个并行 Agent，每个 Agent 在各自的 git worktree 中工作。

---

**Agent-A 任务：AI Provider 抽象层 + 多平台设置**

阅读方案文档中 "Agent-A 任务" 部分，在此 git 仓库（D:\VScode\.vscode\cursor\YomuSensei）的独立 worktree 中完成以下工作：

1. 创建 AiProvider 接口（app/src/main/java/com/yomusensei/data/api/AiProvider.kt）
2. 将 GeminiRepository 重构为实现 AiProvider 的 GeminiProvider（保留原文件作为兼容别名）
3. 创建 OpenAICompatProvider（覆盖 OpenAI / DeepSeek / GLM / Kimi / OpenClaw）
4. 扩展 SettingsRepository 支持多 provider 配置（providerType, openaiCompatApiKey, baseUrl, model）
5. 更新 SettingsScreen 添加 provider 选择器和 OpenAI 兼容配置区
6. 更新 HomeViewModel 和 ReaderViewModel 使用 AiProvider 接口
7. 更新 MainActivity 使用 settingsRepo.buildAiProvider() 工厂方法

完成后运行 ./gradlew assembleDebug 验证编译通过，提交所有改动。
返回：修改的文件列表 + 编译结果。

---

**Agent-B 任务：文章搜索质量改进 + OpenClaw Skill 文件**

阅读方案文档中 "Agent-B 任务" 部分，在独立 worktree 中完成以下工作：

1. 在 WebScraper.kt 中新增 fetchNhkEasyArticleList() 方法（直接抓取 NHK Easy 主页文章列表）
2. 新增 fetchNhkEasyViaJson() 备用方法（通过 NHK JSON API）
3. 可选：新增 fetchAozoraRandomWorks() 方法
4. 更新 HomeViewModel 的文章请求逻辑：优先使用真实抓取，回退才用 AI 搜索
5. 创建 openclaw-skill/SKILL.md 和 openclaw-skill/README.md

注意：WebScraper.kt 和 HomeViewModel.kt 的改动只加新功能，不修改已有方法签名。
完成后运行 ./gradlew assembleDebug 验证编译通过，提交所有改动。
返回：修改的文件列表 + 编译结果。

---

**Agent-C 任务：词库复习 UI 补全**

阅读方案文档中 "Agent-C 任务" 部分，在独立 worktree 中完成以下工作：

1. 先读取以下现有文件理解当前状态：
   - app/src/main/java/com/yomusensei/ui/vocabulary/ReviewModeTab.kt
   - app/src/main/java/com/yomusensei/ui/vocabulary/VocabularyViewModel.kt
   - app/src/main/java/com/yomusensei/data/vocabulary/VocabularyRepository.kt
   - app/src/main/java/com/yomusensei/data/vocabulary/ReviewScheduler.kt
   - app/src/main/java/com/yomusensei/data/model/Models.kt

2. 创建 ReviewScreen.kt（多选题复习界面 + 结果页面）
3. 创建 WordDetailDialog.kt（单词详情对话框）
4. 更新 VocabularyViewModel 添加 reviewQuestions/currentReviewIndex/reviewResults 状态和相关方法
5. 如果 VocabularyRepository 缺少 getWordsForReview() / updateReviewResult() 方法，根据 ReviewScheduler 逻辑补全
6. 更新 ReviewModeTab.kt 作为复习入口
7. 更新 Navigation.kt 添加 "review" 路由
8. 在 WordListTab.kt 的单词卡片点击时显示 WordDetailDialog

完成后运行 ./gradlew assembleDebug 验证编译通过，提交所有改动。
返回：修改的文件列表 + 编译结果。

---

三个 Agent 并行启动。所有 Agent 完成后，告诉我每个 Agent 的结果摘要，并指导我如何将三个 worktree 的改动合并到主分支（注意 HomeViewModel.kt 在 Agent-A 和 Agent-B 中都有修改，合并时需要手动整合）。
```

---

## 执行前检查清单

1. **开启 HTTP endpoint 权限**（避免子 Agent 反复询问）：
   - 编辑 `.claude/settings.local.json`，添加 bash 权限：
   ```json
   {
     "permissions": {
       "allow": [
         "Bash(./gradlew*)",
         "Bash(git *)"
       ]
     }
   }
   ```

2. **确保 git 工作区干净**：
   ```bash
   git add -A && git commit -m "chore: pre-upgrade snapshot"
   ```

3. **在新终端中启动 Claude Code**，粘贴上方 DISPATCH PROMPT。
