package com.example.placementcompanion.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.placementcompanion.data.database.QuestionEntity
import java.util.Locale

enum class QuestionWindow(val label: String, val column: String?) {
    ALL("All time", null), DAYS_30("30 days", "frequency30Days"), MONTHS_3("3 months", "frequency3Months"),
    MONTHS_6("6 months", "frequency6Months"), OLDER("Older than 6 months", "frequencyOlder")
}

enum class QuestionSort(val label: String) { FREQUENCY("Most frequent"), TITLE("Title A–Z"), DIFFICULTY("Easy first") }
enum class QuestionCompletion { PENDING, COMPLETED, ALL }

data class QuestionFilters(
    val query: String = "", val company: String = "All companies", val topic: String = "All",
    val difficulty: String = "All", val window: QuestionWindow = QuestionWindow.ALL,
    val sort: QuestionSort = QuestionSort.FREQUENCY, val savedOnly: Boolean = false,
    val completion: QuestionCompletion = QuestionCompletion.PENDING
)

fun QuestionEntity.frequency(window: QuestionWindow): Double? = when (window) {
    QuestionWindow.ALL -> frequencyAll
    QuestionWindow.DAYS_30 -> frequency30Days
    QuestionWindow.MONTHS_3 -> frequency3Months
    QuestionWindow.MONTHS_6 -> frequency6Months
    QuestionWindow.OLDER -> frequencyOlder
}

/** Bound query arguments allow company/title tokens in any order and literal %/_ searches. */
object QuestionQueries {
    fun build(filters: QuestionFilters, limit: Int = 100, count: Boolean = false): SimpleSQLiteQuery {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<Any>()
        val tokens = filters.query.lowercase(Locale.ROOT).trim().split(Regex("\\s+"))
            .filter { it.isNotBlank() && it !in setOf("pyq", "pyqs", "question", "questions", "of", "for", "company", "interview") }
        for (token in tokens) {
            clauses.add("lower(title || ' ' || companyName || ' ' || companySlug || ' ' || topic || ' ' || tags) LIKE ? ESCAPE '\\'")
            args.add("%" + token.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%")
        }
        if (filters.company != "All companies") { clauses.add("companyName = ?"); args.add(filters.company) }
        if (filters.topic != "All") { clauses.add("topic = ?"); args.add(filters.topic) }
        if (filters.difficulty != "All") { clauses.add("difficulty = ?"); args.add(filters.difficulty) }
        filters.window.column?.let { clauses.add("$it IS NOT NULL") }
        if (filters.savedOnly) clauses.add("isBookmarked = 1")
        when (filters.completion) {
            QuestionCompletion.PENDING -> clauses.add("isSolved = 0")
            QuestionCompletion.COMPLETED -> clauses.add("isSolved = 1")
            QuestionCompletion.ALL -> Unit
        }
        val where = if (clauses.isEmpty()) "" else " WHERE " + clauses.joinToString(" AND ")
        if (count) return SimpleSQLiteQuery("SELECT COUNT(*) FROM company_questions$where", args.toTypedArray())
        val frequency = filters.window.column ?: "frequencyAll"
        val order = when (filters.sort) {
            QuestionSort.FREQUENCY -> "COALESCE($frequency, -1) DESC, title COLLATE NOCASE, companyName COLLATE NOCASE, id"
            QuestionSort.TITLE -> "title COLLATE NOCASE, companyName COLLATE NOCASE, id"
            QuestionSort.DIFFICULTY -> "CASE difficulty WHEN 'Easy' THEN 0 WHEN 'Medium' THEN 1 ELSE 2 END, COALESCE($frequency, -1) DESC, title COLLATE NOCASE, id"
        }
        args.add(limit.coerceIn(1, 50000))
        return SimpleSQLiteQuery("SELECT * FROM company_questions$where ORDER BY $order LIMIT ?", args.toTypedArray())
    }
}
