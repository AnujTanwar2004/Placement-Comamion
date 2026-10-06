package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.CompanyEntity
import com.example.placementcompanion.data.database.NoteEntity
import com.example.placementcompanion.data.database.QuestionEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val companyResults: StateFlow<List<CompanyEntity>> = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList())
        else repository.searchCompanies(q)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val questionResults: StateFlow<List<QuestionEntity>> = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList())
        else repository.searchQuestions(q)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val noteResults: StateFlow<List<NoteEntity>> = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList())
        else repository.searchNotes(q)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
}
