# /perf-analysis 性能优化

全栈性能分析与优化建议。

## 触发方式
```
/perf-analysis <性能问题描述>
/perf-analysis <profiling数据路径>
```

## 支持的场景

### Android
- 卡顿(Jank)、CPU 负载、渲染性能、ANR、慢启动、内存压力
- 性能基准：主线程单次操作 <16ms (60fps)，onDraw() 零对象分配

### Flutter
- 帧率下降、Widget 重建过多、Isolate 通信瓶颈、平台通道延迟
- 性能基准：build() <16ms，避免不必要的 setState

### Spring Boot
- 接口响应慢、数据库查询 N+1、连接池瓶颈、GC 压力
- 性能基准：P99 <200ms，数据库查询 <50ms

### Rust
- 内存分配热点、锁竞争、FFI 调用开销
- 性能基准：零拷贝优先，避免不必要的 clone

### 中间件
- Kafka 吞吐量、RabbitMQ 消费速率、Redis 命中率

## 分析流程
1. 有 profiling 数据 → trace 分析 → 定位热点
2. 无 profiling 数据 → 代码级检查 → 识别反模式
3. 产出优化方案 + 预期收益评估
