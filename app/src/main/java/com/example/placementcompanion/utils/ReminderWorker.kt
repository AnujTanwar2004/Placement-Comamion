package com.example.placementcompanion.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString("TASK_TITLE") ?: "Scheduled Study Task"
        val subject = inputData.getString("TASK_SUBJECT") ?: "Placement Preparation"

        NotificationHelper.createNotificationChannel(applicationContext)
        NotificationHelper.showNotification(
            applicationContext,
            "Reminder: $taskTitle",
            "Time to focus on $subject!"
        )

        return Result.success()
    }
}
