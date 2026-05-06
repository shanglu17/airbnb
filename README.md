# Airbnb (Jetpack Compose)

基于 Kotlin + Jetpack Compose 的 Airbnb 首页/详情页核心流程复现项目，当前优先实现：

- 首页房源列表（LazyColumn 懒加载）
- 点击卡片进入详情页展示
- ViewModel + Navigation 的状态与路由闭环

## 已实现功能

1. 首页懒加载
   - 初始加载 8 条房源
   - 滚动接近底部时自动加载下一批数据
   - 加载中显示底部 `CircularProgressIndicator`
2. 列表卡片
   - 图片、地点、评分、标题、价格
   - 卡片点击进入详情页
3. 详情页
   - 大图、标题、评分、评论数、地点、价格、描述、预订按钮
   - 顶部返回按钮

## 项目结构

```text
app/src/main/java/com/example/airbnb
├─ data/                # Listing 数据模型与 Mock 数据
├─ navigation/          # NavHost 路由
├─ ui/components/       # 可复用组件（ListingCard）
├─ ui/home/             # 首页与列表状态
├─ ui/detail/           # 详情页
└─ ui/theme/            # Compose 主题
```

## 复现中遇到的问题

1. **懒加载触发抖动**
   - 问题：滚动边界附近可能重复触发加载条件。
   - 处理：通过 `state.isLoadingMore` + `state.hasMore` 双重门控，确保同一时间只触发一次分页。
2. **列表重组开销**
   - 问题：滚动时不必要重组会造成卡顿风险。
   - 处理：`LazyColumn` 使用 `key = { it.id }`，并用 `derivedStateOf` 计算是否触底，减少无效计算。
3. **图片加载体验**
   - 问题：网络图加载慢时会影响观感。
   - 处理：接入 Coil，后续可进一步加 placeholder/error 与内存策略优化。

## 已做优化

- 使用 `ViewModel` 统一管理首页列表状态，避免页面切换导致数据丢失。
- 懒加载逻辑与 UI 渲染解耦，后续替换为真实分页接口成本低。
- 导航参数只传 `listingId`，详情数据通过单一数据源查询，避免对象序列化开销。

## 运行方式

```bash
cd Airbnb
./gradlew :app:assembleDebug
```

Windows PowerShell:

```powershell
Set-Location E:\github\Airbnb
.\gradlew.bat :app:assembleDebug
```

> 如首次下载 Gradle 依赖较慢，请重试构建命令。
