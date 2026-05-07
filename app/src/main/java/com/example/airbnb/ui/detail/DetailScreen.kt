package com.example.airbnb.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailRoute(onBack: () -> Unit) {
    val vm: DetailViewModel = viewModel()
    DetailScreen(
        state = vm.uiState,
        onBack = onBack,
        onRetry = vm::retry
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val listing = state.listing
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(text = "Detail", style = MaterialTheme.typography.titleLarge)
            }
        },
        bottomBar = {
            if (listing != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "¥${listing.pricePerNight} / night",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Total before taxes: ¥${listing.pricePerNight + listing.cleaningFee + listing.serviceFee}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = {}) {
                        Text("Reserve")
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null || listing == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.errorMessage ?: "Listing not found")
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                val pagerState = rememberPagerState(pageCount = { listing.imageUrls.size })
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    item {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) { page ->
                            AsyncImage(
                                model = listing.imageUrls[page],
                                contentDescription = listing.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = "${pagerState.currentPage + 1}/${listing.imageUrls.size}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = listing.title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null)
                                Text(
                                    text = "${String.format("%.1f", listing.rating)} (${listing.reviewCount} reviews)",
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                            Text(
                                text = listing.location,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "${listing.guestCount} guests · ${listing.bedroomCount} bedrooms · ${listing.bathroomCount} baths",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Text(
                                text = "${listing.hostName} (${listing.hostSince})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Text(
                                text = listing.description,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text("Amenities", style = MaterialTheme.typography.titleMedium)
                            listing.amenities.forEach { amenity ->
                                Text(
                                    text = "• $amenity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text("Reviews", style = MaterialTheme.typography.titleMedium)
                            listing.reviews.take(3).forEach { review ->
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Text(
                                        text = "${review.author} · ${review.date}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "Rating ${String.format("%.1f", review.rating)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = review.comment,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
