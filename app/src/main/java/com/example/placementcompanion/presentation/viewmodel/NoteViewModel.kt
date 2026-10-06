package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.NoteEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) || it.subject.contains(query, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addNote(title: String, content: String, subject: String, companyName: String = "", isPinned: Boolean = false) {
        viewModelScope.launch {
            val note = NoteEntity(
                title = title,
                content = content,
                subject = subject,
                companyName = companyName,
                isPinned = isPinned
            )
            repository.addNote(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }
}
