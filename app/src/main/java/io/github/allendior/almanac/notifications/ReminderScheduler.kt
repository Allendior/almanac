package io.github.allendior.almanac.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

private const val UNIQUE_WORK_NAME = "daily-reminder"

/**
 * Schedules (or cancels) the once-a-day reminder check. There is no exact-alarm
 * permission anywhere in this app, so the fire time is best-effort — WorkManager's own
 * periodic-work window, which can drift under Doze. For a reminder whose own copy says
 * "whenever suits you", that trade is the right one.
 */
object ReminderScheduler {

    fun schedule(context: Context, minuteOfDay: Int, replaceExisting: Boolean) {
        val delay = delayUntil(minuteOfDay)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay)
            .build()
        val policy = if (replaceExisting) {
            ExistingPeriodicWorkPolicy.UPDATE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, policy, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private fun delayUntil(minuteOfDay: Int): Duration {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.ofSecondOfDay(minuteOfDay * 60L)
        var target = LocalDateTime.of(LocalDate.now(), targetTime)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target)
    }
}
