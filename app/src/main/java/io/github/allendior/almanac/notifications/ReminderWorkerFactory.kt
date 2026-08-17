package io.github.allendior.almanac.notifications

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.github.allendior.almanac.data.PortraitRepository
import io.github.allendior.almanac.data.SettingsStore

/** Manual DI, matching the rest of the app: WorkManager needs a factory to hand
 * [ReminderWorker] the same repository and settings store as everything else. */
class ReminderWorkerFactory(
    private val repository: PortraitRepository,
    private val settingsStore: SettingsStore,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        ReminderWorker::class.java.name -> ReminderWorker(appContext, workerParameters, repository, settingsStore)
        else -> null
    }
}
