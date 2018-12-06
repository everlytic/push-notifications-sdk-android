package com.everlytic.android.pushnotificationsdk

import android.content.Context
import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import com.everlytic.android.pushnotificationsdk.models.ContactData
import com.everlytic.android.pushnotificationsdk.models.DeviceData
import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
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
            sdkRepository.setDeviceId()
        }
    }

    suspend fun subscribeUser(email: String) {
        return suspendCoroutine { continuation ->
            runBlocking {
                val deviceType = if (context.resources.getBoolean(R.bool.isTablet)) "tablet" else "handset"
                val device = DeviceData(sdkRepository.getDeviceId()!!, type = deviceType)
                val firebaseToken = firebaseInstanceId.getInstanceId()

                val contactData = ContactData(email, firebaseToken)
                val subscription = SubscriptionEvent(pushProjectId, contactData, device = device)

                try {
                    val response = api.subscribe(subscription).execute()

                    saveContactSubscriptionFromResponse(response)

                    continuation.resume(Unit)
                } catch (exception: HttpException) {
                    continuation.resumeWithException(exception)
                }
            }
        }
    }

    suspend fun resubscribeUser(email: String) {
        return subscribeUser(email)
    }

    internal fun saveContactSubscriptionFromResponse(response: Response<ResponseBody>) {
        response.body()?.string()?.let { responseBody ->
            try {
                val subscription = Moshi.Builder()
                    .build()
                    .adapter(ApiSubscription::class.java)
                    .fromJson(responseBody)!!

                sdkRepository.setContactSubscription(subscription)
            } catch (jsonException: JsonDataException) {
                throw jsonException
            } catch (otherException: Exception) {
                throw otherException
            }
        }
    }
}