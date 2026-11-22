package com.group04.materialexchange.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** WorkManager can run this reminder even when the app screen is not open. */
class ExchangeReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            ExchangeNotifications.showReminder(applicationContext)
            Result.success()
        } catch (_: SecurityException) {
            // Notification permission may have been denied; do not keep retrying forever.
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
