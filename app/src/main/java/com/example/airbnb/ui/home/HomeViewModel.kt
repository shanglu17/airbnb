package com.example.airbnb.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airbnb.data.Listing
import com.example.airbnb.data.MockData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class HomeUiState(
    val items: List<Listing> = emptyList(),
    val selectedCity: String = "All",
    val searchKeyword: String = "",
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isInitialLoading: Boolean = true,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val listFirstVisibleIndex: Int = 0,
    val listFirstVisibleOffset: Int = 0
)

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val pageSize = 8
    private val source = MockData.listings
    private val cityKey = "home_city_filter"
    private val queryKey = "home_search_query"
    private val loadedCountKey = "home_loaded_count"
    private val scrollIndexKey = "home_scroll_index"
    private val scrollOffsetKey = "home_scroll_offset"

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        refresh(initial = true)
    }

    val cities: List<String> = MockData.supportedCities

    fun onSearchKeywordChange(keyword: String) {
        uiState = uiState.copy(searchKeyword = keyword)
        savedStateHandle[queryKey] = keyword
        refresh()
    }

    fun onCitySelected(city: String) {
        if (uiState.selectedCity == city) return
        uiState = uiState.copy(selectedCity = city)
        savedStateHandle[cityKey] = city
        refresh()
    }

    fun clearFilters() {
        uiState = uiState.copy(selectedCity = "All", searchKeyword = "")
        savedStateHandle[cityKey] = "All"
        savedStateHandle[queryKey] = ""
        refresh()
    }

    fun loadMore() {
        if (uiState.isLoadingMore || uiState.isInitialLoading || !uiState.hasMore) return

        uiState = uiState.copy(isLoadingMore = true, errorMessage = null)
        viewModelScope.launch {
            delay(400)
            if (shouldMockError()) {
                uiState = uiState.copy(
                    isLoadingMore = false,
                    errorMessage = "Failed to load more listings."
                )
                return@launch
            }
            val filtered = currentFilteredSource()
            val currentSize = uiState.items.size.coerceAtMost(filtered.size)
            val nextChunk = filtered.drop(currentSize).take(pageSize)
            val allItems = uiState.items + nextChunk
            savedStateHandle[loadedCountKey] = allItems.size
            uiState = uiState.copy(
                items = allItems,
                isLoadingMore = false,
                hasMore = allItems.size < filtered.size
            )
        }
    }

    fun refresh(initial: Boolean = false) {
        val restoredCity = savedStateHandle.get<String>(cityKey) ?: uiState.selectedCity
        val restoredQuery = savedStateHandle.get<String>(queryKey) ?: uiState.searchKeyword
        uiState = uiState.copy(
            selectedCity = restoredCity,
            searchKeyword = restoredQuery,
            isRefreshing = !initial,
            isInitialLoading = initial,
            errorMessage = null
        )
        viewModelScope.launch {
            delay(if (initial) 300 else 600)
            val filtered = currentFilteredSource()
            if (shouldMockError() && filtered.isNotEmpty()) {
                uiState = uiState.copy(
                    isRefreshing = false,
                    isInitialLoading = false,
                    errorMessage = "Unable to refresh listings. Please retry."
                )
                return@launch
            }
            val restoredCount = (savedStateHandle.get<Int>(loadedCountKey) ?: pageSize)
                .coerceAtLeast(pageSize)
            val targetCount = if (initial) restoredCount else pageSize
            val initialItems = filtered.take(targetCount)
            savedStateHandle[loadedCountKey] = initialItems.size
            uiState = uiState.copy(
                items = initialItems,
                isRefreshing = false,
                isInitialLoading = false,
                hasMore = initialItems.size < filtered.size
            )
        }
    }

    fun retry() {
        if (uiState.items.isEmpty()) {
            refresh()
        } else {
            loadMore()
        }
    }

    fun onListPositionChange(index: Int, offset: Int) {
        if (index == uiState.listFirstVisibleIndex && offset == uiState.listFirstVisibleOffset) return
        uiState = uiState.copy(listFirstVisibleIndex = index, listFirstVisibleOffset = offset)
        savedStateHandle[scrollIndexKey] = index
        savedStateHandle[scrollOffsetKey] = offset
    }

    private fun currentFilteredSource(): List<Listing> {
        val city = uiState.selectedCity
        val query = uiState.searchKeyword.trim()
        return source.filter { listing ->
            val cityMatch = city == "All" || listing.location == city
            val queryMatch = query.isBlank() || listing.title.contains(query, ignoreCase = true)
            cityMatch && queryMatch
        }
    }

    private fun shouldMockError(): Boolean = Random.nextInt(100) < 10

    fun restoreScrollPosition() {
        val restoredIndex = savedStateHandle.get<Int>(scrollIndexKey) ?: 0
        val restoredOffset = savedStateHandle.get<Int>(scrollOffsetKey) ?: 0
        uiState = uiState.copy(
            listFirstVisibleIndex = restoredIndex,
            listFirstVisibleOffset = restoredOffset
        )
    }
}
