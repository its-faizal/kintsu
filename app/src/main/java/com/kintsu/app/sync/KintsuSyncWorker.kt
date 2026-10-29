package com.kintsu.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kintsu.app.data.KintsuDatabase
import com.kintsu.app.data.SettingsDataStore
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class KintsuSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val PERIODIC_WORK_NAME = "kintsu_periodic_cloud_sync"
        const val IMMEDIATE_WORK_NAME = "kintsu_immediate_cloud_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<KintsuSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun enqueueImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<KintsuSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE_WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val database = KintsuDatabase.getDatabase(applicationContext)
        val settingsStore = SettingsDataStore(applicationContext)
        val settings = settingsStore.settingsFlow.first()

        if (!settings.syncEnabled || settings.accountAuthToken.isNullOrBlank()) {
            return Result.success()
        }

        val cloudClient = KintsuCloudClient(
            endpointUrl = settings.cloudEndpointUrl,
            apiKey = settings.cloudApiKey
        )

        val syncEngine = KintsuSyncEngine(
            database = database,
            settingsDataStore = settingsStore,
            cloudClient = cloudClient
        )

        val result = syncEngine.performSync()
        return if (result.isSuccess) {
            Result.success()
        } else {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
