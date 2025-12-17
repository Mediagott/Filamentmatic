package com.filamentmate.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filamentmate.app.ui.screens.DashboardScreen
import com.filamentmate.app.ui.screens.FilamentProfileDetailScreen
import com.filamentmate.app.ui.screens.FilamentProfilesScreen
import com.filamentmate.app.ui.screens.PrintHistoryScreen
import com.filamentmate.app.ui.screens.PrinterSetupScreen
import com.filamentmate.app.ui.screens.SpoolDetailScreen
import com.filamentmate.app.ui.screens.SpoolsScreen
import com.filamentmate.app.ui.screens.calibration.CalibrationHubScreen
import com.filamentmate.app.ui.screens.calibration.CalibrationWizardScreen
import com.filamentmate.app.ui.viewmodel.AppInitViewModel

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, Icons.Default.Home, "Dashboard"),
    BottomNavItem(Routes.SPOOLS, Icons.Default.Inventory2, "Spulen"),
    BottomNavItem(Routes.FILAMENT_PROFILES, Icons.Default.Science, "Profile"),
    BottomNavItem(Routes.PRINTER_SETUP, Icons.Default.Print, "Drucker"),
    BottomNavItem(Routes.PRINT_HISTORY, Icons.Default.History, "Historie")
)

@Composable
fun FilamentMateNavHost(
    navController: NavHostController = rememberNavController()
) {
    // Initialize database on first launch
    val appInitViewModel: AppInitViewModel = hiltViewModel()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToCalibration = { navController.navigate(Routes.CALIBRATION_HUB) },
                    onNavigateToSpoolDetail = { spoolId -> 
                        navController.navigate(Routes.spoolDetail(spoolId)) 
                    }
                )
            }
            
            composable(Routes.SPOOLS) {
                SpoolsScreen(
                    onNavigateToDetail = { spoolId -> 
                        navController.navigate(Routes.spoolDetail(spoolId)) 
                    },
                    onNavigateToAdd = { navController.navigate(Routes.SPOOL_ADD) }
                )
            }
            
            composable(
                route = Routes.SPOOL_DETAIL,
                arguments = listOf(navArgument("spoolId") { type = NavType.LongType })
            ) {
                SpoolDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Routes.SPOOL_ADD) {
                SpoolDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Routes.FILAMENT_PROFILES) {
                FilamentProfilesScreen(
                    onNavigateToDetail = { profileId -> 
                        navController.navigate(Routes.filamentProfileDetail(profileId)) 
                    },
                    onNavigateToAdd = { navController.navigate(Routes.FILAMENT_PROFILE_ADD) }
                )
            }
            
            composable(
                route = Routes.FILAMENT_PROFILE_DETAIL,
                arguments = listOf(navArgument("profileId") { type = NavType.LongType })
            ) {
                FilamentProfileDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCalibration = { testType ->
                        navController.navigate(Routes.calibrationWizard(testType))
                    }
                )
            }
            
            composable(Routes.FILAMENT_PROFILE_ADD) {
                FilamentProfileDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCalibration = { testType ->
                        navController.navigate(Routes.calibrationWizard(testType))
                    }
                )
            }
            
            composable(Routes.PRINTER_SETUP) {
                PrinterSetupScreen()
            }
            
            composable(Routes.PRINT_HISTORY) {
                PrintHistoryScreen()
            }
            
            composable(Routes.CALIBRATION_HUB) {
                CalibrationHubScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWizard = { testType ->
                        navController.navigate(Routes.calibrationWizard(testType))
                    }
                )
            }
            
            composable(
                route = Routes.CALIBRATION_WIZARD,
                arguments = listOf(navArgument("testType") { type = NavType.StringType })
            ) { backStackEntry ->
                val testType = backStackEntry.arguments?.getString("testType") ?: "TEMP"
                CalibrationWizardScreen(
                    testType = testType,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
