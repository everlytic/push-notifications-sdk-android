package com.everlytic.android.pushnotificationsdk.workers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.database.NotificationEventType
import com.everlytic.android.pushnotificationsdk.isDeviceOnline
import com.everlytic.android.pushnotificationsdk.logw
import com.everlytic.android.pushnotificationsdk.models.ApiResponse
import com.everlytic.android.pushnotificationsdk.models.NotificationEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.NotificationEventRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import org.json.JSONObject

/**
 * @suppress
 * */
class UploadMessageEventsService : JobIntentService() {
    private lateinit var repository: NotificationEventRepository
    private lateinit var api: EverlyticApi

    override fun onHandleWork(intent: Intent) {

        if (!isDeviceOnline(applicationContext)) {
            return
        }

        val sdkSettings = EverlyticPush.sdkSettingsBag ?: return

        repository = NotificationEventRepository(
            EvDbHelper.getInstance(applicationContext),
            SdkRepository(applicationContext)
        )

        api = EverlyticApi(EverlyticHttp(sdkSettings.apiInstall, sdkSettings.pushProjectUuid))

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
                        repository.deleteNotificationEvent(id)
                    }
                }

                override fun onFailure(code: Int, response: String?, throwable: Throwable?) {
                    logw(throwable = throwable)
                }

            })
        } catch (exception: Exception) {
            logw(throwable = exception)
        }
    }


    private fun performUploadForEvent(
        eventType: NotificationEventType,
        event: NotificationEvent,
        responseHandler: EverlyticHttp.ResponseHandler
    ) {

        if (EverlyticPush.isInTestMode) {
            responseHandler.onSuccess(ApiResponse("success", JSONObject()))
            return
        }

        return when (eventType) {
            NotificationEventType.CLICK -> api.recordClickEvent(event, responseHandler)
            NotificationEventType.DELIVERY -> api.recordDeliveryEvent(event, responseHandler)
            NotificationEventType.DISMISS -> api.recordDismissEvent(event, responseHandler)
            NotificationEventType.UNKNOWN -> {
            }
        }
    }

    companion object {

        fun enqueue(context: Context) {
            val componentName = ComponentName(context, UploadMessageEventsService::class.java)
            JobIntentService.enqueueWork(context, componentName, UploadMessageEventsService.JOB_SERVICE_ID, Intent())
        }

        private const val JOB_SERVICE_ID = 2018746654
    }
}