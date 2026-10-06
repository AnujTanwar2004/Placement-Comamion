package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // COMPANY, QUESTION, NOTE, EXPERIENCE, RESOURCE
    val itemId: Long,
    val title: String,
    val subtitle: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
