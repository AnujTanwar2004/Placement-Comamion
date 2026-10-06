package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.CompanyEntity
import com.example.placementcompanion.data.database.StudyTaskEntity
import com.example.placementcompanion.data.database.UserEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    val user: StateFlow<UserEntity?> = repository.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val today = flow {
        while (true) {
            emit(LocalDate.now().toString())
            delay(30_000)
        }
    }.distinctUntilChanged()

    val todayTasks: StateFlow<List<StudyTaskEntity>> = today.flatMapLatest(repository::getTasksForDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val companies: StateFlow<List<CompanyEntity>> = repository.allCompanies.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(taskId, isCompleted)
        }
    }
}
