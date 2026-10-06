package com.example.placementcompanion.repository

import com.example.placementcompanion.data.database.StudyTaskEntity
import kotlin.math.roundToInt

/** Activity progress, based only on explicitly recorded completion; not an assessment of skill. */
data class PreparationProgress(
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val solvedQuestions: Int = 0,
    val questionGoal: Int = 20,
    val subjects: List<SubjectProgress> = emptyList()
) {
    val taskFraction: Float get() = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks
    val questionFraction: Float get() = (solvedQuestions.toFloat() / questionGoal).coerceIn(0f, 1f)
    val fraction: Float get() = (taskFraction + questionFraction) / 2f
    val percent: Int get() = (fraction * 100).roundToInt()

    companion object {
        fun from(tasks: List<StudyTaskEntity>, solved: Int) = PreparationProgress(
            completedTasks = tasks.count { it.isCompleted }, totalTasks = tasks.size, solvedQuestions = solved,
            subjects = tasks.groupBy { it.subject.trim().ifEmpty { "Other" }.uppercase() }.toSortedMap()
                .map { (subject, items) -> SubjectProgress(subject, items.count { it.isCompleted }, items.size) }
        )
    }
}

data class SubjectProgress(val subject: String, val completed: Int, val total: Int)
