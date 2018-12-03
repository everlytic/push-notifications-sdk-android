package com.everlytic.android.pushnotificationsdk.facades

import android.os.Build
import com.everlytic.android.pushnotificationsdk.BuildConfig

object BuildFacade {

    fun getPlatformVersion(): String = Build.VERSION.RELEASE
    fun getDeviceManufacturer(): String = Build.MANUFACTURER
    fun getDeviceModel(): String = Build.MODEL

    fun getBuildConfigVersionName(): String = BuildConfig.VERSION_NAME
    fun getBuildConfigVersionCode(): Int = BuildConfig.VERSION_CODE
}