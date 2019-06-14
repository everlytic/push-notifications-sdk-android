@file:JvmName("Functions")

package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.everlytic.android.pushnotificationsdk.models.jsonadapters.MapAdapter
import com.everlytic.android.pushnotificationsdk.repositories.SdkRepository
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.security.MessageDigest


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

internal fun JSONObject.getJSONObjectOrNull(name: String): JSONObject? {
    return try {
        this.getJSONObject(name)
    } catch (e: JSONException) {
        null
    }
}

/**
 * @suppress
 * */
@JvmOverloads
fun Any.logd(message: String? = null, throwable: Throwable? = null) {
    EvLogger.d(this::class.java.simpleName, message, throwable)
}

/**
 * @suppress
 * */
@JvmOverloads
fun Any.logi(message: String? = null, throwable: Throwable? = null) {
    EvLogger.i(this::class.java.simpleName, message, throwable)
}

/**
 * @suppress
 * */
@JvmOverloads
fun Any.logw(message: String? = null, throwable: Throwable? = null) {
    EvLogger.w(this::class.java.simpleName, message, throwable)
}

/**
 * @suppress
 * */
@JvmOverloads
fun Any.loge(message: String? = null, throwable: Throwable? = null) {
    EvLogger.e(this::class.java.simpleName, message, throwable)
}

/**
 * @suppress
 * */
fun Intent.isEverlyticEventIntent(): Boolean {
    return this.hasExtra(EvIntentExtras.EVERLYTIC_DATA) || this.hasExtra(EvIntentExtras.ANDROID_NOTIFICATION_ID)
}

fun isDeviceOnline(context: Context): Boolean {
    return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        .activeNetworkInfo?.isConnected ?: false
}

@Throws(Throwable::class)
internal inline fun Throwable.handle(handler: ((Throwable) -> Unit) = { logi(null, this) }) {
    if (EverlyticPush.throwExceptions)
        throw this
    else
        handler(this)
}

internal fun updateFcmToken(sdkRepository: SdkRepository, token: String?) {
    val email = sdkRepository.getContactEmail()
    if (sdkRepository.getHasSubscription() && !email.isNullOrBlank()) {
        token?.let { newToken ->
            sdkRepository.setNewFcmToken(newToken)
            EverlyticPush.instance?.resubscribeIfRequired()
        }
    }
}

internal fun String.getHash(): String {
    val md = MessageDigest.getInstance("SHA-1")
    val textBytes = this.toByteArray(Charset.forName("iso-8859-1"))
    md.update(textBytes, 0, textBytes.size)
    val buf = StringBuilder()
    for (b in md.digest()) {
        var halfbyte = (b.toInt() ushr 4) and 0x0F
        var two_halfs = 0
        do {
            buf.append(if (halfbyte in 0..9) ('0'.toInt() + halfbyte).toChar() else ('a'.toInt() + (halfbyte - 10)).toChar())
            halfbyte = b.toInt() and 0x0F
        } while (two_halfs++ < 1)
    }
    return buf.toString()
}