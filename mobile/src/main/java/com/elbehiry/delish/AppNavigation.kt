package com.elbehiry.delish

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elbehiry.delish.ui.launcher.LaunchDestination
import com.elbehiry.delish.ui.launcher.LauncherView
import com.elbehiry.delish.ui.main.MainContent
import com.elbehiry.delish.ui.onboarding.OnBoardingContent
import com.elbehiry.delish.ui.util.checkAllMatched
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation

internal sealed class Screen(val route: String) {
    object Launcher : Screen("launcher")
    object OnBoarding : Screen("onBoarding")
    object Feed : Screen("feed")
}

private sealed class LeafScreen(
    private val route: String,
) {
    fun createRoute(root: Screen) = "${root.route}/$route"

}

@Composable
internal fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Launcher.route,
        modifier = modifier
    ) {
        addLauncher(navController = navController)
        addOnBoarding(navController = navController)
        addFeed(navController = navController)
    }
}

private fun NavGraphBuilder.addLauncher(
    navController: NavController
) {
    composable(
        route = Screen.Launcher.route
    ) {
        LauncherView { destination ->
            when (destination) {
                LaunchDestination.MAIN_ACTIVITY -> navController.navigate(Screen.Feed.route) {
                    popUpTo(Screen.Launcher.route) {
                        inclusive = true
                    }
                }
                LaunchDestination.ON_BOARDING -> navController.navigate(Screen.OnBoarding.route) {
                    popUpTo(Screen.Launcher.route) {
                        inclusive = true
                    }
                }
            }.checkAllMatched
        }
    }
}

private fun NavGraphBuilder.addOnBoarding(
    navController: NavController
) {
    composable(
        route = Screen.OnBoarding.route
    ) {
        OnBoardingContent {
            navController.navigate(Screen.Feed.route)
        }
    }
}

private fun NavGraphBuilder.addFeed(
    navController: NavController
) {
    composable(
        route = Screen.Feed.route
    ) {
        MainContent(
            onIngredientContent = {},
            onCuisineSearch = {},
            onDetails = {  },
            onExploreClicked = {},
            onIngredientSearch = {}
        )
    }
}
