package com.example.placementcompanion

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.placementcompanion.data.database.*
import com.example.placementcompanion.presentation.ui.HomeScreen
import com.example.placementcompanion.presentation.viewmodel.HomeViewModel
import com.example.placementcompanion.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PreparationProgressTest {
    @get:Rule val compose = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "preparation-progress-test"
    @After fun cleanup() { context.deleteDatabase(name) }
    private fun open() = Room.databaseBuilder(context, AppDatabase::class.java, name)
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).build()
    private fun task(title: String = "Revise arrays") = StudyTaskEntity(
        title = title, subject = "DSA", scheduledDate = LocalDate.now().toString(), timeSlot = "7 PM")
    private fun question(id: Long, solved: Boolean = false) = QuestionEntity(
        id = id, companyId = 1, companyName = "Amazon", title = "Two Sum", description = "Practice",
        topic = "Coding", difficulty = "Easy", year = 0, round = "", role = "", source = "Test",
        companySlug = "amazon", leetcodeId = 1, isSolved = solved)

    @Test fun readinessStartsAtZeroAndUsesActualTaskAndUniqueQuestionCompletion() = runBlocking {
        assertEquals(0, PreparationProgress.from(emptyList(), 0).percent)
        assertEquals(25, PreparationProgress.from(listOf(task(), task("Other").copy(isCompleted = true)), 0).percent)
        assertEquals(50, PreparationProgress.from(listOf(task().copy(isCompleted = true)), 0).percent)
        assertEquals(75, PreparationProgress.from(listOf(task().copy(isCompleted = true)), 10).percent)
        assertEquals(100, PreparationProgress.from(listOf(task().copy(isCompleted = true)), 40).percent)
        val db = open()
        try {
            db.placementDao().insertQuestions(listOf(question(-1, true), question(-2, true).copy(companySlug = "google", companyName = "Google")))
            assertEquals(1, PlacementRepository(db.placementDao()).solvedQuestionCount.first())
        } finally { db.close() }
    }

    @Test fun dailyCheckboxUpdatesSummaryAndCanBeUnchecked() = runBlocking {
        val db = open()
        try {
            val repo = PlacementRepository(db.placementDao())
            val id = repo.addTask(task())
            val model = HomeViewModel(repo)
            compose.setContent { MaterialTheme { HomeScreen(model, {}, {}, {}, {}, {}) } }
            compose.waitUntil(10000) { model.todayTasks.value.size == 1 }
            compose.onNodeWithText("0 / 1 Completed").assertIsDisplayed()
            compose.onNodeWithContentDescription("Complete: Revise arrays").assertIsOff().performClick()
            compose.waitUntil(10000) { model.todayTasks.value.single().isCompleted }
            compose.onNodeWithText("1 / 1 Completed").assertIsDisplayed()
            compose.onNodeWithContentDescription("Mark incomplete: Revise arrays").assertIsOn().performClick()
            compose.waitUntil(10000) { !model.todayTasks.value.single().isCompleted }
            assertFalse(repo.allTasks.first().single { it.id == id }.isCompleted)
        } finally { db.close() }
    }

    @Test fun completedTasksAndQuestionsSurviveAppDatabaseReopen() = runBlocking {
        var db = open()
        val repo = PlacementRepository(db.placementDao())
        val id = repo.addTask(task())
        assertFalse(repo.allTasks.first().single().isCompleted)
        repo.setTaskCompleted(id, true)
        db.placementDao().insertQuestions(listOf(question(-1)))
        repo.toggleQuestionSolved(-1, false)
        assertTrue(repo.questionPage(QuestionFilters()).first().isEmpty())
        db.close()
        db = open()
        try {
            val reopened = PlacementRepository(db.placementDao())
            assertTrue(reopened.allTasks.first().single().isCompleted)
            assertEquals(-1L, reopened.questionPage(QuestionFilters(completion = QuestionCompletion.COMPLETED)).first().single().id)
            reopened.setTaskCompleted(id, false)
            assertFalse(reopened.allTasks.first().single().isCompleted)
        } finally { db.close() }
    }

    @Test fun upgradeRemovesDemoCompletionsAndPreservesPersonalProgress() = runBlocking {
        var db = open()
        val dao = db.placementDao()
        dao.insertTask(task("Revise Binary Search Trees & Balanced Trees").copy(id = 1, scheduledDate = "2025-03-01", isCompleted = true))
        dao.insertTask(task("My own task").copy(id = 9, isCompleted = true))
        dao.insertQuestions(listOf(question(1, true).copy(title = "Trapping Rain Water", source = "Verified Student Submission", leetcodeId = null), question(-1, true).copy(isBookmarked = true, userNote = "Keep this")))
        db.close()
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { it.version = 2 }
        db = open()
        try {
            assertEquals("My own task", db.placementDao().getAllTasks().first().single().title)
            assertTrue(db.placementDao().getAllTasks().first().single().isCompleted)
            val saved = db.placementDao().getAllQuestions().first().single()
            assertEquals(-1L, saved.id); assertTrue(saved.isSolved); assertTrue(saved.isBookmarked)
            assertEquals("Keep this", saved.userNote)
        } finally { db.close() }
    }
}
