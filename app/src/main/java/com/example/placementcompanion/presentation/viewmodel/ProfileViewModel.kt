package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.UserEntity
import com.example.placementcompanion.repository.PlacementRepository
import com.example.placementcompanion.repository.PreparationProgress
import com.example.placementcompanion.utils.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: PlacementRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    val progress = combine(repository.allTasks, repository.solvedQuestionCount) { tasks, solved ->
        PreparationProgress.from(tasks, solved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreparationProgress())

    val user: StateFlow<UserEntity?> = repository.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun resetOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(false)
            onSuccess()
        }
    }
}
