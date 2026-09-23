package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.Web2ApkViewModel
import com.example.ui.screens.*

object Routes {
    const val DASHBOARD = "dashboard"
    const val CREATE_WIZARD = "create_wizard"
    const val BUILD_PROGRESS = "build_progress"
    const val PROJECTS = "projects"
    const val HISTORY = "history"
    const val HELP = "help"
    const val ADMIN = "admin"
    const val SIMULATOR = "simulator"
}

@Composable
fun AppNavigation(
    viewModel: Web2ApkViewModel
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onCreateNewClick = { navController.navigate(Routes.CREATE_WIZARD) },
                    onViewProjectsClick = { navController.navigate(Routes.PROJECTS) },
                    onViewHistoryClick = { navController.navigate(Routes.HISTORY) },
                    onHelpClick = { navController.navigate(Routes.HELP) },
                    onAdminClick = { navController.navigate(Routes.ADMIN) },
                    onEditProject = { project ->
                        viewModel.loadExistingProject(project)
                        navController.navigate(Routes.CREATE_WIZARD)
                    },
                    onQuickBuild = { project ->
                        viewModel.loadExistingProject(project)
                        viewModel.triggerBuild("DEBUG")
                        navController.navigate(Routes.BUILD_PROGRESS)
                    }
                )
            }

            composable(Routes.CREATE_WIZARD) {
                CreateWizardScreen(
                    viewModel = viewModel,
                    onBackToDashboard = { navController.popBackStack() },
                    onStartBuild = { buildType ->
                        viewModel.triggerBuild(buildType)
                        navController.navigate(Routes.BUILD_PROGRESS)
                    },
                    onOpenPreviewSimulator = { navController.navigate(Routes.SIMULATOR) }
                )
            }

            composable(Routes.BUILD_PROGRESS) {
                BuildProgressScreen(
                    viewModel = viewModel,
                    onBackToDashboard = {
                        navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                    },
                    onLaunchApp = {
                        navController.navigate(Routes.SIMULATOR)
                    }
                )
            }

            composable(Routes.PROJECTS) {
                ProjectsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreateNew = {
                        viewModel.startNewDraft()
                        navController.navigate(Routes.CREATE_WIZARD)
                    },
                    onEditProject = { project ->
                        viewModel.loadExistingProject(project)
                        navController.navigate(Routes.CREATE_WIZARD)
                    },
                    onBuildProject = { project ->
                        viewModel.loadExistingProject(project)
                        viewModel.triggerBuild("DEBUG")
                        navController.navigate(Routes.BUILD_PROGRESS)
                    }
                )
            }

            composable(Routes.HISTORY) {
                BuildHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HELP) {
                HelpScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADMIN) {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SIMULATOR) {
                AppPreviewTestScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
