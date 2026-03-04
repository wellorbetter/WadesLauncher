代码审查（正确性/性能/安全/可维护性）。

Usage: /review <文件路径或模块>

示例:
  /review src/main/java/com/example/UserService.java
  /review lib/features/auth/

四维审查:
1. 正确性: 逻辑错误、边界条件、空安全
2. 性能: 算法复杂度、内存分配、I/O 阻塞
3. 安全: 注入攻击、权限校验、敏感数据
4. 可维护性: 命名、职责单一、测试覆盖
