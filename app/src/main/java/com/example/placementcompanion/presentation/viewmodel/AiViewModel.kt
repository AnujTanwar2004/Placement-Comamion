package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.repository.GroqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "User" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AiViewModel(
    private val groqRepository: GroqRepository = GroqRepository()
) : ViewModel() {

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("AI", "Hello! I'm your Groq Placement AI Assistant. Ask me anything about Data Structures, System Design, Operating Systems, DBMS, or HR interview questions!")
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mock Interview State
    private val _mockFeedback = MutableStateFlow("")
    val mockFeedback: StateFlow<String> = _mockFeedback.asStateFlow()

    fun askDoubt(question: String, subject: String = "CS Fundamentals") {
        if (question.isBlank()) return

        val currentList = _chatHistory.value.toMutableList()
        currentList.add(ChatMessage("User", question))
        _chatHistory.value = currentList

        _isLoading.value = true
        viewModelScope.launch {
            val result = groqRepository.askDoubt(question, subject)
            _isLoading.value = false

            result.onSuccess { answer ->
                val updated = _chatHistory.value.toMutableList()
                updated.add(ChatMessage("AI", answer))
                _chatHistory.value = updated
            }.onFailure { err ->
                val updated = _chatHistory.value.toMutableList()
                updated.add(ChatMessage("AI", "Error: ${err.localizedMessage}"))
                _chatHistory.value = updated
            }
        }
    }

    fun submitMockAnswer(company: String, role: String, round: String, question: String, answer: String) {
        if (answer.isBlank()) return

        _isLoading.value = true
        viewModelScope.launch {
            val result = groqRepository.mockInterviewFeedback(company, role, round, question, answer)
            _isLoading.value = false

            result.onSuccess { feedback ->
                _mockFeedback.value = feedback
            }.onFailure { err ->
                _mockFeedback.value = "Error generating feedback: ${err.localizedMessage}"
            }
        }
    }
}
