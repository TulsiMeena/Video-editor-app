package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.ImageEditorScreen
import com.example.ui.editor.VideoEditorScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.splash.LoadingScreen

object Routes {
    const val LOADING = "loading"
    const val HOME = "home"
    const val VIDEO_EDITOR = "video_editor/{projectId}"
    const val IMAGE_EDITOR = "image_editor/{projectId}"
    const val SETTINGS = "settings"

    fun videoEditor(projectId: String) = "video_editor/$projectId"
    fun imageEditor(projectId: String) = "image_editor/$projectId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOADING
    ) {
        composable(Routes.LOADING) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToVideoEditor = { projectId ->
                    navController.navigate(Routes.videoEditor(projectId))
                },
                onNavigateToImageEditor = { projectId ->
                    navController.navigate(Routes.imageEditor(projectId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.VIDEO_EDITOR,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val editorViewModel: EditorViewModel = viewModel()
            VideoEditorScreen(
                projectId = projectId,
                viewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.IMAGE_EDITOR,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val editorViewModel: EditorViewModel = viewModel()
            ImageEditorScreen(
                projectId = projectId,
                viewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
