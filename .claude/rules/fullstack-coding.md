# 全栈编码规范

## Kotlin / Java (Android & Spring Boot)
- 方法 <50 行，类 <500 行
- 空安全：外部输入始终空检查，@Nullable/@NonNull 注解
- 资源管理：Cursor/Stream/TypedArray 必须在 finally/use{} 中关闭
- 线程：主线程禁止网络/数据库/文件 IO/大量计算
- 命名：camelCase 方法/变量，PascalCase 类，UPPER_SNAKE 常量

## Dart (Flutter)
- 遵循 effective_dart 规范
- Widget <300 行，超过则拆分
- 状态管理统一使用 BLoC 或 Riverpod（项目内保持一致）
- 避免在 build() 中做耗时操作
- 使用 const 构造函数优化重建

## Rust
- 遵循 clippy 全部 warnings
- 优先使用零拷贝，避免不必要的 clone
- FFI 边界做好内存安全检查
- 错误处理使用 thiserror + anyhow

## Spring Boot
- DDD 分层：Controller → Service → Repository → Domain
- Controller 只做参数校验和响应封装
- Service 承载业务逻辑
- Repository 只做数据访问
- 使用 @Transactional 管理事务，注意传播级别

## 通用
- 所有公开 API 必须有参数校验
- 禁止硬编码密钥/密码/token
- 日志不打印敏感信息（密码、token、身份证号等）
- 单元测试覆盖率目标 >80%
