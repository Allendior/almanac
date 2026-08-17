package io.github.allendior.almanac.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.allendior.almanac.MainActivity
import io.github.allendior.almanac.R
import io.github.allendior.almanac.data.PortraitRepository
import io.github.allendior.almanac.data.SettingsStore
import io.github.allendior.almanac.domain.DayId
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Runs at most once a day. Checks quietly and, if today already has a portrait or
 * reminders are off, does nothing at all — there is no "you missed yesterday" catch-up,
 * on purpose.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: PortraitRepository,
    private val settingsStore: SettingsStore,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsStore.settings.first()
        if (!settings.notificationsEnabled) return Result.success()

        val today = DayId.of(LocalDate.now())
        if (repository.findAllByDay(today).isNotEmpty()) return Result.success()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }

        val openApp = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle("Almanac")
            .setContentText("One portrait, whenever suits you today.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(REMINDER_NOTIFICATION_ID, notification)
        return Result.success()
    }
}
