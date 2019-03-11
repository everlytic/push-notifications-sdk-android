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
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticSubscriptionDelayedException
import java.net.UnknownHostException
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

        if (sdkRepository.getHasSubscription()) {
            resubscribeIfRequired()
        }

        context.registerReceiver(
            ResubscribeContactOnNetworkChangeReceiver(),
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
    }

    fun subscribeContact(email: String, onComplete: (EvResult) -> Unit) {
        logd("::subscribeContact() email=$email onComplete=$onComplete")
        val deviceType = if (context.resources.getBoolean(R.bool.isTablet)) "tablet" else "handset"
        val device = DeviceData(sdkRepository.getDeviceId()!!, type = deviceType)
        firebaseInstanceId.getInstanceId { tokenResult ->
            logd("::subscribeContact() firebaseInstanceId->tokenResult=$tokenResult")
            if (tokenResult.success) {
                val contactData = ContactData(email, tokenResult.value!!)
                val subscription = SubscriptionEvent(settingsBag.listId.toString(), contactData, device = device)

                logd("::subscribeContact() subscription=$subscription")

                if (isDeviceOnline(context)) {
                    api.subscribe(subscription, object : EverlyticHttp.ResponseHandler {
                        override fun onSuccess(response: ApiResponse?) {
                            logd("::onSuccess() response=$response")
                            val responseObject = JSONAdapter.decodeAs(ApiSubscription::class.java, response!!.data)

                            saveContactSubscriptionFromResponse(email, responseObject)
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
                } else {
                    sdkRepository.setContactEmail(email)
                    sdkRepository.setNewFcmToken(tokenResult.value)
                    onComplete(
                        EvResult(false, EverlyticSubscriptionDelayedException())
                    )
                }

            }
        }
    }

    internal fun resubscribeUserWithToken(email: String, token: String, onComplete: (EvResult) -> Unit) {
        logd("::resubscribeUserWithToken() email=$email token=$token onComplete=$onComplete")
        val deviceType = if (context.resources.getBoolean(R.bool.isTablet)) "tablet" else "handset"
        val device = DeviceData(sdkRepository.getDeviceId()!!, type = deviceType)
        val contactData = ContactData(email, token)
        val subscription = SubscriptionEvent(settingsBag.listId.toString(), contactData, device = device)

        logd("::resubscribeUserWithToken() subscription=$subscription")

        api.subscribe(subscription, object : EverlyticHttp.ResponseHandler {
            override fun onSuccess(response: ApiResponse?) {
                logd("::onSuccess() response=$response")
                val responseObject = JSONAdapter.decodeAs(ApiSubscription::class.java, response!!.data)
                saveContactSubscriptionFromResponse(email, responseObject)
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
        })
    }

    internal fun resubscribeIfRequired() {
        logd("::resubscribeIfRequired()")
        if (sdkRepository.getHasSubscription()) {
            val newToken = sdkRepository.getNewFcmToken()
            newToken?.let { fcmToken ->
                if (fcmToken.datetime.after(sdkRepository.getSubscriptionDatetime() ?: Date(0))) {
                    val email = sdkRepository.getContactEmail()!!
                    resubscribeUserWithToken(email, fcmToken.token) {
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

    fun getPublicNotificationHistory(): List<EverlyticNotification> {
        val dbHelper = EvDbHelper.getInstance(context)
        val notificationRepository = NotificationLogRepository(dbHelper)

        return notificationRepository.getNotificationLogHistory()
    }

    internal fun saveContactSubscriptionFromResponse(contactEmail: String, responseBody: ApiSubscription) {
        try {
            Log.d("PushSdk", "API Response: $responseBody")
            sdkRepository.setContactSubscription(contactEmail, responseBody)
        } catch (otherException: Exception) {
            throw otherException
        }
    }

    internal fun isContactSubscribed(): Boolean {
        return sdkRepository.getSubscriptionId() != null && sdkRepository.getContactId() != null
    }
}