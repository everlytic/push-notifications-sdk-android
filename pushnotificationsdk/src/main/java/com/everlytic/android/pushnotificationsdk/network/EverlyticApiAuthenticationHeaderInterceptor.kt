package com.everlytic.android.pushnotificationsdk.network

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

internal class EverlyticApiAuthenticationHeaderInterceptor(val username: String, val key: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val credentials = Credentials.basic(username, key)

        return chain.request()
            .newBuilder()
            .header("Authorization", credentials)
            .build()
            .let {
                chain.proceed(it)
            }
    }
}