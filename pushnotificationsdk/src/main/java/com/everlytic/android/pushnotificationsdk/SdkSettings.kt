package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import java.nio.charset.Charset

internal object SdkSettings {

    const val KEY_VALUE_SEPARATOR = "="
    const val VALUES_SEPARATOR = ";"
    const val KEY_API_USER = "u"
    const val KEY_API_KEY = "k"
    const val KEY_INSTALL_URL = "i"
    const val KEY_LIST_ID = "l"

    const val META_SDK_CONFIGURATION_STRING = "com.everlytic.api.SDK_CONFIGURATION"

    data class SdkSettingsBag(
        val apiInstall: String?,
        val apiUsername: String?,
        val apiKey: String?,
        val listId: Int
    )

    private fun getConfigurationString(context: Context): String {
        return context
            .packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString(META_SDK_CONFIGURATION_STRING)
    }

    fun getSettings(context: Context): SdkSettingsBag {
        val decoded = Base64.decode(getConfigurationString(context), Base64.DEFAULT).toString(Charset.defaultCharset())

        val map: Map<String, String> = decoded.split(VALUES_SEPARATOR).map { entry ->
            entry.split(KEY_VALUE_SEPARATOR, limit = 2).let {
                it[0] to it[1]
            }
        }.toMap()

        return SdkSettingsBag(
            map[KEY_INSTALL_URL],
            map[KEY_API_USER],
            map[KEY_API_KEY],
            map[KEY_LIST_ID]!!.toInt()
        )
    }

}