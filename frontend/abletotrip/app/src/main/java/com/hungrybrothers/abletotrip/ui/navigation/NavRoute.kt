package com.hungrybrothers.abletotrip.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.hungrybrothers.abletotrip.ui.screen.AddressScreen
import com.hungrybrothers.abletotrip.ui.screen.DepartureScreen
import com.hungrybrothers.abletotrip.ui.screen.DetailScreen
import com.hungrybrothers.abletotrip.ui.screen.GuideScreen
import com.hungrybrothers.abletotrip.ui.screen.HomeScreen
import com.hungrybrothers.abletotrip.ui.screen.LoginScreen
import com.hungrybrothers.abletotrip.ui.screen.OnboardingScreen
import com.hungrybrothers.abletotrip.ui.screen.SearchScreen
import com.hungrybrothers.abletotrip.ui.screen.ShowMoreScreen
import com.hungrybrothers.abletotrip.ui.screen.SplashScreen
import com.hungrybrothers.abletotrip.ui.screen.TotalRouteScreen
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.NavigationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.PlaceCompleteViewModel

enum class NavRoute(val routeName: String, val description: String) {
    SPLASH("SPLASH", "스플래시화면"),
    ONBOARDING("ONBOARDING", "온보딩 화면"),
    HOME("HOME", "홈화면"),
    LOGIN("LOGIN", "로그인화면"),
    ADDRESS("ADDRESS", "초기주소입력화면"),
    SEARCH("SEARCH", "검색화면"),
    DETAIL("DETAIL", "상세화면"),
    DEPARTURE("DEPARTURE", "출발화면"),
    TOTAL_ROUTE("TOTAL_ROUTE", "전체경로화면"),
    GUIDE("GUIDE", "길안내화면"),
    SHOWMORE("SHOWMORE", "더보기"),
}

@Composable
fun Navigation(navController: NavHostController) {
    val currentLocationViewModel = viewModel<CurrentLocationViewModel>()
    val navigationViewModel = viewModel<NavigationViewModel>()

    NavHost(navController, startDestination = NavRoute.SPLASH.routeName) {
        composable(NavRoute.SPLASH.routeName) { SplashScreen(navController) }
        composable(NavRoute.ONBOARDING.routeName) { OnboardingScreen(navController) }
        auth(navController)
        home(navController, currentLocationViewModel, navigationViewModel)
    }
}

fun NavGraphBuilder.auth(navController: NavController) {
    navigation(startDestination = NavRoute.LOGIN.routeName, route = "AUTHGRAPH") {
        composable(NavRoute.LOGIN.routeName) { LoginScreen(navController) }
        composable(NavRoute.ADDRESS.routeName) {
            val autocompleteViewModel = viewModel<PlaceCompleteViewModel>()
            AddressScreen(navController, autocompleteViewModel)
        }
    }
}

fun NavGraphBuilder.home(
    navController: NavController,
    currentLocationViewModel: CurrentLocationViewModel,
    navigationViewModel: NavigationViewModel,
) {
    navigation(startDestination = NavRoute.HOME.routeName, route = "HOMEGRAPH") {
        composable(NavRoute.HOME.routeName) { HomeScreen(navController, currentLocationViewModel) }
        composable("${NavRoute.SEARCH.routeName}/{keyword}") { backStackEntry ->
            val keyword = backStackEntry.arguments?.getString("keyword").orEmpty()
            SearchScreen(navController, keyword, currentLocationViewModel)
        }
        composable("${NavRoute.DETAIL.routeName}/{id}") { backStackEntry ->
            DetailScreen(navController, backStackEntry.arguments?.getString("id")?.toInt())
        }
        composable(
            route = "DEPARTURE/{latitude}/{longitude}/{address}",
            arguments =
                listOf(
                    navArgument("latitude") { type = NavType.FloatType },
                    navArgument("longitude") { type = NavType.FloatType },
                    navArgument("address") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
            val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0
            val address = backStackEntry.arguments?.getString("address") ?: ""

            val autocompleteViewModel = viewModel<PlaceCompleteViewModel>()
            DepartureScreen(
                navController,
                autocompleteViewModel,
                latitude,
                longitude,
                address,
                currentLocationViewModel,
            )
        }
        composable(
            route = "TOTAL_ROUTE/{departure}/{arrival}",
            arguments =
                listOf(
                    navArgument("departure") { type = NavType.StringType },
                    navArgument("arrival") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val departure = backStackEntry.arguments?.getString("departure")
            val arrival = backStackEntry.arguments?.getString("arrival")

            TotalRouteScreen(navController, departure, arrival, navigationViewModel)
        }
        composable(
            route = "${NavRoute.SHOWMORE.routeName}/{category}",
            arguments =
                listOf(
                    navArgument("category") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ShowMoreScreen(navController, category, currentLocationViewModel)
        }
        composable(
            NavRoute.GUIDE.routeName,
        ) { GuideScreen(navController, navigationViewModel, currentLocationViewModel) }
    }
}
