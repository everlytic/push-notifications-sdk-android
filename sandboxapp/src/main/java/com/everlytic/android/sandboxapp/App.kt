package com.everlytic.android.sandboxapp

import android.app.Application
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        FirebaseApp.initializeApp(this)
        EverlyticPush
//            .setInTestMode(true)
            .init(this)
    }

}