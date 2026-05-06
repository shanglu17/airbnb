package com.example.airbnb.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airbnb.data.Listing
import com.example.airbnb.data.MockData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<Listing> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val source = MockData.listings
    private val pageSize = 8

    var uiState by mutableStateOf(
        HomeUiState(
            items = source.take(pageSize),
            hasMore = source.size > pageSize
        )
    )
        private set

    fun loadMore() {
        if (uiState.isLoadingMore || !uiState.hasMore) return

        uiState = uiState.copy(isLoadingMore = true)
        viewModelScope.launch {
            delay(400)
            val currentSize = uiState.items.size
            val nextChunk = source.drop(currentSize).take(pageSize)
            uiState = uiState.copy(
                items = uiState.items + nextChunk,
                isLoadingMore = false,
                hasMore = currentSize + nextChunk.size < source.size
            )
        }
    }

    fun findListing(id: Int): Listing? = source.firstOrNull { it.id == id }
}
