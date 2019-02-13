package com.everlytic.android.pushnotificationsdk.network

import android.util.Base64
import java.net.HttpURLConnection

internal class EverlyticApiAuthenticator(private val username: String, private val key: String) {
    fun authenticate(connection: HttpURLConnection) {
        val auth = Base64.encodeToString("$username:$key".toByteArray(), Base64.DEFAULT)
        connection.addRequestProperty("Authorization", "Basic $auth")
    }
}