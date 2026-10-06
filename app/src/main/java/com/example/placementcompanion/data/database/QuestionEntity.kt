package com.example.placementcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(tableName = "company_questions", indices = [
    Index(value = ["companySlug", "leetcodeId"], unique = true),
    Index(value = ["companyName"]), Index(value = ["difficulty"])
])
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val companyName: String,
    val title: String,
    val description: String,
    val topic: String,
    val difficulty: String,
    val year: Int,
    val round: String,
    val role: String,
    val source: String,
    val solutionLink: String = "",
    val tags: String = "",
    val isSolved: Boolean = false,
    val isBookmarked: Boolean = false,
    val userNote: String = "",
    @ColumnInfo(defaultValue = "''") val companySlug: String = "",
    val leetcodeId: Int? = null,
    @ColumnInfo(defaultValue = "''") val snapshotDate: String = "",
    val acceptanceRate: Double? = null,
    val frequencyAll: Double? = null,
    val frequency30Days: Double? = null,
    val frequency3Months: Double? = null,
    val frequency6Months: Double? = null,
    val frequencyOlder: Double? = null
)
