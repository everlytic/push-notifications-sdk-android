package com.everlytic.android.sandboxapp

import android.app.Application
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.facebook.stetho.Stetho

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        EverlyticPush
//            .setInTestMode(true)
            .init(this)
    }

}