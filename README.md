# Wades Launcher

创新型 Android Launcher — 单页垂直滚动桌面 + 分组水平滑动 + 滑动变阻搜索栏 + 左滑负一屏/右滑抽屉。

## 技术栈

- Kotlin · Jetpack Compose · MVI · Clean Architecture
- Hilt · Room · Coroutines + Flow · Gradle KTS
- minSdk 26 (Android 8.0)

## 模块结构

```
:app                  入口、Hilt 根、三屏 HorizontalPager 导航
:feature:home         主桌面：分组列表、搜索栏、编辑模式、堆叠交互
:feature:drawer       右滑抽屉：全部应用 A-Z 列表
:feature:widget       左滑负一屏：AppWidgetHost、小部件管理
:feature:assistant    下拉 AI 助手面板
:core:domain          纯 Kotlin：Entity、Repository 接口、UseCase
:core:data            Repository 实现、Room、IconCache、搜索引擎
:core:ui              共享 Compose 组件、MVI 基类、主题
:core:ai              AI 客户端、Prompt 模板
```

## 核心特性

- 三屏导航：负一屏 ← 主桌面 → 应用抽屉
- 智能分区：常用 / 分类 / 最近使用 / 最近安装 / 文件夹
- 搜索引擎：前缀匹配 + 拼音匹配 + 模糊匹配（策略模式）
- 编辑模式：长按进入，iOS 风格抖动，删除/重命名/拖拽堆叠
- 溢出堆叠：分区超过 3 个时自动堆叠为 SwipeCardStack
- 下拉 AI 面板：绳子拉手视觉，下拉展开 AI 助手
- 图标三级缓存：内存 LruCache → 磁盘 WebP → PackageManager

## 设计模式

Repository · 策略 · 组合 · 观察者 · 工厂 · 适配器 · 代理 · 建造者 · 模板方法 · 依赖注入

## 构建

```bash
./gradlew assembleDebug
```

## TODO

- [ ] Round 4 堆叠编辑交互：编辑模式下长按拖拽分区卡片到另一分区形成堆叠，效果需要重新调试
- [ ] SwipeCardStack 视觉修正：后方卡片纯平移偏移（不缩放），簇状堆叠感需要微调
- [ ] PullDownPanel 绳子视觉：竖线 + 小圆球拉手，下拉伸长动画需要打磨
- [ ] 堆叠二级页面：点击堆叠展开详情 Dialog，内部排序和拆出功能需要验证
- [ ] 堆叠状态持久化：stackGroups 通过 DataStore JSON 持久化，需要测试边界情况
- [ ] 单元测试：覆盖率目标 >80%，目前缺失
- [ ] Instrumented Test：UI 自动化测试
- [ ] CI/CD：GitHub Actions 自动构建

## 文档

详细架构文档见 [docs/wades-launcher-architecture.md](docs/wades-launcher-architecture.md)
