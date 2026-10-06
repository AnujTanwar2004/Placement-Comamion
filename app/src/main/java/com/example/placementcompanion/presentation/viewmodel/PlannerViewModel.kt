package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.StudyTaskEntity
import com.example.placementcompanion.data.network.AiPlanResponse
import com.example.placementcompanion.repository.GroqRepository
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlannerViewModel(
    private val repository: PlacementRepository,
    private val groqRepository: GroqRepository = GroqRepository()
) : ViewModel() {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _selectedDate = MutableStateFlow(sdf.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val allTasks: StateFlow<List<StudyTaskEntity>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // AI Plan Mode State
    private val _aiPlanState = MutableStateFlow<AiPlanState>(AiPlanState.Idle)
    val aiPlanState: StateFlow<AiPlanState> = _aiPlanState.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun addTask(
        title: String,
        subject: String,
        companyName: String = "",
        scheduledDate: String,
        timeSlot: String,
        durationMinutes: Int = 60,
        priority: String = "Medium"
    ) {
        viewModelScope.launch {
            val task = StudyTaskEntity(
                title = title,
                subject = subject,
                companyName = companyName,
                scheduledDate = scheduledDate,
                timeSlot = timeSlot,
                durationMinutes = durationMinutes,
                priority = priority
            )
            repository.addTask(task)
        }
    }

    fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(taskId, isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun generateAiPlan(
        company: String,
        daysRemaining: Int,
        hoursPerDay: Int,
        level: String,
        weakTopics: List<String>,
        strongTopics: List<String>
    ) {
        _aiPlanState.value = AiPlanState.Loading
        viewModelScope.launch {
            val result = groqRepository.generateStudyPlan(
                company = company,
                daysRemaining = daysRemaining,
                hoursPerDay = hoursPerDay,
                skillLevel = level,
                weakTopics = weakTopics,
                strongTopics = strongTopics
            )

            result.onSuccess { planResponse ->
                _aiPlanState.value = AiPlanState.Success(planResponse)
            }.onFailure { error ->
                _aiPlanState.value = AiPlanState.Error(error.localizedMessage ?: "Failed to generate AI plan")
            }
        }
    }

    fun convertAiPlanToTasks(planResponse: AiPlanResponse) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val tasksToInsert = mutableListOf<StudyTaskEntity>()

            planResponse.weeks.forEach { weekPlan ->
                weekPlan.tasks.forEach { aiTask ->
                    val dateForTask = sdf.format(calendar.time)
                    tasksToInsert.add(
                        StudyTaskEntity(
                            title = aiTask.title,
                            subject = aiTask.subject,
                            companyName = planResponse.company,
                            scheduledDate = dateForTask,
                            timeSlot = "07:00 PM - 08:00 PM",
                            durationMinutes = aiTask.durationMinutes,
                            priority = aiTask.priority,
                            isAiGenerated = true,
                            weekNumber = weekPlan.week
                        )
                    )
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (tasksToInsert.isNotEmpty()) {
                repository.addTasks(tasksToInsert)
            }
        }
    }
}

sealed class AiPlanState {
    object Idle : AiPlanState()
    object Loading : AiPlanState()
    data class Success(val plan: AiPlanResponse) : AiPlanState()
    data class Error(val message: String) : AiPlanState()
}
