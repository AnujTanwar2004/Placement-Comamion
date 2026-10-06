package com.example.placementcompanion.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.placementcompanion.presentation.ui.*
import com.example.placementcompanion.presentation.viewmodel.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onboardingViewModel: OnboardingViewModel,
    homeViewModel: HomeViewModel,
    plannerViewModel: PlannerViewModel,
    companyViewModel: CompanyViewModel,
    pyqViewModel: PyqViewModel,
    experienceViewModel: ExperienceViewModel,
    noteViewModel: NoteViewModel,
    resourceViewModel: ResourceViewModel,
    calendarViewModel: CalendarViewModel,
    aiViewModel: AiViewModel,
    searchViewModel: SearchViewModel,
    profileViewModel: ProfileViewModel,
    isOnboardingCompleted: Boolean
) {
    val startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToTasks = { navController.navigate(Screen.Preparation.route) },
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                    onNavigateToAi = { navController.navigate(Screen.AiPlanner.route) },
                    onNavigateToCompanies = { navController.navigate(Screen.Companies.route) },
                    onNavigateToSearch = { navController.navigate(Screen.SearchBookmarks.route) }
                )
            }

            composable(Screen.Preparation.route) {
                PlannerScreen(
                    viewModel = plannerViewModel,
                    onNavigateToAiPlanner = { navController.navigate(Screen.AiPlanner.route) }
                )
            }

            composable(Screen.Companies.route) {
                CompanyListScreen(
                    viewModel = companyViewModel,
                    onCompanyClick = { companyId ->
                        navController.navigate(Screen.CompanyDetail.createRoute(companyId))
                    }
                )
            }

            composable(
                route = Screen.CompanyDetail.route,
                arguments = listOf(navArgument("companyId") { type = NavType.LongType })
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getLong("companyId") ?: 1L
                CompanyDetailScreen(
                    companyId = companyId,
                    viewModel = companyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Resources.route) {
                ResourceScreen(viewModel = resourceViewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToPyqs = { navController.navigate(Screen.PyqList.route) },
                    onNavigateToExperiences = { navController.navigate(Screen.Experiences.route) },
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                    onNavigateToAiDoubt = { navController.navigate("ai_doubt") },
                    onNavigateToMockInterview = { navController.navigate(Screen.MockInterview.route) },
                    onResetOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Secondary Screens
            composable(Screen.AiPlanner.route) {
                AiPlanScreen(
                    viewModel = plannerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PyqList.route) {
                PyqScreen(
                    viewModel = pyqViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Experiences.route) {
                ExperienceScreen(
                    viewModel = experienceViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notes.route) {
                NoteScreen(
                    viewModel = noteViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SearchBookmarks.route) {
                SearchBookmarksScreen(
                    viewModel = searchViewModel,
                    onBack = { navController.popBackStack() },
                    onBrowseQuestions = { query ->
                        pyqViewModel.clearFilters()
                        pyqViewModel.setCompletion(com.example.placementcompanion.repository.QuestionCompletion.PENDING)
                        pyqViewModel.setSearchQuery(query)
                        navController.navigate(Screen.PyqList.route)
                    }
                )
            }

            composable("ai_doubt") {
                AiAssistantScreen(
                    viewModel = aiViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MockInterview.route) {
                MockInterviewScreen(
                    viewModel = aiViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
