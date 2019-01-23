package com.everlytic.android.pushnotificationsdk.network

import android.util.Log
import com.everlytic.android.pushnotificationsdk.exceptions.EverlyticApiException
import com.everlytic.android.pushnotificationsdk.models.Status
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.Response

internal class EverlyticApiErrorRequestFailerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.isSuccessful) {
            val bodyString = response.peekBody(10240).string();
            Log.d("ApiRequestFailer", bodyString)
            Moshi.Builder().build().adapter(Status::class.java).fromJson(bodyString)?.let { status ->
                if (status.status != null && status.status == "error") {
                    throw EverlyticApiException("An API Exception occurred")
                }
            }
        }

        return response
    }
}