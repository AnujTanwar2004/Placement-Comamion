package com.example.placementcompanion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.placementcompanion.presentation.navigation.BottomNavigationBar
import com.example.placementcompanion.presentation.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BottomNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var navController: NavHostController

    @Before
    fun setUp() {
        compose.setContent {
            navController = rememberNavController()
            MaterialTheme {
                Scaffold(bottomBar = { BottomNavigationBar(navController) }) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Screen.Home.route) { Text("Dashboard screen") }
                        composable(Screen.Preparation.route) { Text("Planner screen") }
                        composable(Screen.Companies.route) { Text("Companies screen") }
                        composable(Screen.Resources.route) { Text("Resources screen") }
                        composable(Screen.Profile.route) { Text("Profile screen") }
                        composable(Screen.AiPlanner.route) { Text("AI planner screen") }
                    }
                }
            }
        }
    }

    @Test
    fun homeReturnsFromPlannerOpenedByStudyAction() {
        compose.runOnIdle { navController.navigate(Screen.Preparation.route) }
        compose.onNodeWithText("Planner screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Home", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Dashboard screen").assertIsDisplayed()
    }

    @Test
    fun homeReturnsFromAiPlannerOpenedByQuickAction() {
        compose.runOnIdle { navController.navigate(Screen.AiPlanner.route) }
        compose.onNodeWithText("AI planner screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Home", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Dashboard screen").assertIsDisplayed()
    }

    @Test
    fun homeReturnsToDashboardFromPlanner() {
        compose.onNodeWithContentDescription("Planner", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Planner screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Home", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Dashboard screen").assertIsDisplayed()
    }

    @Test
    fun homeReturnsToDashboardAfterSwitchingTabs() {
        compose.onNodeWithContentDescription("Planner", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Companies", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Companies screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Home", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Dashboard screen").assertIsDisplayed()
    }

    @Test
    fun homeReturnsFromAiPlannerAndPlannerKeepsItsState() {
        compose.onNodeWithContentDescription("Planner", useUnmergedTree = true).performClick()
        compose.runOnIdle { navController.navigate(Screen.AiPlanner.route) }
        compose.onNodeWithText("AI planner screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Companies", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Planner", useUnmergedTree = true).performClick()
        compose.onNodeWithText("AI planner screen").assertIsDisplayed()
        compose.onNodeWithContentDescription("Home", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Dashboard screen").assertIsDisplayed()
    }
}
