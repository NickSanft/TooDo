package com.divora.toodo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class CleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CleanupWorkerEntryPoint {
        fun getTaskDao(): TaskDao
    }

    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val taskDao = EntryPointAccessors.fromApplication(
            appContext,
            CleanupWorkerEntryPoint::class.java
        ).getTaskDao()

        val sharedPreferences = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val deleteOption = sharedPreferences.getString("auto_delete_option", "Never")

        if (deleteOption == "Never") {
            return Result.success()
        }

        val cutoffTime = when (deleteOption) {
            "After 1 week" -> System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            "After 1 month" -> System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            else -> return Result.success()
        }

        taskDao.deleteOldCompletedTasks(cutoffTime)

        return Result.success()
    }
}
