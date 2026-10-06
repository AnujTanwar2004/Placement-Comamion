package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.QuestionEntity
import com.example.placementcompanion.data.database.QuestionCatalogEntity
import com.example.placementcompanion.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PyqUiState(val questions: List<QuestionEntity> = emptyList(), val total: Int = 0,
    val loading: Boolean = true, val error: String? = null, val catalog: QuestionCatalogEntity? = null)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PyqViewModel(private val repository: PlacementRepository) : ViewModel() {
    private val _filters = MutableStateFlow(QuestionFilters())
    val filters = _filters.asStateFlow()
    private val limit = MutableStateFlow(100)
    val companies = repository.questionCompanies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val uiState = combine(_filters.debounce(180), limit, repository.catalogState) { filters, limit, catalog -> Triple(filters, limit, catalog) }
        .flatMapLatest { (filters, limit, catalog) ->
            when (catalog) {
                CatalogState.Loading -> flowOf(PyqUiState())
                is CatalogState.Error -> flowOf(PyqUiState(loading = false, error = catalog.message))
                is CatalogState.Ready -> combine(repository.questionPage(filters, limit), repository.questionCount(filters)) { rows, count ->
                    PyqUiState(rows, count, loading = false, catalog = catalog.catalog)
                }.onStart { emit(PyqUiState(catalog = catalog.catalog)) }
            }
        }.catch { emit(PyqUiState(loading = false, error = it.message ?: "Could not search questions")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PyqUiState())
    private fun change(value: QuestionFilters) { limit.value = 100; _filters.value = value }
    fun setSearchQuery(value: String) = change(_filters.value.copy(query = value))
    fun setCompany(value: String) = change(_filters.value.copy(company = value))
    fun setTopic(value: String) = change(_filters.value.copy(topic = value))
    fun setDifficulty(value: String) = change(_filters.value.copy(difficulty = value))
    fun setWindow(value: QuestionWindow) = change(_filters.value.copy(window = value))
    fun setSort(value: QuestionSort) = change(_filters.value.copy(sort = value))
    fun setSavedOnly(value: Boolean) = change(_filters.value.copy(savedOnly = value))
    fun setCompletion(value: QuestionCompletion) = change(_filters.value.copy(completion = value))
    fun clearFilters() = change(QuestionFilters(completion = _filters.value.completion))
    fun loadMore() { limit.value += 100 }
    fun toggleSolved(id: Long, current: Boolean) { viewModelScope.launch { repository.toggleQuestionSolved(id, current) } }
    fun toggleBookmark(id: Long, current: Boolean) { viewModelScope.launch { repository.toggleQuestionBookmark(id, current) } }
}
