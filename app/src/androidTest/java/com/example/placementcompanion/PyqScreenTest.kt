package com.example.placementcompanion

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.placementcompanion.data.database.AppDatabase
import com.example.placementcompanion.presentation.ui.PyqScreen
import com.example.placementcompanion.presentation.viewmodel.PyqViewModel
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PyqScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var database: AppDatabase
    private lateinit var model: PyqViewModel
    @Before fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repository = PlacementRepository(database.placementDao())
        repository.loadQuestionCatalog(database, context)
        model = PyqViewModel(repository)
        compose.setContent { MaterialTheme { PyqScreen(model, {}) } }
        compose.waitUntil(20000) { !model.uiState.value.loading }
    }
    @After fun close() { database.close() }
    private fun search(query: String) {
        compose.onNode(hasSetTextAction()).performTextReplacement(query)
        compose.waitUntil(15000) { !model.uiState.value.loading && model.filters.value.query == query && model.uiState.value.questions.any { it.title == "Two Sum" } && model.uiState.value.questions.all { it.companyName == "Amazon" } }
        compose.waitForIdle()
    }
    @Test fun searchesCompanyOfflineAndTracksSolvedAndSavedQuestion() {
        search("pyqs of Amazon Two Sum")
        compose.onNodeWithText("Two Sum").assertIsDisplayed()
        val q = model.uiState.value.questions.single { it.leetcodeId == 1 }
        val ancestor = hasAnyAncestor(hasTestTag("question-${q.id}"))
        compose.onNode(hasText("Open in LeetCode") and ancestor).assertIsDisplayed()
        compose.onNode(hasContentDescription("Mark solved") and ancestor).performClick()
        compose.waitUntil(10000) { !model.uiState.value.loading && model.uiState.value.questions.none { it.id == q.id } }
        compose.onNodeWithTag("question-${q.id}").assertDoesNotExist()
        compose.onNodeWithText("Completed").performClick()
        compose.waitUntil(10000) { !model.uiState.value.loading && model.uiState.value.questions.any { it.id == q.id && it.isSolved } }
        compose.onNode(hasContentDescription("Mark unsolved") and ancestor).assertIsDisplayed()
        compose.onNode(hasContentDescription("Bookmark") and ancestor).performClick()
        compose.waitUntil(10000) { model.uiState.value.questions.single { it.id == q.id }.isBookmarked }
        compose.onNodeWithText("Bookmarked").performClick()
        compose.waitUntil(10000) { !model.uiState.value.loading && model.uiState.value.total == 1 }
        compose.onNodeWithText("1 matching questions · Showing 1").assertIsDisplayed()
        compose.onNodeWithText("Two Sum").assertIsDisplayed()
        compose.onNode(hasContentDescription("Mark unsolved") and ancestor).performClick()
        compose.waitUntil(10000) { !model.uiState.value.loading && model.uiState.value.total == 0 }
        compose.onNodeWithText("To solve").performClick()
        compose.waitUntil(10000) { !model.uiState.value.loading && model.uiState.value.questions.any { it.id == q.id && !it.isSolved } }
        compose.onNodeWithText("Two Sum").assertIsDisplayed()
    }
    @Test fun filtersRecentCompanyQuestionsAndClearsFilters() {
        compose.onNodeWithText("Filters").performClick()
        compose.onNodeWithText("30 days").performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("Amazon")
        compose.waitUntil(15000) { !model.uiState.value.loading && model.uiState.value.total == 153 }
        compose.onNodeWithText("153 matching questions · Showing 100").assertIsDisplayed()
        compose.onNodeWithText("Reset").performClick()
        compose.waitUntil(15000) { !model.uiState.value.loading && model.uiState.value.total == 17826 }
        compose.onNodeWithText("17826 matching questions · Showing 100").assertIsDisplayed()
    }
}
