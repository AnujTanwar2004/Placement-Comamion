package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fileUri: String,
    val fileType: String,
    val subject: String,
    val category: String = "Notes",
    val companyName: String = "",
    val addedDate: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isFavorite: Boolean = false
)
