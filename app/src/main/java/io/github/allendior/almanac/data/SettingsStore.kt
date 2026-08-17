package io.github.allendior.almanac.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Which alignment aid is drawn over the viewfinder. Never over the file. */
enum class FramingGuide(val label: String) {
    GHOST("Ghost"),
    GRID("Grid"),
    OFF("No guide");

    fun next(): FramingGuide = entries[(ordinal + 1) % entries.size]

    companion object {
        fun from(raw: String?): FramingGuide = entries.firstOrNull { it.name == raw } ?: GHOST
    }
}

data class Settings(
    val guide: FramingGuide = FramingGuide.GHOST,
    val biometricLock: Boolean = false,
    val numberLabel: String = DEFAULT_NUMBER_LABEL,
    val hasSeenWelcome: Boolean = false,
    val hasSeenIntroduction: Boolean = false,
    val notificationsEnabled: Boolean = true,
    /** Minutes since midnight, local time. Default 20:00. */
    val notificationMinuteOfDay: Int = DEFAULT_NOTIFICATION_MINUTE,
    /** True once the one-time, automatic POST_NOTIFICATIONS prompt has been shown. */
    val hasRequestedNotificationPermission: Boolean = false,
) {
    companion object {
        const val DEFAULT_NUMBER_LABEL = "Weight (kg)"
        const val DEFAULT_NOTIFICATION_MINUTE = 20 * 60
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "almanac_settings")

/**
 * Preferences only — no portrait data, no metadata. Excluded from cloud backup and
 * device transfer along with everything else the app owns.
 */
class SettingsStore(private val context: Context) {

    private val guideKey = stringPreferencesKey("framing_guide")
    private val lockKey = booleanPreferencesKey("biometric_lock")
    private val numberLabelKey = stringPreferencesKey("number_label")
    private val welcomeSeenKey = booleanPreferencesKey("has_seen_welcome")
    private val introductionSeenKey = booleanPreferencesKey("has_seen_introduction")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val notificationMinuteKey = intPreferencesKey("notification_minute_of_day")
    private val notificationPermissionRequestedKey = booleanPreferencesKey("notification_permission_requested")

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            guide = FramingGuide.from(prefs[guideKey]),
            biometricLock = prefs[lockKey] ?: false,
            numberLabel = prefs[numberLabelKey]?.takeIf { it.isNotBlank() }
                ?: Settings.DEFAULT_NUMBER_LABEL,
            hasSeenWelcome = prefs[welcomeSeenKey] ?: false,
            hasSeenIntroduction = prefs[introductionSeenKey] ?: false,
            notificationsEnabled = prefs[notificationsEnabledKey] ?: true,
            notificationMinuteOfDay = prefs[notificationMinuteKey] ?: Settings.DEFAULT_NOTIFICATION_MINUTE,
            hasRequestedNotificationPermission = prefs[notificationPermissionRequestedKey] ?: false,
        )
    }

    suspend fun setGuide(guide: FramingGuide) {
        context.dataStore.edit { it[guideKey] = guide.name }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { it[lockKey] = enabled }
    }

    suspend fun setNumberLabel(label: String) {
        context.dataStore.edit { it[numberLabelKey] = label.trim() }
    }

    suspend fun setHasSeenWelcome(seen: Boolean) {
        context.dataStore.edit { it[welcomeSeenKey] = seen }
    }

    suspend fun setHasSeenIntroduction(seen: Boolean) {
        context.dataStore.edit { it[introductionSeenKey] = seen }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[notificationsEnabledKey] = enabled }
    }

    suspend fun setNotificationMinuteOfDay(minute: Int) {
        context.dataStore.edit { it[notificationMinuteKey] = minute.coerceIn(0, 23 * 60 + 59) }
    }

    suspend fun setNotificationPermissionRequested(requested: Boolean) {
        context.dataStore.edit { it[notificationPermissionRequestedKey] = requested }
    }
}
