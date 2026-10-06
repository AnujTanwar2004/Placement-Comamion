package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val logoUrl: String = "",
    val description: String,
    val companyType: String, // Product, Service, Startup, Core
    val hiringRoles: String,
    val eligibilityCriteria: String,
    val expectedSkills: String,
    val recruitmentProcess: String,
    val commonSubjects: String,
    val difficulty: String,
    val preparationStrategy: String,
    val isBookmarked: Boolean = false
)
