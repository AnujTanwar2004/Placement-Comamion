package com.example.placementcompanion.repository

import com.example.placementcompanion.data.network.*
import com.example.placementcompanion.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class GroqRepository(
    private val apiService: GroqApiService = RetrofitClient.groqApiService
) {
    // Configured locally; never commit API credentials.
    private val apiKey = BuildConfig.GROQ_API_KEY
    private val authHeader = "Bearer $apiKey"
    private val gson = Gson()

    suspend fun generateStudyPlan(
        company: String,
        daysRemaining: Int,
        hoursPerDay: Int,
        skillLevel: String,
        weakTopics: List<String>,
        strongTopics: List<String>
    ): Result<AiPlanResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("Configure GROQ_API_KEY in local.properties to use AI features."))
        val systemPrompt = """
            You are a senior placement preparation architect. Generate a structured JSON study roadmap for a student preparing for campus placement at $company.
            Return strictly a JSON object with this exact JSON schema:
            {
              "company": "$company",
              "durationDays": $daysRemaining,
              "weeks": [
                {
                  "week": 1,
                  "topics": ["Arrays", "DBMS"],
                  "tasks": [
                    {
                      "title": "Solve 5 Array LeetCode questions",
                      "subject": "DSA",
                      "dayNumber": 1,
                      "durationMinutes": 60,
                      "priority": "High"
                    }
                  ]
                }
              ]
            }
            Do NOT output markdown ticks or extra explanations outside the raw JSON object.
        """.trimIndent()

        val userPrompt = """
            Student Profile:
            - Target Company: $company
            - Days until placement test: $daysRemaining days
            - Available study time: $hoursPerDay hours/day
            - Skill Level: $skillLevel
            - Weak Topics: ${weakTopics.joinToString(", ")}
            - Strong Topics: ${strongTopics.joinToString(", ")}
            Generate a realistic plan divided into weeks covering weak topics first, followed by revision and mock practice.
        """.trimIndent()

        val request = GroqRequest(
            messages = listOf(
                GroqMessage("system", systemPrompt),
                GroqMessage("user", userPrompt)
            ),
            responseFormat = GroqResponseFormat("json_object")
        )

        try {
            val response = apiService.getChatCompletion(authHeader, request)
            if (response.isSuccessful && response.body() != null) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                val cleanJson = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val plan = gson.fromJson(cleanJson, AiPlanResponse::class.java)
                Result.success(plan)
            } else {
                Result.failure(apiError(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askDoubt(question: String, subject: String = "Computer Science"): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("Configure GROQ_API_KEY in local.properties to use AI features."))
        val systemPrompt = "You are a friendly, concise placement prep assistant. Answer the student's question clearly with key bullet points, code snippet or step-by-step explanation where helpful."
        val userPrompt = "Subject: $subject\nQuestion: $question"

        val request = GroqRequest(
            messages = listOf(
                GroqMessage("system", systemPrompt),
                GroqMessage("user", userPrompt)
            )
        )

        try {
            val response = apiService.getChatCompletion(authHeader, request)
            if (response.isSuccessful && response.body() != null) {
                val answer = response.body()?.choices?.firstOrNull()?.message?.content ?: "No answer received."
                Result.success(answer)
            } else {
                Result.failure(apiError(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun mockInterviewFeedback(
        company: String,
        role: String,
        interviewType: String,
        question: String,
        userAnswer: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("Configure GROQ_API_KEY in local.properties to use AI features."))
        val systemPrompt = """
            You are a technical interviewer for $company evaluating a $role candidate during a $interviewType round.
            Review the user's answer to the question: "$question".
            Provide feedback with 3 sections:
            1. Accuracy & Positives
            2. Missing Points & Edge Cases
            3. Model Answer / Suggested Improvement
            Then end with: NEXT QUESTION: <Follow up question>
        """.trimIndent()

        val request = GroqRequest(
            messages = listOf(
                GroqMessage("system", systemPrompt),
                GroqMessage("user", "Candidate Answer: $userAnswer")
            )
        )

        try {
            val response = apiService.getChatCompletion(authHeader, request)
            if (response.isSuccessful && response.body() != null) {
                val answer = response.body()?.choices?.firstOrNull()?.message?.content ?: "No feedback generated."
                Result.success(answer)
            } else {
                Result.failure(apiError(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private fun apiError(response: Response<GroqChatResponse>): Exception {
        val serverMessage = runCatching {
            gson.fromJson(response.errorBody()?.string(), JsonObject::class.java)
                ?.getAsJsonObject("error")
                ?.get("message")
                ?.asString
        }.getOrNull()
        val message = serverMessage?.takeIf { it.isNotBlank() }
            ?: response.message().ifBlank { "Request failed" }
        return Exception("Groq API error (${response.code()}): ${message.replace(apiKey, "<redacted>")}")
    }
}
