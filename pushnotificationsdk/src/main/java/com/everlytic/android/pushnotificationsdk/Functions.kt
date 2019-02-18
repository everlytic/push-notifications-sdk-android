package com.everlytic.android.pushnotificationsdk

import android.os.Handler
import android.os.Looper
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
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

fun Any.logd(message: String? = null, throwable: Throwable? = null) {
    EvLogger.d(this::class.java.simpleName, message, throwable)
}

fun Any.logw(message: String? = null, throwable: Throwable? = null) {
    EvLogger.w(this::class.java.simpleName, message, throwable)
}