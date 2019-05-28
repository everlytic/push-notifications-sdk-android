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
             .init(this, "dT1hZG1pbmlzdHJhdG9yO2s9MlVvZXdYaXJSMEJPZVFHOGh1M1pFRHpLNnVXd0l5NHJfMDtpPWh0dHA6Ly9xYS5ldmVybHl0aWMubmV0O2w9NA==")
    }

}