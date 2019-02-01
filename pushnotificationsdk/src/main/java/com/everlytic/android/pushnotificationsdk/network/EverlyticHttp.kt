package com.everlytic.android.pushnotificationsdk.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

        return builder
            .client(okhttp)
            .baseUrl("https://$installUrl/api/3.0/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}