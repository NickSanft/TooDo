package com.divora.toodo

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class TooDoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        setupCleanupWorker()
    }

    private fun setupCleanupWorker() {
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleanup_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}
