package com.example.airbnb.ui.detail

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

data class DetailUiState(
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var uiState by mutableStateOf(DetailUiState())
        private set

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        val listingId = savedStateHandle.get<Int>("listingId") ?: -1
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            delay(350)
            val listing = MockData.findListing(listingId)
            uiState = if (listing == null) {
                DetailUiState(
                    listing = null,
                    isLoading = false,
                    errorMessage = "Listing not found."
                )
            } else {
                DetailUiState(
                    listing = listing,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }
}
