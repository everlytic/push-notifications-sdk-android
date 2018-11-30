package com.everlytic.android.pushnotificationsdk.network

import okhttp3.*

internal class EverlyticApiAuthenticator(val username: String, val key: String) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val credentials = Credentials.basic(username, key)

        return response.request()
            .newBuilder()
            .header("Authorization", credentials)
            .build()
    }
}