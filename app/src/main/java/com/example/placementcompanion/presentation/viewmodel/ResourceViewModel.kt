package com.example.placementcompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placementcompanion.data.database.ResourceEntity
import com.example.placementcompanion.repository.PlacementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResourceViewModel(
    private val repository: PlacementRepository
) : ViewModel() {

    val resources: StateFlow<List<ResourceEntity>> = repository.allResources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addResource(title: String, fileUri: String, fileType: String, subject: String, category: String) {
        viewModelScope.launch {
            val resource = ResourceEntity(
                title = title,
                fileUri = fileUri,
                fileType = fileType,
                subject = subject,
                category = category
            )
            repository.addResource(resource)
        }
    }

    fun deleteResource(id: Long) {
        viewModelScope.launch {
            repository.deleteResource(id)
        }
    }
}
