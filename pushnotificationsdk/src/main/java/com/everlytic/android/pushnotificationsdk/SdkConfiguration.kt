package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import java.lang.IllegalArgumentException
import java.nio.charset.Charset

/**
 * @suppress
 * */
internal object SdkConfiguration {

    private const val KEY_VALUE_SEPARATOR = "="
    private const val VALUES_SEPARATOR = ";"
    private const val KEY_INSTALL_URL = "i"
    private const val KEY_PROJECT_UUID = "p"

    const val META_SDK_CONFIGURATION_STRING = "com.everlytic.api.SDK_CONFIGURATION"

    data class SdkConfigBag(
        val apiInstall: String,
        val pushProjectUuid: String
    )

    private fun getConfigurationString(context: Context): String {
        return context
            .packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString(META_SDK_CONFIGURATION_STRING)
    }

    fun getConfigurationBag(context: Context): SdkConfigBag {
        return getConfigurationBag(getConfigurationString(context))
    }

    @Throws(IllegalArgumentException::class)
    fun getConfigurationBag(configString: String): SdkConfigBag {
        logd("Decoding config string $configString")
        val decoded = Base64.decode(configString, Base64.DEFAULT).toString(Charset.defaultCharset())

        logd("Mapping decoded string $decoded")
        val map: Map<String, String> = decoded.split(VALUES_SEPARATOR).map { entry ->
            entry.split(KEY_VALUE_SEPARATOR, limit = 2).let {
                it[0] to it[1]
            }
        }.toMap()

        logd("Mapped string $map")

        return SdkConfigBag(
            map[KEY_INSTALL_URL] ?: error("Install Url cannot be null"),
            map[KEY_PROJECT_UUID] ?: error("Project Uuid cannot be null")
        ).also { logd("Created SdkConfigBag=$it") }
    }

}