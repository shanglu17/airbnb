# Airbnb (Jetpack Compose)

Kotlin + Jetpack Compose 复现版 Airbnb 核心流程，当前覆盖：

- 首页：搜索/城市筛选、下拉刷新、懒加载分页、空态、错误重试
- 首页卡片：图片、标签、评分、房型信息、价格区块
- 详情页：独立 ViewModel 状态、顶部大图轮播、房源信息、设施、评价、底部固定 CTA
- 导航与状态：进入详情再返回时保留首页滚动位置、筛选条件和已加载内容

## 当前实现结构

```text
app/src/main/java/com/example/airbnb
├─ data/                # Listing/Review 模型 + Mock 数据
├─ navigation/          # NavHost 路由
├─ ui/components/       # 首页复用卡片组件
├─ ui/home/             # 首页 UI + 状态容器
├─ ui/detail/           # 详情 UI + 独立 ViewModel
└─ ui/theme/            # 主题与配色
```

## 复现过程中遇到的问题与优化

1. **分页触底抖动**
   - 问题：列表在底部附近可能重复触发分页。
   - 优化：`derivedStateOf + isLoadingMore + hasMore` 三重门控，避免重复请求。
2. **返回首页闪烁和重载**
   - 问题：详情返回首页时，列表位置和筛选容易丢失。
   - 优化：`SavedStateHandle` 保存筛选、已加载条数、滚动 index/offset，返回时恢复。
3. **图片加载观感**
   - 问题：网络图片延迟导致体验不稳定。
   - 优化：列表与详情统一 `ContentScale.Crop` 和稳定尺寸，减少跳动。
4. **异常态缺失**
   - 问题：只做 happy path 时，刷新失败/分页失败体验差。
   - 优化：首页补齐初始失败、分页失败、空态和重试入口。

## 运行

```powershell
Set-Location E:\github\Airbnb
.\gradlew.bat :app:assembleDebug
```
