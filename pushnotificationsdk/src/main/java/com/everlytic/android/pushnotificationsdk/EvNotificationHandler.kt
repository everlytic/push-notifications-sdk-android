package com.everlytic.android.pushnotificationsdk

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import java.security.SecureRandom

internal class EvNotificationHandler(val context: Context) {

    fun displayNotification(evNotification: EvNotification) {

        registerChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        val notification = createNotification(evNotification)

        notificationManager.notify(evNotification.androidNotificationId, notification)
    }

    private fun registerChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = DEFAULT_CHANNEL_NAME
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

    private fun createNotification(notification: EvNotification): Notification {
        val intent = createLauncherIntent(notification)

        val onClickIntent = createPendingIntent(intent)

        val smallIcon = getSmallIconReference()

        return NotificationCompat.Builder(context, DEFAULT_CHANNEL)
            .setSmallIcon(smallIcon)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setPriority(notification.priority)
            .setGroup(DEFAULT_GROUP)
            .setColor(notification.color)
            // todo dismiss action
            .setContentIntent(onClickIntent)
            .build()
    }

    private fun getSmallIconReference(): Int {
        return context
            .resources
            .getIdentifier("ic_ev_notification", "drawable", context.packageName)
    }

    private fun createPendingIntent(intent: Intent) =
        PendingIntent.getBroadcast(context, SecureRandom().nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

    private fun createLauncherIntent(notification: EvNotification): Intent {
        return Intent(context, EvNotificationClickReceiver::class.java).apply {
            putExtra(EvIntentExtras.EVERLYTIC_DATA, notification)
            putExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID, notification.androidNotificationId)
        }
    }

    fun dismissNotificationByAndroidId(int: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(int)
    }

    fun canDisplayNotifications(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    companion object {
        private const val DEFAULT_CHANNEL = "com.everlytic.android.pushnotificationsdk.DEFAULT_CHANNEL"
        private const val DEFAULT_CHANNEL_NAME = "All Notifications"
        private const val DEFAULT_GROUP = "com.everlytic.android.pushnotificationsdk.DEFAULT_GROUP"
    }

}