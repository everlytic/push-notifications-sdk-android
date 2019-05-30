package com.everlytic.android.pushnotificationsdk


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.everlytic.android.pushnotificationsdk.eventreceivers.EvNotificationClickReceiver
import com.everlytic.android.pushnotificationsdk.eventreceivers.EvNotificationDismissedReceiver
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.GoToUrlNotificationAction
import com.everlytic.android.pushnotificationsdk.models.LaunchAppNotificationAction
import com.everlytic.android.pushnotificationsdk.models.NotificationAction
import java.security.SecureRandom

/**
 * @suppress
 * */
class EvNotificationHandler(val context: Context) {
    fun displayNotification(evNotification: EvNotification) {
        registerChannel()

        val notification = createSystemNotification(evNotification)

        getNotificationManager().notify(evNotification.androidNotificationId, notification)
    }
    private fun registerChannel() {
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

    private fun createSystemNotification(notification: EvNotification): Notification {

        val dismissalIntent = createDismissalIntent(notification)

        val onDismissPendingIntent = createPendingIntent(dismissalIntent)

        val smallIcon = getSmallIconReference()

        return NotificationCompat.Builder(context, DEFAULT_CHANNEL).apply {

            setSmallIcon(smallIcon)
            setContentTitle(notification.title)
            setContentText(notification.body)
            setPriority(notification.priority)
            setGroup(DEFAULT_GROUP)
            setColor(notification.color)
            setDeleteIntent(onDismissPendingIntent)
            setWhen(notification.received_at.time)

            notification.actions.firstOrNull { it.action == NotificationAction.Action.DEFAULT }.let {

                when (it) {
                    null, is LaunchAppNotificationAction -> {
                        val launcherIntent = createLauncherIntent(notification)
                            .addCustomParameters(notification)
                        logd("->setContentIntent() launcherIntent=$launcherIntent launcherIntent.extras=${launcherIntent.extras}")
                        setContentIntent(createPendingIntent(launcherIntent))
                    }

                    is GoToUrlNotificationAction -> {
                        val intent = createUriIntent(notification, it.uri)
                        setContentIntent(createPendingIntent(intent))
                    }
                }
            }

        }.build()
    }

    private fun getSmallIconReference(): Int {

        fun getReference(resource: String): Int {
            return context
                .resources
                .getIdentifier(resource, "drawable", context.packageName)
        }

        var ic = getReference("ic_ev_notification_small")

        if (ic == 0) ic = getReference("ic_ev_fallback_small")

        return ic
    }

    private fun createPendingIntent(intent: Intent) =
        PendingIntent.getBroadcast(context, SecureRandom().nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

    private fun createLauncherIntent(notification: EvNotification): Intent {
        return Intent(context, EvNotificationClickReceiver::class.java).apply {
            putExtra(EvIntentExtras.ACTION_TYPE, LaunchAppNotificationAction.ACTION_ID)
            putExtra(EvIntentExtras.EVERLYTIC_DATA, notification)
            putExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID, notification.androidNotificationId)
        }
    }

    private fun createUriIntent(notification: EvNotification, uri: Uri): Intent {
        return Intent(context, EvNotificationClickReceiver::class.java).apply {
            putExtra(EvIntentExtras.ACTION_TYPE, GoToUrlNotificationAction.ACTION_ID)
            putExtra(EvIntentExtras.EVERLYTIC_DATA, notification)
            putExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID, notification.androidNotificationId)
            putExtra(EvIntentExtras.ACTION_URI, uri)
        }
    }

    private fun createDismissalIntent(notification: EvNotification): Intent {
        return Intent(context, EvNotificationDismissedReceiver::class.java).apply {
            putExtra(EvIntentExtras.EVERLYTIC_DATA, notification)
            putExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID, notification.androidNotificationId)
        }
    }

    fun dismissNotificationByAndroidId(int: Int) {
        getNotificationManager().cancel(int)
    }

    fun canDisplayNotifications(): Boolean {
        return getNotificationManager().areNotificationsEnabled()
    }

    private fun getNotificationManager() = NotificationManagerCompat.from(context)

    companion object {
        private const val DEFAULT_CHANNEL = "com.everlytic.android.pushnotificationsdk.DEFAULT_CHANNEL"
        private const val DEFAULT_CHANNEL_NAME = "All Notifications"
        private const val DEFAULT_GROUP = "com.everlytic.android.pushnotificationsdk.DEFAULT_GROUP"
    }

    private fun Intent.addCustomParameters(notification: EvNotification): Intent {
        val bundle = Bundle().apply {
            notification.customParameters.forEach {
                logd("::addCustomParameters() key=${it.key} value=${it.value}")
                putString(it.key, it.value)
            }
        }

        return this.apply {
            putExtra(EvIntentExtras.CUSTOM_PARAMS_BUNDLE, bundle)
        }
    }
}
