package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    data object Root : Screen("root")
    data object BeefwebNotConfigured : Screen("beefweb_not_configured")
    data class Directory(val path: String) : Screen("directory/{path}") {
        // Add this companion object to access the route template
        companion object {
            const val ROUTE_TEMPLATE = "directory/{path}"
        }
    }
}

@Composable
fun BrowserMainPage(vm: AppViewModel) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (!vm.browserViewModel.loadingData.value && vm.browserViewModel.getDirectory().getEntries().isEmpty()) Screen.BeefwebNotConfigured.route else Screen.Root.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() },
    ) {
        composable(Screen.Root.route) { RootScreen(vm, navController) }
        composable(Screen.BeefwebNotConfigured.route) { NotConfiguredScreen(vm) }
        composable(
            Screen.Directory.ROUTE_TEMPLATE,
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            DirectoryScreen(vm, navController, path)
        }
    }
}

@Composable
fun NotConfiguredScreen(vm: AppViewModel) {
    NoMusicDirectoriesConfiguredInfo(vm.browserViewModel)
}

@Composable
fun RootScreen(vm: AppViewModel, navController: NavHostController) {
    // Display root directory contents
    // Use navController.navigate() to navigate to subdirectories or files

    vm.browserViewModel.reset()  // reset isAdded-Flags
    vm.browserViewModel.setCurrentPath("")
    BrowserPage(vm, navController)
}


@Composable
fun DirectoryScreen(vm: AppViewModel, navController: NavHostController, path: String) {
    // Display directory contents at the given path
    // Use navController.navigate() to navigate to subdirectories or files

    vm.browserViewModel.setCurrentPath(
        if (path.startsWith("{path}"))
            path.replace("{path}", "")
        else
            path
    )
    BrowserPage(vm, navController)
}
