package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.util.Log
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticNotSubscribedException
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.models.*
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class PushSdk constructor(
    private val context: Context,
    private val apiInstallUrl: String,
    private val apiUsername: String,
    private val apiKey: String,
    private val pushProjectId: String,
    httpBuilder: EverlyticHttp = EverlyticHttp(),
    private val firebaseInstanceId: FirebaseInstanceIdFacade = FirebaseInstanceIdFacade.getDefaultInstance(),
    private val sdkRepository: SdkRepository = SdkRepository(context)
) {

    private val api = httpBuilder.buildEverlyticApi(apiInstallUrl, apiUsername, apiKey)

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
                val subscription = SubscriptionEvent(pushProjectId, contactData, device = device)

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

    internal fun saveContactSubscriptionFromResponse(response: Response<ApiSubscriptionResponse>) {
        response.body()?.let { responseBody ->
            try {
//                val subscription = Moshi.Builder()
//                    .build()
//                    .adapter(ApiSubscription::class.java)
//                    .fromJson(responseBody)!!
                Log.d("PushSdk", "API Response: $responseBody")
                sdkRepository.setContactSubscription(responseBody.data.subscription)
            } catch (jsonException: JsonDataException) {
                throw jsonException
            } catch (otherException: Exception) {
                throw otherException
            }
        } ?: throw Exception("Empty response body string")
    }
}