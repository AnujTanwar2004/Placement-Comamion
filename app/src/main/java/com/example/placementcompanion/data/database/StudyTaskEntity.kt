package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val companyName: String = "",
    val scheduledDate: String,
    val timeSlot: String,
    val durationMinutes: Int = 60,
    val priority: String = "Medium",
    val isCompleted: Boolean = false,
    val isAiGenerated: Boolean = false,
    val reminderEnabled: Boolean = false,
    val weekNumber: Int = 1
)
