# OpenClaw + Tailscale 配置指南

本指南解决的问题：让手机在**任何网络环境**下都能访问电脑上运行的 OpenClaw。

---

## 一、整体架构

```
手机（YomuSensei App）
    │  HTTP 请求（OpenAI 兼容格式）
    │  通过 Tailscale 虚拟局域网
    ▼
电脑（OpenClaw，端口 18789）
    │
    ▼
OpenClaw Agent（加载了 yomusensei skill）
```

---

## 二、Tailscale 安装与配置

### 2.1 注册账号
前往 [tailscale.com](https://tailscale.com) 注册，用 Google / GitHub / 微软账号均可，免费计划够用。

### 2.2 电脑端（Windows）
1. 下载并安装 Tailscale for Windows
2. 安装完成后用你的账号登录
3. 右键系统托盘的 Tailscale 图标 → 点击电脑名称，可以看到分配的 IP
   - 格式为 `100.x.x.x`，这就是你的 **Tailscale IP**，记下来

### 2.3 手机端（Android）
1. 安装 Tailscale App（Google Play 或 APK）
2. 登录**同一个账号**
3. 打开 VPN 开关
4. 在设备列表里能看到你的电脑节点显示 `online` 即为成功

### 2.4 验证连通
手机打开浏览器，访问 `http://100.x.x.x:18789`（替换为你的 Tailscale IP）。
- 如果 OpenClaw 在运行，会看到 API 响应
- 如果超时，检查 OpenClaw 是否已启动，以及 Windows 防火墙是否放行了 18789 端口

---

## 三、OpenClaw 配置

### 3.1 安装 YomuSensei Skill
将项目根目录的 `openclaw-skill/` 文件夹（含 `SKILL.md`）复制到：
```
C:\Users\你的用户名\.openclaw\workspace\skills\yomusensei\
```
复制后目录结构应为：
```
.openclaw\workspace\skills\
└── yomusensei\
    ├── SKILL.md
    └── README.md
```
复制完成后重启 OpenClaw。

### 3.2 开启 HTTP Endpoint
在 OpenClaw 的 config 文件中确认已开启 HTTP API：
```json
{
  "gateway": {
    "http": {
      "endpoints": {
        "responses": {
          "enabled": true
        }
      }
    }
  }
}
```
端口默认为 `18789`，一般不需要修改。

### 3.3 获取 Bearer Token
在 OpenClaw 设置中找到 API Token / Bearer Token，复制备用。

---

## 四、YomuSensei App 配置

打开 App → 右上角设置图标 → 进入设置页面：

| 配置项 | 填写内容 |
|--------|---------|
| AI 提供商 | 选择「兼容 OpenAI」 |
| Base URL | `http://100.x.x.x:18789`（替换为你的 Tailscale IP） |
| API Key | OpenClaw 的 Bearer Token |
| 模型名称 | `openclaw` 或 `openclaw:main` |

点击「保存设置」。

> **注意**：Base URL 中填 Tailscale IP（`100.x.x.x`），不是本地局域网 IP（`192.168.x.x`）。
> Tailscale IP 无论你在哪个网络下都有效。

---

## 五、每次使用流程

```
1. 电脑：打开 OpenClaw（保持运行）
2. 电脑：确认 Tailscale 已连接（托盘图标正常）
3. 手机：打开 Tailscale，确认 VPN 开关已开
4. 手机：打开 YomuSensei，正常使用
```

Tailscale 会在后台保持连接，步骤 2-3 通常自动完成，主要确保 OpenClaw 在运行即可。

---

## 六、扩展：给 OpenClaw Agent 添加更多能力

YomuSensei App 只是 OpenClaw Agent 的前端，Agent 能力越强，App 体验越好。

你可以在 OpenClaw 中给 Agent 添加：
- **网页浏览工具**：让 Agent 实时抓取 NHK Easy 最新文章
- **日语词典工具**：查询更准确的词义和例句
- **文件读写工具**：保存学习笔记

**添加后不需要修改 App 代码**，App 发出同样的请求，Agent 会自动使用新 Skill 来增强回答质量。

---

## 七、常见问题

**Q：App 提示连接超时**
- 检查电脑上 OpenClaw 是否在运行
- 检查手机 Tailscale VPN 是否已开启
- 确认 Base URL 填的是 Tailscale IP（`100.x.x.x`），不是本地 IP

**Q：Tailscale IP 会变吗？**
不会。每台设备的 Tailscale IP 在同一账号下是固定的，一次配置永久有效。

**Q：收费吗？**
Tailscale 免费计划支持最多 3 台设备，手机 + 电脑完全够用。
