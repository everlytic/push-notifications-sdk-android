package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.BuildConfig
import com.everlytic.android.pushnotificationsdk.facades.BuildFacade
import okhttp3.Interceptor
import okhttp3.Response

internal class EverlyticApiExtraHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.request()
            .newBuilder()
            .header("X-EV-SDK-Version-Name", BuildFacade.getBuildConfigVersionName())
            .header("X-EV-SDK-Version-Code", BuildFacade.getBuildConfigVersionCode().toString())
            .build()
            .let {
                chain.proceed(it)
            }
    }
}