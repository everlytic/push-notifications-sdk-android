package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.util.Log
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticNotSubscribedException
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.models.*
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import retrofit2.Response
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class PushSdk constructor(
    private val context: Context,
    private val settingsBag: SdkSettings.SdkSettingsBag,
    httpBuilder: EverlyticHttp = EverlyticHttp(),
    private val firebaseInstanceId: FirebaseInstanceIdFacade = FirebaseInstanceIdFacade.getDefaultInstance(),
    private val sdkRepository: SdkRepository = SdkRepository(context)
) {

    private val api =
        httpBuilder.buildEverlyticApi(settingsBag.apiInstall!!, settingsBag.apiUsername!!, settingsBag.apiKey!!)

    init {
        if (sdkRepository.getDeviceId().isNullOrEmpty()) {
            sdkRepository.setDeviceId(UUID.randomUUID().toString())
        }
    }

    suspend fun subscribeContact(email: String) {
        return suspendCoroutine { continuation ->
            runBlocking {
                val deviceType = if (context.resources.getBoolean(R.bool.isTablet)) "tablet" else "handset"
                val device = DeviceData(sdkRepository.getDeviceId()!!, type = deviceType)
                val firebaseToken = firebaseInstanceId.getInstanceId()

                val contactData = ContactData(email, firebaseToken)
                val subscription = SubscriptionEvent(settingsBag.listId.toString(), contactData, device = device)

                try {
                    val response = api.subscribe(subscription).execute()

                    if (response.isSuccessful) {
                        saveContactSubscriptionFromResponse(response)

                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(Exception("An API Exception occurred"))
                    }

                } catch (exception: HttpException) {
                    continuation.resumeWithException(exception)
                }
            }
        }
    }

    suspend fun resubscribeUser(email: String) {
        return subscribeContact(email)
    }

    @Suppress("NAME_SHADOWING")
    suspend fun unsubscribeCurrentContact() {
        return suspendCoroutine { continuation ->
            runBlocking {
                val deviceId = sdkRepository.getDeviceId()
                val subscriptionId = sdkRepository.getSubscriptionId()

                deviceId?.let { deviceId ->
                    subscriptionId?.let { subscriptionId ->
                        val unsubscribeEvent = UnsubscribeEvent(subscriptionId, deviceId)

                        try {
                            val response = api.unsubscribe(unsubscribeEvent).execute()

                            if (response.isSuccessful) {
                                sdkRepository.removeContactSubscription()
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(Exception("An API Exception occurred"))
                            }

                        } catch (exception: Exception) {
                            continuation.resumeWithException(exception)
                        }
                    } ?: continuation.resumeWithException(
                        EverlyticNotSubscribedException("No subscription to unsubscribe.")
                    )
                } ?: continuation.resumeWithException(Exception("No device ID set."))
            }
        }
    }

    internal fun saveContactSubscriptionFromResponse(response: Response<ApiSubscription>) {
        response.body().let { responseBody ->
            try {
                Log.d("PushSdk", "API Response: $responseBody")
                sdkRepository.setContactSubscription(responseBody!!)
            } catch (otherException: Exception) {
                throw otherException
            }
        } ?: throw Exception("Empty response body string")
    }

    internal fun isContactSubscribed(): Boolean {
        return sdkRepository.getSubscriptionId() != null && sdkRepository.getContactId() != null
    }
}