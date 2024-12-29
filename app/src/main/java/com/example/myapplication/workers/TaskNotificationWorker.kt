package com.example.myapplication.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.utils.NotificationHelper

class TaskNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val taskTitle = inputData.getString("taskTitle") ?: return Result.failure()
        val taskDescription = inputData.getString("taskDescription") ?: "High Priority Task"

        NotificationHelper.showNotification(context, taskTitle, taskDescription)

        return Result.success()
    }
}
