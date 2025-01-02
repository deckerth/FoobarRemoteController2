package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.BrowserViewModel

sealed class Screen(val route: String) {
    data object Root : Screen("root")
    data class Directory(val path: String) : Screen("directory/{path}") {
        // Add this companion object to access the route template
        companion object {
            const val ROUTE_TEMPLATE = "directory/{path}"
        }
    }
}

@Composable
fun BrowserMainPage(vm: BrowserViewModel = viewModel()) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Root.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() },
    ) {
        composable(Screen.Root.route) { RootScreen(vm, navController) }
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
fun RootScreen(vm: BrowserViewModel, navController: NavHostController) {
    // Display root directory contents
    // Use navController.navigate() to navigate to subdirectories or files

    vm.setCurrentPath("")
    BrowserPage(vm, navController)
}

@Composable
fun DirectoryScreen(vm: BrowserViewModel, navController: NavHostController, path: String) {
    // Display directory contents at the given path
    // Use navController.navigate() to navigate to subdirectories or files

    vm.setCurrentPath(
        if (path.startsWith("{path}"))
            path.replace("{path}", "")
        else
            path
    )
    BrowserPage(vm, navController)
}
