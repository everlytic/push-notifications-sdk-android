package com.everlytic.android.pushnotificationsdk.handlers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.everlytic.android.pushnotificationsdk.*
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.GoToUrlNotificationAction
import com.everlytic.android.pushnotificationsdk.models.LaunchAppNotificationAction
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.workers.JobIntentService
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsService

internal class NotificationOpenedHandler(
    private val sdkRepository: SdkRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationLogRepository: NotificationLogRepository,
    private val notificationHandler: EvNotificationHandler
) {

    private val eventType = NotificationEventType.CLICK

    fun handleIntentWithContext(context: Context, intent: Intent) {
        if (!intent.isEverlyticEventIntent()) {
            return
        }

        processIntent(context, intent)
        setNotificationReadFromIntent(intent)
    }

    private fun processIntent(context: Context, intent: Intent) {

        val event = createNotificationEvent(intent, sdkRepository)
        notificationEventRepository.storeNotificationEvent(event)
        scheduleEventUploadWorker(context)

        notificationHandler.dismissNotificationByAndroidId(
            intent.extras?.getInt(EvIntentExtras.ANDROID_NOTIFICATION_ID) ?: 0
        )

        val customParams = intent.extras?.getBundle(EvIntentExtras.CUSTOM_PARAMS_BUNDLE)

        logd("::processIntent() ")

        when (intent.extras.getString(EvIntentExtras.ACTION_TYPE)) {
            LaunchAppNotificationAction.ACTION_ID -> {
                startLauncherActivityInContext(context, customParams)
            }

            GoToUrlNotificationAction.ACTION_ID -> {
                startUriIntentInContext(context, intent.extras.getParcelable(EvIntentExtras.ACTION_URI) as Uri)
            }
        }

    }

    private fun setNotificationReadFromIntent(intent: Intent) {
        val androidNotificationId =
            intent.getParcelableExtra<EvNotification>(EvIntentExtras.EVERLYTIC_DATA).androidNotificationId

        notificationLogRepository.setNotificationAsRead(androidNotificationId)
    }

    private fun startLauncherActivityInContext(context: Context, extras: Bundle?) {
        logd("::startLauncherActivityInContext() extras=$extras")
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT and Intent.FLAG_ACTIVITY_NEW_TASK
            extras?.let { putExtras(it) }
        }?.let {
            context.startActivity(it)
        }
    }

    private fun startUriIntentInContext(context: Context, uri: Uri) {
        try {
            Intent(Intent.ACTION_VIEW, uri).let {
                context.startActivity(it)
            }
        } catch (e: Exception) {
            e.handle()
        }
    }

    private fun createNotificationEvent(
        intent: Intent,
        sdkRepository: SdkRepository
    ): NotificationEvent {
        val notification = intent.getParcelableExtra<EvNotification>(EvIntentExtras.EVERLYTIC_DATA)

        return NotificationEvent(
            notification.androidNotificationId,
            sdkRepository.getSubscriptionId() ?: -1,
            notification.messageId,
            type = eventType
        )
    }

    private fun scheduleEventUploadWorker(context: Context) {
        UploadMessageEventsService.enqueue(context)
    }
}