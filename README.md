# 🐢 海龟汤推理游戏  
> **基于百度千帆大模型的沉浸式文字解谜体验**

---

## 🌊 游戏简介
在 **海龟汤推理游戏** 中，你将与 **AI 主持人** 进行一场逻辑与想象力的对决。  
系统会实时生成一段看似矛盾的情境，你只需不断提出 **“是 / 否”问题** 与 **递进式提示**，最终揭开隐藏的真相！

---

## 🎯 核心功能

| 功能 | 说明 |
|---|---|
| 🎲 **动态谜题** | 大模型即时生成全新海龟汤，永不重复 |
| ❓ **是 / 否问答** | AI 仅回答 **是 / 否 / 与此无关** |
| 💡 **递进提示** | 最多 3 条线索，层层递进 |
| 🔍 **真相验证** | 提交完整猜测，AI 判定对错 |
| 📜 **全程记录** | 所有对话自动存档，随时复盘 |

---

## 🚀 部署步骤

### ① 克隆仓库
```bash
git clone https://github.com/sp-shark/TurtleSoup.git
```

### ② 配置 API 密钥  
创建 `src/main/resources/application.properties`：

```properties
# 服务端端口
server.port=8080

# 百度千帆 API 密钥
qianfan.api-key=your-api-key
```

> 如何获取？  
> [百度千帆控制台 → 安全认证 → API Key](https://console.bce.baidu.com/qianfan)

### ③ 运行 & 访问
打开浏览器 → [http://localhost:8080](http://localhost:8080)  
即刻开始推理！

---


## 📦 技术栈
- **后端**：Spring Boot 2.7 + OkHttp + Gson  
- **前端**：HTML5
- **LLM**：百度千帆 ERNIE-X1-Turbo-32K  
