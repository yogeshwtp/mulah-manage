package com.example.mulahmanage.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mulahmanage.ui.dashboard.AddExpenseScreen
import com.example.mulahmanage.ui.dashboard.DashboardScreen
import com.example.mulahmanage.ui.dashboard.DashboardViewModel
import com.example.mulahmanage.ui.dashboard.EditTransactionScreen
import com.example.mulahmanage.ui.reports.ReportsScreen
import com.example.mulahmanage.ui.settings.SettingsScreen

// Sealed class to define all possible routes in a type-safe way
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object AddExpense : Screen("add_expense")

    // The route for editing includes a placeholder for the transaction ID
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        // Helper function to create the full route with a specific ID
        fun createRoute(transactionId: Int) = "edit_transaction/$transactionId"
    }
}

// Data class to easily manage bottom bar items
data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

// List of items that will appear in the bottom navigation bar
val bottomBarItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Reports, "Reports", Icons.Default.Assessment),
    BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings)
)

@Composable
fun MainScreen(viewModel: DashboardViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Logic to only show the bottom bar on the main screens
    val showBottomBar = bottomBarItems.any { it.screen.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 4.dp) {
                    bottomBarItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToEditTransaction = { transactionId ->
                        // Use the helper function to navigate to the correct edit screen
                        navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                    }
                )
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            // Define the route for the Edit Transaction screen, expecting an ID
            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: 0
                EditTransactionScreen(
                    viewModel = viewModel,
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

