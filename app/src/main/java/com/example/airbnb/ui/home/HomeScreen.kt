package com.example.airbnb.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.airbnb.data.Listing
import com.example.airbnb.ui.components.ListingCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    availableCities: List<String>,
    onListingClick: (Listing) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSearchKeywordChange: (String) -> Unit,
    onCitySelected: (String) -> Unit,
    onClearFilters: () -> Unit,
    onListPositionChange: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.listFirstVisibleIndex,
        initialFirstVisibleItemScrollOffset = state.listFirstVisibleOffset
    )
    val shouldLoadMore by remember(state.hasMore, state.isLoadingMore, state.isRefreshing, listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.hasMore &&
                !state.isLoadingMore &&
                !state.isRefreshing &&
                totalItems > 0 &&
                lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .map { it.first to it.second }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                onListPositionChange(index, offset)
            }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh
    )
    val isSearchMode = state.searchKeyword.trim().isNotBlank()
    val showPersonalizedLayout = state.continueSearchItems.isNotEmpty() || state.recentViewedItems.isNotEmpty()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(text = "Airbnb", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = state.searchKeyword,
                    onValueChange = onSearchKeywordChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    placeholder = { Text("搜索房源") },
                    singleLine = true
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp)
                ) {
                    items(availableCities) { city ->
                        val selected = city == state.selectedCity
                        AssistChip(
                            onClick = { onCitySelected(city) },
                            label = { Text(city) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.isInitialLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null && state.items.isEmpty() -> {
                    CenterMessage(
                        title = "加载失败",
                        subtitle = state.errorMessage,
                        buttonLabel = "重试",
                        onAction = onRetry
                    )
                }

                state.items.isEmpty() -> {
                    CenterMessage(
                        title = "没有找到房源",
                        subtitle = "可以换个城市或关键词试试。",
                        buttonLabel = "清空筛选",
                        onAction = onClearFilters
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isSearchMode) {
                            item {
                                SectionTitle("搜索结果")
                            }
                            items(state.items, key = { "search_${it.id}" }) { listing ->
                                ListingCard(item = listing, onClick = onListingClick)
                            }
                        } else if (showPersonalizedLayout) {
                            val recommendSections = listOf(
                                "可免费取消的房源" to state.items.filter { it.supportsFreeCancellation }.take(8),
                                "周边的房源" to state.items.filter { it.isNearbyArea }.take(8),
                                "前后日期可订" to state.items.filter { it.isFlexibleDateAvailable }.take(8)
                            ).filter { it.second.isNotEmpty() }

                            if (state.continueSearchItems.isNotEmpty()) {
                                item {
                                    ContinueSearchSection(
                                        items = state.continueSearchItems,
                                        onListingClick = onListingClick
                                    )
                                }
                            }
                            if (state.recentViewedItems.isNotEmpty()) {
                                item {
                                    SectionTitle("最近浏览")
                                }
                                item {
                                    HorizontalListingRow(
                                        items = state.recentViewedItems,
                                        keyPrefix = "recent",
                                        onListingClick = onListingClick
                                    )
                                }
                            }
                            item {
                                SectionTitle("为你推荐")
                            }
                            if (recommendSections.isEmpty()) {
                                item {
                                    HorizontalListingRow(
                                        items = state.items.take(8),
                                        keyPrefix = "recommend",
                                        onListingClick = onListingClick
                                    )
                                }
                            } else {
                                recommendSections.forEach { (title, listings) ->
                                    item(key = "recommend_title_$title") {
                                        SectionTitle(title)
                                    }
                                    item(key = "recommend_row_$title") {
                                        HorizontalListingRow(
                                            items = listings,
                                            keyPrefix = "recommend_$title",
                                            onListingClick = onListingClick
                                        )
                                    }
                                }
                            }
                        } else {
                            state.items
                                .groupBy { it.location }
                                .forEach { (city, listings) ->
                                    item(key = "city_$city") {
                                        SectionTitle(text = city)
                                    }
                                    item(key = "city_row_$city") {
                                        HorizontalListingRow(
                                            items = listings.take(10),
                                            keyPrefix = "city_${city}",
                                            onListingClick = onListingClick
                                        )
                                    }
                                }
                        }

                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        if (state.errorMessage != null && state.items.isNotEmpty()) {
                            item {
                                Surface(
                                    tonalElevation = 1.dp,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = state.errorMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = onRetry,
                                            modifier = Modifier.padding(start = 12.dp)
                                        ) {
                                            Text("重试")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun ContinueSearchSection(
    items: List<Listing>,
    onListingClick: (Listing) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = "继续搜索",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "基于你最近的搜索关键词",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            HorizontalListingRow(
                items = items,
                keyPrefix = "continue",
                onListingClick = onListingClick,
                cardWidth = 260.dp
            )
        }
    }
}

@Composable
private fun HorizontalListingRow(
    items: List<Listing>,
    keyPrefix: String,
    onListingClick: (Listing) -> Unit,
    cardWidth: Dp = 260.dp
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { "${keyPrefix}_${it.id}" }) { listing ->
            CompactListingCard(
                item = listing,
                onClick = onListingClick,
                width = cardWidth
            )
        }
    }
}

@Composable
private fun CompactListingCard(
    item: Listing,
    onClick: (Listing) -> Unit,
    width: Dp
) {
    Card(
        modifier = Modifier
            .width(width)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = item.imageUrls.firstOrNull(),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            if (item.tags.isNotEmpty()) {
                Text(
                    text = item.tags.first(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(28.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = String.format("%.1f", item.rating))
                }
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "¥${item.pricePerNight} / 晚",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CenterMessage(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
        Button(
            onClick = onAction,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(buttonLabel)
        }
    }
}
