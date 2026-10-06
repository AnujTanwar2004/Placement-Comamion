package com.example.placementcompanion.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Planner", Screen.Preparation.route, Icons.Default.DateRange),
        BottomNavItem("Companies", Screen.Companies.route, Icons.Default.Business),
        BottomNavItem("Resources", Screen.Resources.route, Icons.Default.Folder),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on onboarding screen
    if (currentRoute == Screen.Onboarding.route) return

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            // Home anchors the stack; restoring it can reopen a saved planner screen.
                            restoreState = item.route != Screen.Home.route
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}
