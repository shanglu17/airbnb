package com.example.airbnb.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.airbnb.ui.detail.DetailRoute
import com.example.airbnb.ui.home.HomeScreen
import com.example.airbnb.ui.home.HomeViewModel

private object Route {
    const val HOME = "home"
    const val DETAIL = "detail/{listingId}"
    const val DETAIL_PREFIX = "detail"
}

@Composable
fun AirbnbApp() {
    val navController = rememberNavController()
    val vm: HomeViewModel = viewModel()
    LaunchedEffect(Unit) {
        vm.restoreScrollPosition()
    }

    NavHost(
        navController = navController,
        startDestination = Route.HOME
    ) {
        composable(Route.HOME) {
            HomeScreen(
                state = vm.uiState,
                availableCities = vm.cities,
                onListingClick = { listing ->
                    vm.onListingOpened(listing)
                    navController.navigate("${Route.DETAIL_PREFIX}/${listing.id}")
                },
                onLoadMore = vm::loadMore,
                onRefresh = vm::refresh,
                onRetry = vm::retry,
                onSearchKeywordChange = vm::onSearchKeywordChange,
                onCitySelected = vm::onCitySelected,
                onClearFilters = vm::clearFilters,
                onListPositionChange = vm::onListPositionChange
            )
        }
        composable(
            route = Route.DETAIL,
            arguments = listOf(navArgument("listingId") { type = NavType.IntType })
        ) {
            DetailRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
