package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.IntentFilter
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
import com.everlytic.android.pushnotificationsdk.eventreceivers.ResubscribeContactOnNetworkChangeReceiver
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticSdkException
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticSubscriptionDelayedException
import java.util.*

/**
 * @suppress
 * */
internal class PushSdk @JvmOverloads constructor(
    private val context: Context,
    private val settingsBag: SdkConfiguration.SdkConfigBag,
    private val api: EverlyticApi = EverlyticApi(
        EverlyticHttp(
            settingsBag.apiInstall,
            settingsBag.pushProjectUuid
        )
    ),
    private val firebaseInstanceId: FirebaseInstanceIdFacade = FirebaseInstanceIdFacade.getDefaultInstance(context),
    private val sdkRepository: SdkRepository = SdkRepository(context),
    private val testMode: Boolean = false
) {

    init {
        logd("class::init{}")
        if (sdkRepository.getDeviceId().isNullOrEmpty()) {
            sdkRepository.setDeviceId(UUID.randomUUID().toString())
        }

        if (sdkRepository.getHasSubscription()) {
            resubscribeIfRequired()
        }

        context.registerReceiver(
            ResubscribeContactOnNetworkChangeReceiver(),
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )

        if (testMode) {
            logi("Everlytic Push SDK initialized with testMode=$testMode")
        }
    }

    fun subscribeContact(uniqueId: String?, email: String?, onComplete: (EvResult) -> Unit) {
        logd("::subscribeContact() email=$email onComplete=$onComplete")

        if (uniqueId.isNullOrBlank() && email.isNullOrBlank()) {
            throw EverlyticSdkException("Both uniqueId and email cannot be blank or null. Please provide a valid uniqueId, email, or both")
        }

        firebaseInstanceId.getInstanceId { tokenResult ->
            logd("::subscribeContact() firebaseInstanceId->tokenResult=$tokenResult")

            if (tokenResult.success && !tokenResult.value.isNullOrBlank()) {
                val subscription = createSubscriptionEvent(uniqueId, email, tokenResult.value)

                logd("::subscribeContact() subscription=$subscription")

                if (isDeviceOnline(context)) {
                    val responseHandler = object : EverlyticHttp.ResponseHandler {
                        override fun onSuccess(response: ApiResponse?) {
                            logd("::onSuccess() response=$response")
                            val responseObject = JSONAdapter.decodeAs(ApiSubscription::class.java, response!!.data)

                            saveContactSubscriptionFromResponse(uniqueId, email, responseObject)
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
                    }

                    if (testMode) {
                        logi("Push Notification Token: ${tokenResult.value}")
                        responseHandler.onSuccess(Testing_ApiResponses.subscribeSuccess(sdkRepository.getDeviceId()!!))
                    } else {
                        api.subscribe(subscription, responseHandler)
                    }

                } else {
                    sdkRepository.setContactDetails(uniqueId, email)
                    sdkRepository.setNewFcmToken(tokenResult.value)
                    onComplete(
                        EvResult(false, EverlyticSubscriptionDelayedException())
                    )
                }

            }
        }
    }

    internal fun resubscribeUserWithToken(uniqueId: String?, email: String?, token: String, onComplete: (EvResult) -> Unit) {
        logd("::resubscribeUserWithToken() email=$email token=$token onComplete=$onComplete")

        if (uniqueId.isNullOrBlank() && email.isNullOrBlank()) {
            throw EverlyticSdkException("Both uniqueId and email cannot be blank or null. Please provide a valid uniqueId, email, or both")
        }

        val subscription = createSubscriptionEvent(uniqueId, email, token)

        logd("::resubscribeUserWithToken() subscription=$subscription")

        val responseHandler = object : EverlyticHttp.ResponseHandler {
            override fun onSuccess(response: ApiResponse?) {
                logd("::onSuccess() response=$response")
                val responseObject = JSONAdapter.decodeAs(ApiSubscription::class.java, response!!.data)
                saveContactSubscriptionFromResponse(uniqueId,  email, responseObject)
                sdkRepository.removeNewFcmToken()
                onComplete(EvResult(true))
            }

            override fun onFailure(code: Int, response: String?, throwable: Throwable?) {
                logd("::onFailure() code=$code response=$response throwable=$throwable")
                sdkRepository.removeNewFcmToken()
                onComplete(
                    EvResult(
                        false,
                        throwable ?: Exception("An API Exception occurred")
                    )
                )
            }
        }

        if (testMode) {
            responseHandler.onSuccess(Testing_ApiResponses.subscribeSuccess(sdkRepository.getDeviceId()!!))
        } else {
            api.subscribe(subscription, responseHandler)
        }
    }

    internal fun resubscribeIfRequired() {
        logd("::resubscribeIfRequired()")
        if (sdkRepository.getHasSubscription()) {
            val newToken = sdkRepository.getNewFcmToken()
            newToken?.let { fcmToken ->
                if (fcmToken.datetime.after(sdkRepository.getSubscriptionDatetime() ?: Date(0))) {
                    val email = sdkRepository.getContactEmail()
                    val uniqueId = sdkRepository.getContactUniqueId()
                    resubscribeUserWithToken(uniqueId, email, fcmToken.token) {
                        logd(throwable = it.exception)
                    }
                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    fun unsubscribeCurrentContact(onComplete: ((EvResult) -> Unit)? = null) {
        val deviceId = sdkRepository.getDeviceId()
        val subscriptionId = sdkRepository.getSubscriptionId()

        deviceId?.let { deviceId ->
            subscriptionId?.let { subscriptionId ->
                val unsubscribeEvent = UnsubscribeEvent(subscriptionId, deviceId)
                val responseHandler = object : EverlyticHttp.ResponseHandler {
                    override fun onSuccess(response: ApiResponse?) {
                        sdkRepository.removeContactSubscription()
                        getNotificationLogRepository().clearNotificationLogHistory()
                        onComplete?.invoke(EvResult(true))
                    }

                    override fun onFailure(code: Int, response: String?, throwable: Throwable?) {
                        onComplete?.invoke(EvResult(false, throwable ?: Exception("An API Exception occurred")))
                    }
                }
                if (testMode) {
                    responseHandler.onSuccess(Testing_ApiResponses.unsubscribeSuccess())
                } else {
                    api.unsubscribe(unsubscribeEvent, responseHandler)
                }
            } ?: onComplete?.invoke(EvResult(false, EverlyticNotSubscribedException("No subscription to unsubscribe.")))
        } ?: onComplete?.invoke(EvResult(false, Exception("No device ID set.")))

    }

    fun getPublicNotificationHistory(): List<EverlyticNotification> {
        val notificationRepository = getNotificationLogRepository()
        return notificationRepository.getPublicNotificationLogHistory()
    }

    fun getNotificationHistoryCount(): Int {
        val notificationRepository = getNotificationLogRepository()
        return notificationRepository.getNotificationHistoryCount()
    }

    private fun getNotificationLogRepository(): NotificationLogRepository {
        val dbHelper = EvDbHelper.getInstance(context)
        return NotificationLogRepository(dbHelper)
    }

    internal fun saveContactSubscriptionFromResponse(contactUniqueId: String?, contactEmail: String?, responseBody: ApiSubscription) {
        try {
            Log.d("PushSdk", "API Response: $responseBody")
            sdkRepository.setContactSubscription(contactUniqueId, contactEmail, responseBody)
        } catch (otherException: Exception) {
            otherException.handle()
        }
    }

    internal fun isContactSubscribed(): Boolean {
        return sdkRepository.getHasSubscription()
    }

    private fun createSubscriptionEvent(
        uniqueId: String?,
        email: String?,
        token: String
    ): SubscriptionEvent {
        val deviceType = if (context.resources.getBoolean(R.bool.isTablet)) "tablet" else "handset"
        val device = DeviceData(sdkRepository.getDeviceId()!!, type = deviceType)
        val contactData = ContactData(uniqueId, email, token)
        return SubscriptionEvent(settingsBag.pushProjectUuid, contactData, device = device)
    }
}