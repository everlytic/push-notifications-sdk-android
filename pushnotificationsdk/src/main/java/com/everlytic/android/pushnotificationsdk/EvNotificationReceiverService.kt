package com.everlytic.android.pushnotificationsdk

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.workers.JobIntentService
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.security.SecureRandom
import java.util.*


internal class EvNotificationReceiverService : FirebaseMessagingService() {

    private val notificationRepository by lazy {
        NotificationLogRepository(getDatabase())
    }

    private val sdkRepository by lazy {
        SdkRepository(getContext())
    }

    private val notificationHandler by lazy {
        EvNotificationHandler(getContext())
    }

    private val notificationEventRepository by lazy {
        NotificationEventRepository(getDatabase(), sdkRepository)
    }

    override fun onNewToken(token: String?) {
        logd("::onNewToken() token=$token")
        // TODO("Implement onNewToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val subscriptionId = sdkRepository.getSubscriptionId() ?: -1L
        val contactId = sdkRepository.getContactId() ?: -1L

        val notification = createEvNotification(remoteMessage.data)

        processDeliveryEventForNotification(notification)

        notificationRepository.storeNotification(notification, subscriptionId, contactId)
        notificationHandler.displayNotification(notification)
    }

    private fun processDeliveryEventForNotification(notification: EvNotification) {
        val event = createDeliveryEvent(notification, sdkRepository)
        notificationEventRepository.storeNotificationEvent(event)
        scheduleEventUploadWorker()
    }

    private fun scheduleEventUploadWorker() {
        val componentName = ComponentName(getContext(), UploadMessageEventsService::class.java)
        JobIntentService.enqueueWork(getContext(), componentName, UploadMessageEventsService.JOB_SERVICE_ID, Intent())
    }

    private fun createDeliveryEvent(notification: EvNotification, sdkRepository: SdkRepository): NotificationEvent {
        return NotificationEvent(
            notification.androidNotificationId,
            sdkRepository.getSubscriptionId() ?: -1,
            notification.messageId,
            metadata = mapOf("displayed" to notificationHandler.canDisplayNotifications().toString()),
            type = NotificationEventType.DELIVERY
        )
    }

    private fun createEvNotification(data: MutableMap<String, String>): EvNotification {
        val androidMessageId = SecureRandom().nextInt()

        return EvNotification(
            data["message_id"]?.toLong() ?: -1L,
            androidMessageId,
            data["title"] ?: "",
            data["body"],
            data["sound"]?.toBoolean() ?: false,
            getColorReference(),
            0,
            0,
            emptyList(),
            Date()
        )
    }

    private fun getColorReference(): Int {
        val ctx = getContext()

        val id = ctx
            .resources
            .getIdentifier("colorPrimary", "color", getContext().packageName)

        return if (Build.VERSION.SDK_INT > 23) {
            ctx.resources.getColor(id, ctx.theme)
        } else {
            ctx.resources.getColor(id)
        }
    }

    private fun getContext() = applicationContext
    private fun getDatabase() = EvDbHelper.getInstance(getContext())

}