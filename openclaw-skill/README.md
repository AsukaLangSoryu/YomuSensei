# YomuSensei OpenClaw Skill

将此目录放入 OpenClaw 的 skills 文件夹即可使用。

## 安装方法

1. 找到你的 OpenClaw skills 目录（通常在 `~/.openclaw/skills/` 或 OpenClaw 安装目录下的 `skills/`）
2. 将 `yomusensei` 文件夹复制进去
3. 重启 OpenClaw

## 在 YomuSensei App 中配置

1. 打开 YomuSensei → 设置
2. 选择 AI 提供商：**兼容 OpenAI**
3. Base URL：`http://你的电脑IP:18789`（点击"预设"选择 `OpenClaw (本地)`，再替换 IP）
4. API Key：OpenClaw 的 Bearer token（在 OpenClaw 设置中查看）
5. 模型名称：`openclaw` 或 `openclaw:main`

## 使用要求

- 手机和电脑处于同一 WiFi 网络
- OpenClaw 服务正在运行
- 已在 OpenClaw config 中开启 HTTP endpoint：
  ```json
  { "gateway": { "http": { "endpoints": { "responses": { "enabled": true } } } } }
  ```

## 功能说明

- 文章推荐：抓取 NHK Web Easy 最新简单日语新闻
- 词汇解释：读音、词性、释义、例句
- 语法问答：解释语法点和句型
- 多轮对话：记住上下文，支持追问
