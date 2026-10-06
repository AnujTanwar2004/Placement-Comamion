package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Student",
    val college: String = "Engineering College",
    val course: String = "B.Tech Computer Science",
    val graduationYear: String = "2025",
    val preferredRole: String = "Software Engineer",
    val targetCompanies: String = "Amazon, Microsoft, Google, TCS",
    val dailyStudyHours: Int = 3,
    val isOnboardingCompleted: Boolean = false,
    val streakCount: Int = 0,
    val lastActiveDate: String = ""
)
