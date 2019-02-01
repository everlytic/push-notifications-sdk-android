package com.everlytic.android.pushnotificationsdk

import android.content.Context
import com.everlytic.android.pushnotificationsdk.database.Database
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.security.SecureRandom
import java.util.*


internal class EvNotificationReceiverService : FirebaseMessagingService() {

    val context : Context
        get() = applicationContext

    private val notificationRepository by lazy {
        NotificationLogRepository(getDatabase())
    }

    private val sdkRepository by lazy {
        SdkRepository(context)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val subscriptionId = sdkRepository.getSubscriptionId() ?: -1L
        val contactId = sdkRepository.getContactId() ?: -1L

        val notification = createEvNotification(remoteMessage.data)

        notificationRepository.storeNotification(notification, subscriptionId, contactId)

        EvNotificationManager.displayNotification(context, notification)
    }

    private fun createEvNotification(data: MutableMap<String, String>): EvNotification {
        val androidMessageId = SecureRandom().nextInt()

        return EvNotification(
            data["message_id"]?.toLong() ?: -1L,
            androidMessageId,
            data["title"] ?: "",
            data["body"],
            data["sound"]?.toBoolean() ?: false,
            -1,
            -1,
            0,
            emptyList(),
            Date()
        )

    }
    private fun getDatabase() = Database.getInstance(context)

}