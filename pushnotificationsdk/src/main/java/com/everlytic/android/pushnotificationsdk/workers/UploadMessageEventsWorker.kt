package com.everlytic.android.pushnotificationsdk.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.everlytic.android.pushnotificationsdk.NotificationEventsLog
import com.everlytic.android.pushnotificationsdk.SdkSettings
import com.everlytic.android.pushnotificationsdk.database.Database
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.decodeJsonMap
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository


class UploadMessageEventsWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    private lateinit var repository: NotificationEventRepository
    private lateinit var api: EverlyticApi

    override fun doWork(): Result {

        val sdkSettings = SdkSettings.getSettings(applicationContext)

        repository = NotificationEventRepository(
            Database.getInstance(context),
            SdkRepository(context)
        )

        api = EverlyticApi(EverlyticHttp(sdkSettings.apiInstall!!, sdkSettings.apiUsername!!, sdkSettings.apiKey!!))

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
            performUploadForEvent(rawEvent.event_type, notificationEvent, object : EverlyticHttp.ResponseHandler {
                override fun onSuccess(response: ApiResponse?) {
                    repository.updateEventIsUploaded(rawEvent._id, true)
                }

                override fun onFailure(code: Int, response: String?, throwable: Throwable?) {

                }

            })
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

    private fun performUploadForEvent(
        eventType: NotificationEventType,
        event: NotificationEvent,
        responseHandler: EverlyticHttp.ResponseHandler
    ) {
        return when (eventType) {
            NotificationEventType.CLICK -> api.recordClickEvent(event, responseHandler)
            NotificationEventType.DELIVERY -> api.recordDeliveryEvent(event, responseHandler)
            NotificationEventType.DISMISS -> TODO()
            NotificationEventType.BOUNCE -> TODO()
            NotificationEventType.SOFT_BOUNCE -> TODO()
            NotificationEventType.UNKNOWN -> TODO()
        }
    }
}