package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.util.Log
import com.everlytic.android.pushnotificationsdk.database.EvDbHelper
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticNotSubscribedException
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.models.*
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.JSONAdapter
import com.everlytic.android.pushnotificationsdk.network.EverlyticApi
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import java.util.*

internal class PushSdk @JvmOverloads constructor(
    private val context: Context,
    private val settingsBag: SdkSettings.SdkSettingsBag,
    private val api: EverlyticApi = EverlyticApi(
        EverlyticHttp(
            settingsBag.apiInstall!!,
            settingsBag.apiUsername!!,
            settingsBag.apiKey!!
        )
    ),
    private val firebaseInstanceId: FirebaseInstanceIdFacade = FirebaseInstanceIdFacade.getDefaultInstance(),
    private val sdkRepository: SdkRepository = SdkRepository(context)
) {

    init {
        logd("class::init{}")
        if (sdkRepository.getDeviceId().isNullOrEmpty()) {
            sdkRepository.setDeviceId(UUID.randomUUID().toString())
        }
    }

    fun subscribeContact(email: String, onComplete: (EvResult) -> Unit) {
        logd("::subscribeContact() email=$email onComplete=$onComplete")
        val deviceType = if (context.resources.getBoolean(R.bool.isTablet)) "tablet" else "handset"
        val device = DeviceData(sdkRepository.getDeviceId()!!, type = deviceType)
        firebaseInstanceId.getInstanceId { result ->
            logd("::subscribeContact() firebaseInstanceId->result=$result")
            if (result.success) {
                val contactData = ContactData(email, result.value!!)
                val subscription = SubscriptionEvent(settingsBag.listId.toString(), contactData, device = device)

                logd("::subscribeContact() subscription=$subscription")

                api.subscribe(subscription, object : EverlyticHttp.ResponseHandler {
                    override fun onSuccess(response: ApiResponse?) {
                        logd("::onSuccess() response=$response")
                        val responseObject = JSONAdapter.decodeAs(ApiSubscription::class.java, response!!.data)

                        saveContactSubscriptionFromResponse(responseObject)
                        onComplete(EvResult(true))
                    }

                    override fun onFailure(code: Int, response: String?, throwable: Throwable?) {
                        logd("::onFailure() code=$code response=$response throwable=$throwable")
                        onComplete(
                            EvResult(
                                false,
                                throwable ?: Exception("An API Exception occurred")
                            )
                        )
                    }
                })
            }

        }
    }

    fun resubscribeUser(email: String, onComplete: (EvResult) -> Unit) {
        return subscribeContact(email, onComplete)
    }

    @Suppress("NAME_SHADOWING")
    fun unsubscribeCurrentContact(onComplete: ((EvResult) -> Unit)? = null) {
        val deviceId = sdkRepository.getDeviceId()
        val subscriptionId = sdkRepository.getSubscriptionId()

        deviceId?.let { deviceId ->
            subscriptionId?.let { subscriptionId ->
                val unsubscribeEvent = UnsubscribeEvent(subscriptionId, deviceId)
                api.unsubscribe(unsubscribeEvent, object : EverlyticHttp.ResponseHandler {
                    override fun onSuccess(response: ApiResponse?) {
                        sdkRepository.removeContactSubscription()
                        onComplete?.invoke(EvResult(true))
                    }

                    override fun onFailure(code: Int, response: String?, throwable: Throwable?) {
                        onComplete?.invoke(EvResult(false, throwable ?: Exception("An API Exception occurred")))
                    }
                })
            } ?: onComplete?.invoke(EvResult(false, EverlyticNotSubscribedException("No subscription to unsubscribe.")))
        } ?: onComplete?.invoke(EvResult(false, Exception("No device ID set.")))

    }

    fun getPublicNotificationHistory() : List<EverlyticNotification> {
        val dbHelper = EvDbHelper.getInstance(context)
        val notificationRepository = NotificationLogRepository(dbHelper)

        return notificationRepository.getNotificationLogHistory()
    }

    internal fun saveContactSubscriptionFromResponse(responseBody: ApiSubscription) {
        try {
            Log.d("PushSdk", "API Response: $responseBody")
            sdkRepository.setContactSubscription(responseBody)
        } catch (otherException: Exception) {
            throw otherException
        }
    }

    internal fun isContactSubscribed(): Boolean {
        return sdkRepository.getSubscriptionId() != null && sdkRepository.getContactId() != null
    }
}