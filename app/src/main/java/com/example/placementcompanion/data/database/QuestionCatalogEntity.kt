package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_catalog")
data class QuestionCatalogEntity(
    @PrimaryKey val id: Int = 1,
    val commit: String,
    val snapshotDate: String,
    val companyCount: Int,
    val questionCount: Int
)
