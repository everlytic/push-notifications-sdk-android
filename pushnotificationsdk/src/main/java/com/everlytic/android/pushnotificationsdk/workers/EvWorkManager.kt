package com.everlytic.android.pushnotificationsdk.workers

import androidx.work.*

object EvWorkManager {

    inline fun <reified T : Worker> scheduleOneTimeWorker(tag: String? = null, requireNetwork: Boolean = true) {
        val constraints = Constraints.Builder().apply {
            if (requireNetwork) setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

        val workRequest = OneTimeWorkRequestBuilder<T>().apply {
            setConstraints(constraints)
            tag?.let { addTag(it) }
        }.build()

        WorkManager
            .getInstance()
            .enqueue(workRequest)
    }

}