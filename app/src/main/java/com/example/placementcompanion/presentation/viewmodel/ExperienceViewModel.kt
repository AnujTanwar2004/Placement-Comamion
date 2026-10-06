package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.InterviewExperienceEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExperienceViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    private val _searchCompany = MutableStateFlow("")
    val searchCompany: StateFlow<String> = _searchCompany.asStateFlow()

    val experiences: StateFlow<List<InterviewExperienceEntity>> = repository.allExperiences
        .combine(_searchCompany) { list, company ->
            if (company.isBlank()) list
            else list.filter { it.companyName.contains(company, ignoreCase = true) || it.role.contains(company, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchCompany(query: String) { _searchCompany.value = query }

    fun addExperience(
        companyName: String,
        role: String,
        studentName: String,
        college: String,
        result: String,
        numberOfRounds: Int,
        difficulty: String,
        experienceText: String,
        topicsAsked: String
    ) {
        viewModelScope.launch {
            val exp = InterviewExperienceEntity(
                companyName = companyName,
                role = role,
                studentName = studentName.ifBlank { "Anonymous" },
                college = college,
                result = result,
                numberOfRounds = numberOfRounds,
                difficulty = difficulty,
                experienceText = experienceText,
                topicsAsked = topicsAsked,
                date = "2025"
            )
            repository.addExperience(exp)
        }
    }

    fun upvote(id: Long) {
        viewModelScope.launch {
            repository.upvoteExperience(id)
        }
    }
}
