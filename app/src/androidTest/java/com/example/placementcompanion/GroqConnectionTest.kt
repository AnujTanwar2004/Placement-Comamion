package com.example.placementcompanion

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.placementcompanion.repository.GroqRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Live checks, enabled explicitly with the runGroqLive=true instrumentation argument. */
@RunWith(AndroidJUnit4::class)
class GroqConnectionTest {
    private val repository = GroqRepository()

    @Before
    fun requireLiveCheck() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runGroqLive") == "true")
    }

    @Test
    fun assistantReceivesReply() = runBlocking {
        val result = repository.askDoubt("Reply with exactly: CONNECTION_OK")
        assertTrue(result.exceptionOrNull()?.message, result.isSuccess)
        assertEquals("CONNECTION_OK", result.getOrThrow().trim())
    }

    @Test
    fun studyPlanReceivesStructuredTasks() = runBlocking {
        val result = repository.generateStudyPlan(
            company = "Amazon",
            daysRemaining = 1,
            hoursPerDay = 1,
            skillLevel = "Beginner",
            weakTopics = listOf("Arrays"),
            strongTopics = emptyList()
        )
        assertTrue(result.exceptionOrNull()?.message, result.isSuccess)
        val plan = result.getOrThrow()
        assertEquals("Amazon", plan.company)
        assertEquals(1, plan.durationDays)
        assertTrue(plan.weeks.isNotEmpty())
        assertTrue(plan.weeks.flatMap { it.tasks }.any { it.title.isNotBlank() })
    }

    @Test
    fun interviewReceivesFeedback() = runBlocking {
        val result = repository.mockInterviewFeedback(
            company = "Amazon",
            role = "Software Engineer",
            interviewType = "Technical",
            question = "What is a stack?",
            userAnswer = "A stack is a last-in, first-out data structure with push and pop operations."
        )
        assertTrue(result.exceptionOrNull()?.message, result.isSuccess)
        assertTrue(result.getOrThrow().isNotBlank())
    }
}
