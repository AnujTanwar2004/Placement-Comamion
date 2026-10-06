package com.example.placementcompanion.data.network

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model") val model: String = "openai/gpt-oss-120b",
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Double = 0.5,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat? = null
)

data class GroqResponseFormat(
    @SerializedName("type") val type: String = "json_object"
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqChatResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("choices") val choices: List<GroqChoice>?
)

data class GroqChoice(
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String?
)

// Structured AI Plan response objects parsed from JSON format
data class AiPlanResponse(
    val company: String = "",
    val durationDays: Int = 30,
    val weeks: List<AiWeekPlan> = emptyList()
)

data class AiWeekPlan(
    val week: Int = 1,
    val topics: List<String> = emptyList(),
    val tasks: List<AiTaskItem> = emptyList()
)

data class AiTaskItem(
    val title: String,
    val subject: String,
    val dayNumber: Int = 1,
    val durationMinutes: Int = 60,
    val priority: String = "Medium"
)
