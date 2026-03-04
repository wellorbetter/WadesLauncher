# /dev 一站式开发流水线

从需求到可交付代码 + 完整技术文档的 6 阶段流水线。适配 Android / Flutter+Rust / Spring Boot 全栈技术栈。

## 触发方式
```
/dev <需求描述或PRD路径>
/dev <PRD路径> 补充：<额外说明>
```

## 阶段 1：需求校准
- **输入**: PRD / 需求文档 / 口头描述
- **输出**: 结构化「需求确认书」
  - 功能目标、非功能需求、约束条件、验收标准、开放问题
- **动作**: 提出 Blocking 提问清单，不猜测不编造
- **门禁**: 用户明确确认需求理解正确

## 阶段 2：架构设计
- 根据目标平台自动选择架构模式：
  - Android → MVVM/MVI + Clean Architecture
  - Flutter → BLoC/Riverpod + Clean Architecture
  - Spring Boot → DDD 分层
  - Rust → 模块化 + trait 抽象
- 产出 2-3 个设计方案，每个方案含：
  - 模块划分、类图、时序图、线程/协程模型、异常边界、改动量评估
- 同步产出：Mermaid 架构图 + 类图 + 设计决策文档
- **标注使用的架构模式和设计模式，附参考链接**
- **门禁**: 用户选择方案

## 阶段 3：编码实现
- 算法设计 → 性能分析 → 实现（最小改动）→ 防御性检查
- 遵循 CLAUDE.md 中定义的编码规范
- 同步产出：基于实际代码路径的时序图 + 流程图 + 变更清单
- **门禁**: 代码语法正确，lint 通过

## 阶段 4：自校验
- 根据技术栈自动选择验证策略：
  - Android: `./gradlew test` + `./gradlew lint`
  - Flutter: `flutter test` + `flutter analyze`
  - Rust: `cargo test` + `cargo clippy`
  - Spring Boot: `./mvnw test` 或 `./gradlew test`
- 验证不通过 → 诊断修复 → 重新验证（最多 3 轮）
- **门禁**: 所有测试通过，无高危 lint 问题

## 阶段 5：代码审查
- 四维审查：正确性、性能、安全、可维护性
- 高危问题自动修复并重审（最多 3 轮）
- **门禁**: 无未解决高危问题

## 阶段 6：交付输出
- 代码变更摘要（文件列表 + 关键改动说明）
- 完整技术文档 → `docs/<feature-name>-tech-doc.md`
  - 文档中单独章节列出使用的架构模式和设计模式
- 开发总结（3-5 句话）
