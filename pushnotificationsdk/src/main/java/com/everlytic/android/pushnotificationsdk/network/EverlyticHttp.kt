package com.everlytic.android.pushnotificationsdk.network

import com.everlytic.android.pushnotificationsdk.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class EverlyticHttp {

    val builder: Retrofit.Builder = Retrofit.Builder()

    fun buildEverlyticApi(apiInstallUrl: String, apiUsername: String, apiKey: String) : EverlyticApi {
        return getRetrofitClient(apiInstallUrl, apiUsername, apiKey)
            .create(EverlyticApi::class.java)
    }

    private fun getRetrofitClient(installUrl: String, username: String, key: String): Retrofit {

        val okhttp = OkHttpClient.Builder()
            .authenticator(EverlyticApiAuthenticator(username, key))
            .addInterceptor(EverlyticApiAuthenticationHeaderInterceptor(username, key))
            .addInterceptor(EverlyticApiErrorRequestFailerInterceptor())
            .build()

        return builder
            .client(okhttp)
            .baseUrl("https://$installUrl")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}