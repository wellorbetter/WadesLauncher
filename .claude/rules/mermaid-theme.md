# Mermaid 图表配色与结构规范

所有 Mermaid 图表必须遵循以下规范。

## 配色方案（蓝青色系）

### 架构图 / 流程图
```
%%{init: {'theme': 'base', 'themeVariables': {'primaryColor': '#4A90D9', 'primaryTextColor': '#FFFFFF', 'primaryBorderColor': '#2D6CB4', 'secondaryColor': '#67B7DC', 'tertiaryColor': '#E8F4FD', 'lineColor': '#2D6CB4', 'noteTextColor': '#333333', 'noteBkgColor': '#FFF8E1'}}}%%
```

### 时序图
```
%%{init: {'theme': 'base', 'themeVariables': {'actorBkg': '#4A90D9', 'actorTextColor': '#FFFFFF', 'actorBorder': '#2D6CB4', 'signalColor': '#2D6CB4', 'signalTextColor': '#333333', 'noteBkgColor': '#FFF8E1', 'noteTextColor': '#333333', 'activationBkgColor': '#E8F4FD', 'activationBorderColor': '#4A90D9'}}}%%
```

### 类图
```
%%{init: {'theme': 'base', 'themeVariables': {'primaryColor': '#4A90D9', 'primaryTextColor': '#FFFFFF', 'primaryBorderColor': '#2D6CB4', 'lineColor': '#2D6CB4', 'secondaryColor': '#E8F4FD'}}}%%
```

## 结构规范
- 中文标注，每个标注 ≤15 字
- 关键路径用粗线 `==>` 标注
- 异常路径用虚线 `-.->` + 红色 `style ... fill:#E57373`
- 成功状态用绿色 `style ... fill:#81C784`
- 节点数 ≤15，超过则拆分为多张图
- subgraph 分组逻辑清晰
- 连线标签 ≤8 字
- 层级关系用 TD（上到下），流程用 LR（左到右）

## 语义符号前缀
在节点标签中使用：
- 📱 移动端/客户端
- 🖥️ 服务端/后台
- 💾 数据库/存储
- 🔌 接口/API
- 🔀 分支/判断
- 🔄 循环/重试
- 🔒 安全/权限
- ✅ 成功 / ❌ 失败 / ⚠️ 警告
