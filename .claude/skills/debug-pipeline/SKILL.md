# /debug-pipeline Bug 系统分析

全栈 Bug 分析流水线，支持 Android / Flutter / Spring Boot / Rust 各平台问题诊断。

## 触发方式
```
/debug-pipeline <错误描述或日志>
/debug-pipeline <crash日志路径>
```

## 支持的问题类型

### Android
- ANR / FC / Crash / FD 泄漏 / 内存泄漏 / 动画异常

### Flutter
- Widget 构建异常 / 状态管理错误 / Platform Channel 通信失败 / 渲染溢出

### Spring Boot
- 启动失败 / Bean 注入异常 / 事务问题 / 连接池耗尽 / OOM

### Rust
- Panic / 生命周期错误 / 死锁 / FFI 边界问题

### 中间件
- Kafka 消费积压 / RabbitMQ 死信 / Redis 连接超时

## 5 步流程
1. **信息收集**: 收集错误日志、堆栈、环境信息
2. **分类定性**: 判断问题类型和严重程度
3. **根因分析**: 委托 root-cause-analyzer 子代理深层分析
4. **方案制定**: 提出修复方案（含代码改动）
5. **验证策略**: 提供验证步骤和回归测试建议

## 输出
- 诊断报告（含置信度：高/中/低）
- 修复代码（如适用）
- 回归测试用例
