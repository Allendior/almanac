package io.github.allendior.almanac.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

const val REMINDER_CHANNEL_ID = "daily_reminder"
const val REMINDER_NOTIFICATION_ID = 1

/** Idempotent: safe to call on every process start. */
fun ensureReminderChannel(context: Context) {
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        "Daily reminder",
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = "A single reminder, at most once a day, only when today has no portrait yet."
    }
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}
