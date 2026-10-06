package com.example.placementcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import com.example.placementcompanion.data.database.AppDatabase
import com.example.placementcompanion.presentation.navigation.AppNavGraph
import com.example.placementcompanion.presentation.theme.PlacementCompanionTheme
import com.example.placementcompanion.presentation.viewmodel.*
import com.example.placementcompanion.repository.PlacementRepository
import com.example.placementcompanion.utils.DataStoreManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PlacementRepository(database.placementDao())
        lifecycleScope.launch { repository.loadQuestionCatalog(database, applicationContext) }
        val dataStoreManager = DataStoreManager(applicationContext)

        setContent {
            PlacementCompanionTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val isOnboardingCompleted by dataStoreManager.isOnboardingCompleted.collectAsState(initial = true)

                    val onboardingViewModel = viewModel { OnboardingViewModel(repository, dataStoreManager) }
                    val homeViewModel = viewModel { HomeViewModel(repository) }
                    val plannerViewModel = viewModel { PlannerViewModel(repository) }
                    val companyViewModel = viewModel { CompanyViewModel(repository) }
                    val pyqViewModel = viewModel { PyqViewModel(repository) }
                    val experienceViewModel = viewModel { ExperienceViewModel(repository) }
                    val noteViewModel = viewModel { NoteViewModel(repository) }
                    val resourceViewModel = viewModel { ResourceViewModel(repository) }
                    val calendarViewModel = viewModel { CalendarViewModel(repository) }
                    val aiViewModel = viewModel { AiViewModel() }
                    val searchViewModel = viewModel { SearchViewModel(repository) }
                    val profileViewModel = viewModel { ProfileViewModel(repository, dataStoreManager) }

                    AppNavGraph(
                        navController = navController,
                        onboardingViewModel = onboardingViewModel,
                        homeViewModel = homeViewModel,
                        plannerViewModel = plannerViewModel,
                        companyViewModel = companyViewModel,
                        pyqViewModel = pyqViewModel,
                        experienceViewModel = experienceViewModel,
                        noteViewModel = noteViewModel,
                        resourceViewModel = resourceViewModel,
                        calendarViewModel = calendarViewModel,
                        aiViewModel = aiViewModel,
                        searchViewModel = searchViewModel,
                        profileViewModel = profileViewModel,
                        isOnboardingCompleted = isOnboardingCompleted
                    )
                }
            }
        }
    }
}
