# Personal Indie Dev Workspace

## 项目定位
个人独立开发工作区，覆盖移动端、跨端、后端、中间件全栈技术栈。

## 技术栈
- **移动端（Phase 1）**: Android (Kotlin/Java), Jetpack Compose, MVVM/MVI + Clean Architecture
- **跨端（Phase 2）**: Flutter (Dart) + Rust FFI 高性能模块
- **后端**: Spring Boot (Java/Kotlin), Spring Security, Spring Data JPA
- **中间件**: Kafka, RabbitMQ, Redis, Elasticsearch
- **大数据**: Flink, Spark (按需引入)
- **数据库**: PostgreSQL (主), MySQL, MongoDB (文档), ClickHouse (分析)
- **DevOps**: Docker, Docker Compose, GitHub Actions
- **文档**: Mermaid 图表, Markdown 技术文档

## 开发规范

### 架构原则
- 所有项目遵循 Clean Architecture 分层（data / domain / presentation）
- Android 项目使用 MVVM 或 MVI 架构
- Flutter 项目使用 BLoC/Riverpod + Clean Architecture
- Spring Boot 项目使用 DDD 分层（controller / service / repository / domain）
- Rust 模块通过 FFI 桥接，保持接口最小化

### 编码规范
- Kotlin/Java: 方法 <50 行，类 <500 行
- Dart: 遵循 effective_dart，widget <300 行
- Rust: 遵循 clippy 全部 warnings
- Java (Spring Boot): 遵循 Google Java Style
- 所有代码必须有单元测试，覆盖率目标 >80%

### 文档规范
- 每个功能开发完成后，在 `docs/<feature-name>-tech-doc.md` 生成技术文档
- 技术文档结构：背景 → 架构概览(含Mermaid图) → 核心流程 → 设计决策 → API文档 → 注意事项
- 架构图使用 Mermaid，遵循 `.claude/rules/mermaid-theme.md` 配色规范
- 涉及的架构模式和设计模式需在文档中单独标注说明

### Git 规范
- Commit message: `<type>(<scope>): <description>`
- type: feat / fix / refactor / docs / test / chore / perf
- 分支: main / develop / feature/* / bugfix/* / release/*

## 开发流水线
使用 `/dev` 触发 6 阶段一站式开发流水线：
1. 需求校准 → 2. 架构设计 → 3. 编码实现 → 4. 自校验 → 5. 代码审查 → 6. 交付输出

## 自校验策略
- Android: 单元测试 + Instrumented Test + Lint 检查
- Flutter: widget test + integration test + dart analyze
- Rust: cargo test + cargo clippy + cargo fmt --check
- Spring Boot: JUnit5 + MockMvc + Testcontainers
- 中间件: Embedded Kafka/RabbitMQ 测试

## 沟通原则
- 先确认再动手，不猜测不编造
- 展示推理过程，数据驱动
- 主动提出方案选项，说明倾向和理由
- 不确定就说不确定，然后去查代码或问用户
- 涉及架构模式/设计模式时，附参考链接和最小示例讲解
