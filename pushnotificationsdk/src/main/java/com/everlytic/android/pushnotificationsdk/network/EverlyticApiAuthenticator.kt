package com.everlytic.android.pushnotificationsdk.network

import java.net.HttpURLConnection

internal class EverlyticApiAuthenticator(private val pushProjectUuid: String) {
    fun authenticate(connection: HttpURLConnection) {
        connection.addRequestProperty("X-EV-Project-UUID", pushProjectUuid)
    }
}