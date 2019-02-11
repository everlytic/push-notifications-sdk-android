package com.everlytic.android.pushnotificationsdk.network

import android.util.Log
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.ApiResponseAdapter
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

internal class EverlyticApiErrorRequestFailerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())

        if (response.isSuccessful) {
            val bodyString = response.peekBody(10240).string()
            Log.d("ApiRequestFailer", bodyString)
            ApiResponseAdapter.fromJson(JSONObject(bodyString)).let { status ->
                if (status.result == "error") {
                    response = response.newBuilder()
                        .code(400)
                        .build()
                }
            }
        }

        return response
    }
}