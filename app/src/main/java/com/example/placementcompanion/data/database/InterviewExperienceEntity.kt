package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interview_experiences")
data class InterviewExperienceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyName: String,
    val role: String,
    val studentName: String = "Anonymous",
    val college: String = "",
    val year: Int = 2024,
    val result: String = "Selected",
    val numberOfRounds: Int = 3,
    val difficulty: String = "Medium",
    val experienceText: String,
    val topicsAsked: String = "",
    val date: String = "",
    val helpfulVotes: Int = 0,
    val isBookmarked: Boolean = false
)
