package com.hacybeyker.finflow.feature.reminders.data

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hacybeyker.finflow.MainActivity
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Posts the daily "log your movements" notification. It re-checks the preference before notifying:
 * a restored device (WorkManager's DB survives backups the prefs might not) or any missed cancel
 * must never produce a notification the user turned off.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferences: PreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val enabled = preferences.observePreferences().first().reminderEnabled
        if (enabled && hasNotificationPermission()) postNotification()
        // Skipping is a deliberate no-op, not a failed attempt: retry() would re-fire at the
        // backoff's hour instead of the chosen one, and failure() would report a broken reminder
        // that did exactly what it should.
        return Result.success()
    }

    private fun hasNotificationPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    // The permission is checked in hasNotificationPermission(); lint can't follow it across methods.
    @android.annotation.SuppressLint("MissingPermission")
    private fun postNotification() {
        val manager = NotificationManagerCompat.from(applicationContext)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(applicationContext.getString(R.string.reminder_channel_name))
                .setDescription(applicationContext.getString(R.string.reminder_channel_description))
                .build()
        )
        val openApp = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(applicationContext.getString(R.string.reminder_notification_text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "reminders"
        const val NOTIFICATION_ID = 1
    }
}
