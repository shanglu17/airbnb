package com.example.airbnb.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.airbnb.ui.detail.DetailScreen
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

    NavHost(
        navController = navController,
        startDestination = Route.HOME
    ) {
        composable(Route.HOME) {
            HomeScreen(
                state = vm.uiState,
                onListingClick = { listing ->
                    navController.navigate("${Route.DETAIL_PREFIX}/${listing.id}")
                },
                onLoadMore = vm::loadMore
            )
        }
        composable(
            route = Route.DETAIL,
            arguments = listOf(navArgument("listingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getInt("listingId") ?: -1
            DetailScreen(
                listing = vm.findListing(listingId),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
