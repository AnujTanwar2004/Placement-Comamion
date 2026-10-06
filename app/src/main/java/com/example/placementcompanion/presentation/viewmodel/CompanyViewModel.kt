package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.CompanyEntity
import com.example.placementcompanion.data.database.QuestionEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompanyViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val companies: StateFlow<List<CompanyEntity>> = repository.allCompanies.combine(_searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.name.contains(query, ignoreCase = true) || it.hiringRoles.contains(query, ignoreCase = true) }
    }.combine(_selectedFilter) { list, filter ->
        if (filter == "All") list
        else list.filter { it.companyType.equals(filter, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleBookmark(companyId: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleCompanyBookmark(companyId, isBookmarked)
        }
    }

    suspend fun getCompanyById(id: Long): CompanyEntity? {
        return repository.getCompanyById(id)
    }

    fun getQuestionsForCompany(companyId: Long): Flow<List<QuestionEntity>> {
        return repository.getQuestionsByCompany(companyId)
    }
}
