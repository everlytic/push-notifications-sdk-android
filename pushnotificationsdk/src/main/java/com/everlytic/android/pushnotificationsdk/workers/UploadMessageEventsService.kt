package com.everlytic.android.pushnotificationsdk.workers

import android.content.Intent
import com.everlytic.android.pushnotificationsdk.SdkSettings
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.logw
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository

class UploadMessageEventsService : JobIntentService() {
    private lateinit var repository: NotificationEventRepository
    private lateinit var api: EverlyticApi

    override fun onHandleWork(intent: Intent) {
        val sdkSettings = SdkSettings.getSettings(applicationContext)

        repository = NotificationEventRepository(
            EvDbHelper.getInstance(applicationContext),
            SdkRepository(applicationContext)
        )

        api = EverlyticApi(EverlyticHttp(sdkSettings.apiInstall!!, sdkSettings.apiUsername!!, sdkSettings.apiKey!!))

        NotificationEventType
            .values()
            .flatMap {
                repository.getAllPendingEventsForType(it)
            }
            .forEach(::processEventUpload)
    }

    private fun processEventUpload(notificationEvent: NotificationEvent) {

        try {
            performUploadForEvent(notificationEvent.type, notificationEvent, object : EverlyticHttp.ResponseHandler {
                override fun onSuccess(response: ApiResponse?) {
                    notificationEvent._id?.let { id ->
                        repository.updateEventIsUploaded(id, true)
                    }
                }

                override fun onFailure(code: Int, response: String?, throwable: Throwable?) {
                    logw(throwable = throwable)
                }

            })
        } catch (exception: Exception) {
            notificationEvent._id?.let { id ->
                repository.updateEventIsUploaded(id, false)
            }
        }
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
            NotificationEventType.UNKNOWN -> {
            }
        }
    }

    companion object {
        const val JOB_SERVICE_ID = 2018746654
    }
}