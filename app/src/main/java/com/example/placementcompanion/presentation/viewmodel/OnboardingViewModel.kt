package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.UserEntity
import com.example.placementcompanion.repository.PlacementRepository
import com.example.placementcompanion.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: PlacementRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _step = MutableStateFlow(1)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedRole = MutableStateFlow("Software Engineer")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    private val _selectedSubjects = MutableStateFlow(setOf("DSA", "DBMS", "OS", "Aptitude"))
    val selectedSubjects: StateFlow<Set<String>> = _selectedSubjects.asStateFlow()

    private val _targetCompanies = MutableStateFlow("Amazon, Microsoft, Google, TCS")
    val targetCompanies: StateFlow<String> = _targetCompanies.asStateFlow()

    private val _dailyHours = MutableStateFlow(3)
    val dailyHours: StateFlow<Int> = _dailyHours.asStateFlow()

    fun updateName(value: String) { _name.value = value }
    fun updateRole(value: String) { _selectedRole.value = value }
    fun updateTargetCompanies(value: String) { _targetCompanies.value = value }
    fun updateDailyHours(value: Int) { _dailyHours.value = value }

    fun toggleSubject(subject: String) {
        val current = _selectedSubjects.value.toMutableSet()
        if (current.contains(subject)) {
            if (current.size > 1) current.remove(subject)
        } else {
            current.add(subject)
        }
        _selectedSubjects.value = current
    }

    fun nextStep() {
        if (_step.value < 4) {
            _step.value += 1
        }
    }

    fun prevStep() {
        if (_step.value > 1) {
            _step.value -= 1
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = UserEntity(
                id = 1,
                name = _name.value.ifBlank { "Student" },
                preferredRole = _selectedRole.value,
                targetCompanies = _targetCompanies.value,
                dailyStudyHours = _dailyHours.value,
                isOnboardingCompleted = true,
                streakCount = 0,
                lastActiveDate = System.currentTimeMillis().toString()
            )
            repository.saveUser(user)
            dataStoreManager.setOnboardingCompleted(true)
            onSuccess()
        }
    }
}
