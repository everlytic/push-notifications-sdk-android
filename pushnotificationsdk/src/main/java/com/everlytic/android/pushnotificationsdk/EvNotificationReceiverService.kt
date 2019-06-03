package com.everlytic.android.pushnotificationsdk

import android.net.Uri
import android.os.Build
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.everlytic.android.pushnotificationsdk.handlers.NotificationDeliveredHandler
import com.everlytic.android.pushnotificationsdk.models.GoToUrlNotificationAction
import com.everlytic.android.pushnotificationsdk.models.LaunchAppNotificationAction
import com.everlytic.android.pushnotificationsdk.models.NotificationAction
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.security.SecureRandom
import java.util.*

/**
 * @suppress
 * */
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

    private val notificationDeliveredHandler by lazy {
        NotificationDeliveredHandler(
            getContext(),
            sdkRepository,
            notificationEventRepository,
            notificationHandler
        )
    }

    override fun onNewToken(token: String?) {
        logd("::onNewToken() token=$token")
        val email = sdkRepository.getContactEmail()
        if (sdkRepository.getHasSubscription() && !email.isNullOrBlank()) {
            token?.let { newToken ->
                sdkRepository.setNewFcmToken(newToken)
                EverlyticPush.instance?.resubscribeIfRequired()
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val subscriptionId = sdkRepository.getSubscriptionId() ?: -1L
        val contactId = sdkRepository.getContactId() ?: -1L

        val notification = createEvNotification(remoteMessage.data)

        notificationDeliveredHandler.processDeliveryEventForNotification(notification)

        notificationRepository.storeNotification(notification, subscriptionId, contactId)
        notificationHandler.displayNotification(notification)
    }

    private fun createEvNotification(data: MutableMap<String, String>): EvNotification {
        val androidMessageId = SecureRandom().nextInt()

        val customParameters = decodeCustomParameters(data)
        val customActions = decodeCustomActions(data)

        return EvNotification(
            data["message_id"]?.toLong() ?: -1L,
            androidMessageId,
            data["title"] ?: "",
            data["body"],
            data["sound"]?.toBoolean() ?: false,
            getThemeColor(),
            0,
            0,
            customActions,
            customParameters,
            Date()
        )
    }

    private fun decodeCustomActions(data: MutableMap<String, String>): List<NotificationAction> {
        return data
            .filterKeys {
                it.startsWith(EvNotification.ACTION_PREFIX)
            }
            .mapNotNull {
                logd("::decodeCustomActions() key=${it.key} value=${it.value}")
                val actionType =
                    NotificationAction.Action.getValue(it.key.removePrefix(EvNotification.ACTION_PREFIX))

                it.value.let { action ->
                    val actionParams = action.substring(
                        action.indexOf(NotificationAction.ACTION_ID_DELIMITER) + 1
                    )
                    logd("::decodeCustomActions() action=$action")
                    when {
                        action.startsWith(LaunchAppNotificationAction.ACTION_ID) -> {
                            logd("::decodeCustomActions() LaunchAppNotificationAction action=$actionType params=$actionParams")
                            LaunchAppNotificationAction(actionType, actionParams)
                        }
                        action.startsWith(GoToUrlNotificationAction.ACTION_ID) -> {
                            val (title, url) = decodeMarkdownUrlFragments(actionParams)
                            logd("::decodeCustomActions() GoToUrlNotificationAction action=$actionType url=$url")
                            GoToUrlNotificationAction(actionType, title, Uri.parse(url))
                        }
                        else -> null
                    }
                }
            }
    }

    private fun decodeMarkdownUrlFragments(actionParams: String): Pair<String, String> {
        val markdownUrlRegex = "^\\[([\\p{L}\\s]*)\\]\\(([a-z]+:\\/\\/.+)\\)\$".toRegex()

        return markdownUrlRegex
            .find(actionParams)!!
            .groupValues
            .drop(1)
            .let { matches -> matches.first() to matches.last() }
    }

    private fun decodeCustomParameters(data: Map<String, String>): Map<String, String> {
        return data
            .filterKeys {
                it.startsWith(EvNotification.CUSTOM_PARAM_DELIMITER)
            }
            .map {
                it.key.removePrefix(EvNotification.CUSTOM_PARAM_DELIMITER) to it.value
            }
            .toMap()
    }

    private fun getThemeColor(): Int {
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