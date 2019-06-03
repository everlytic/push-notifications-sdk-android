package com.everlytic.android.pushnotificationsdk.workers

import android.content.ComponentName
import android.content.Context

internal object EvWorkManager {

    inline fun <reified T : JobIntentService> scheduleOneTimeWorker(context: Context) {


//        val constraints = Constraints.Builder().apply {
//            if (requireNetwork) setRequiredNetworkType(NetworkType.CONNECTED)
//        }.build()
//
//        val workRequest = OneTimeWorkRequestBuilder<T>().apply {
//            setConstraints(constraints)
//            tag?.let { addTag(it) }
//        }.build()
//
//        WorkManager
//            .getInstance()
//            .enqueue(workRequest)
    }

}