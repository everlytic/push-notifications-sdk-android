package com.everlytic.android.pushnotificationsdk

import android.content.Context
import android.content.pm.PackageManager

object SdkSettings {

    const val META_API_USERNAME_PATH = "com.everlytic.api.API_USERNAME"
    const val META_API_KEY_PATH = "com.everlytic.api.API_KEY"
    const val META_PUSH_PROJECT_ID = "com.everlytic.api.PUSH_NOTIFICATIONS_PROJECT_ID"
    const val META_API_INSTALL_URL = "com.everlytic.api.API_INSTALL_URL"

    data class SdkSettingsBag(
        val apiInstall: String?,
        val apiUsername: String?,
        val apiKey: String?,
        val projectId: Int
    )

    fun getSettings(context: Context) : SdkSettingsBag {
        val appInfo = context.applicationInfo

        return SdkSettingsBag(
            appInfo.metaData.getString(SdkSettings.META_API_INSTALL_URL),
            appInfo.metaData.getString(SdkSettings.META_API_USERNAME_PATH),
            appInfo.metaData.getString(SdkSettings.META_API_KEY_PATH),
            appInfo.metaData.getInt(SdkSettings.META_PUSH_PROJECT_ID, -1)
        )
    }

}