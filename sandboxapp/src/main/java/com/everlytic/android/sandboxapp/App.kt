package com.everlytic.android.sandboxapp

import android.app.Application
import com.everlytic.android.pushnotificationsdk.EverlyticPush

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        EverlyticPush.init(this)
    }

}