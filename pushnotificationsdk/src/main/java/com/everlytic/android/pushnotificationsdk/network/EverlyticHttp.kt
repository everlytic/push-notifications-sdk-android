package com.everlytic.android.pushnotificationsdk.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.*

internal class EverlyticHttp {

    val builder: Retrofit.Builder = Retrofit.Builder()

    fun buildEverlyticApi(apiInstallUrl: String, apiUsername: String, apiKey: String) : EverlyticApi {
        return getRetrofitClient(apiInstallUrl, apiUsername, apiKey)
            .create(EverlyticApi::class.java)
    }

    private fun getRetrofitClient(installUrl: String, username: String, key: String): Retrofit {

        val okhttp = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .authenticator(EverlyticApiAuthenticator(username, key))
            .addInterceptor(EverlyticApiAuthenticationHeaderInterceptor(username, key))
            .addInterceptor(EverlyticApiErrorRequestFailerInterceptor())
            .addInterceptor(EverlyticApiExtraHeadersInterceptor())
            .build()

        return builder
            .client(okhttp)
            .baseUrl("https://$installUrl/api/3.0/")
            .build()
    }
}