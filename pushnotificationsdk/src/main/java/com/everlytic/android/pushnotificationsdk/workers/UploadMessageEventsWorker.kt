package com.everlytic.android.pushnotificationsdk.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.everlytic.android.pushnotificationsdk.NotificationEventsLog
import com.everlytic.android.pushnotificationsdk.SdkSettings
import com.everlytic.android.pushnotificationsdk.database.Database
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.decodeJsonMap
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import retrofit2.Call



class UploadMessageEventsWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    private lateinit var repository: NotificationEventRepository
    private lateinit var api: EverlyticApi

    override fun doWork(): Result {

        val sdkSettings = SdkSettings.getSettings(applicationContext)

        repository = NotificationEventRepository(
            Database.getInstance(context),
            SdkRepository(context)
        )

        api = EverlyticHttp()
            .buildEverlyticApi(
                sdkSettings.apiInstall!!,
                sdkSettings.apiUsername!!,
                sdkSettings.apiKey!!
            )

        val events = NotificationEventType.values().flatMap {
            repository.getAllPendingEventsForType(it)
        }

        events.forEach(::processEventUpload)

        Database.releaseInstance()

        return Result.success()
    }

    private fun processEventUpload(rawEvent: NotificationEventsLog) {

        val notificationEvent = createNotificationEvent(rawEvent)

        try {
            createUploadCallForEvent(rawEvent.event_type, notificationEvent)?.let {
                it.execute()
                repository.updateEventIsUploaded(rawEvent._id, true)
            }
        } catch (exception: Exception) {
            repository.updateEventIsUploaded(rawEvent._id, false)
        }
    }

    private fun createNotificationEvent(event: NotificationEventsLog): NotificationEvent {

        val jsonObject = decodeJsonMap(event.metadata)

        return NotificationEvent(
            event.android_notification_id,
            event.subscription_id,
            event.message_id,
            jsonObject
        )
    }

    private fun createUploadCallForEvent(
        eventType: NotificationEventType,
        event: NotificationEvent
    ): Call<*>? {
        return when (eventType) {
            NotificationEventType.CLICK -> api.recordClickEvent(event)
            NotificationEventType.DELIVERY -> api.recordDeliveryEvent(event)
            NotificationEventType.DISMISS -> TODO()
            NotificationEventType.BOUNCE -> TODO()
            NotificationEventType.SOFT_BOUNCE -> TODO()
            NotificationEventType.UNKNOWN -> TODO()
        }
    }
}