@file:JvmName("Functions")
package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: String): Map<String, String> {
    return decodeJsonMap(JSONObject(data))
}

@Suppress("UNCHECKED_CAST")
internal fun decodeJsonMap(data: JSONObject): Map<String, String> {
    return MapAdapter.fromJson(data)
}

@Suppress("UNCHECKED_CAST")
internal fun encodeJsonMap(map: Map<String, String>): JSONObject {
    return MapAdapter.toJson(map)
}

internal inline fun HttpURLConnection.use(block: HttpURLConnection.() -> Unit) {
    try {
        this.block()
    } finally {
        this.disconnect()
    }
}

fun JSONObject.getJSONObjectOrNull(name: String): JSONObject? {
    return try {
        this.getJSONObject(name)
    } catch (e: JSONException) {
        null
    }
}

@JvmOverloads
fun Any.logd(message: String? = null, throwable: Throwable? = null) {
    EvLogger.d(this::class.java.simpleName, message, throwable)
}

@JvmOverloads
fun Any.logw(message: String? = null, throwable: Throwable? = null) {
    EvLogger.w(this::class.java.simpleName, message, throwable)
}

fun Intent.isEverlyticEventIntent(): Boolean {
    return this.hasExtra(EvIntentExtras.EVERLYTIC_DATA) || this.hasExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID)
}

val Context.isDeviceOnline: Boolean
    get() {
        return (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            .activeNetworkInfo?.isConnected ?: false
    }