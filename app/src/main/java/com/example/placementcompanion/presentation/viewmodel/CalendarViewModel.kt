package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.StudyTaskEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _selectedDate = MutableStateFlow(sdf.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val tasksForSelectedDate: StateFlow<List<StudyTaskEntity>> = _selectedDate.flatMapLatest { date ->
        repository.getTasksForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(taskId, isCompleted)
        }
    }
}
