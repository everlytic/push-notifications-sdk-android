package com.everlytic.android.pushnotificationsdk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import java.security.SecureRandom

internal object EvNotificationManager {

    private const val DEFAULT_CHANNEL = "ev_ch_default"

    fun displayNotification(context: Context, evNotification: EvNotification) {

        registerChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        val notification = createNotification(context, evNotification)

        notificationManager.notify(evNotification.androidNotificationId, notification)
    }

    private fun registerChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = DEFAULT_CHANNEL
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(DEFAULT_CHANNEL, name, importance).apply {
                description = "Default Notification Channel"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(context: Context, notification: EvNotification): Notification {
        val intent = Intent(context, EvNotificationClickReceiver::class.java).apply {
            putExtra(EvIntentExtras.EVERLYTIC_DATA, notification)
            putExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID, notification.androidNotificationId)
        }

        val onClickIntent =
            PendingIntent.getBroadcast(context, SecureRandom().nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val smallIcon = context
            .resources
            .getIdentifier("ic_ev_notification", "drawable", context.packageName)

        return NotificationCompat.Builder(context, DEFAULT_CHANNEL)
            .setSmallIcon(smallIcon)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setPriority(notification.priority)
            // todo color, dismiss action
            .setContentIntent(onClickIntent)
            .build()
    }

}