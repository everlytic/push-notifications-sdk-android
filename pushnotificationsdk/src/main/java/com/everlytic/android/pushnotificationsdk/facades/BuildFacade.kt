package com.everlytic.android.pushnotificationsdk.facades

import android.os.Build

object BuildFacade {

    fun getPlatformVersion() : String = Build.VERSION.RELEASE
    fun getDeviceManufacturer() : String = Build.MANUFACTURER
    fun getDeviceModel() : String = Build.MODEL

}