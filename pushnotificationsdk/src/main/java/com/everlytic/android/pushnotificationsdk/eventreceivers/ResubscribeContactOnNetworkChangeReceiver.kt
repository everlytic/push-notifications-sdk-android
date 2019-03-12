package com.everlytic.android.pushnotificationsdk.eventreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.everlytic.android.pushnotificationsdk.isDeviceOnline
import com.everlytic.android.pushnotificationsdk.logd
import com.everlytic.android.pushnotificationsdk.workers.UploadMessageEventsService

class ResubscribeContactOnNetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logd("::onReceive() activeNetwork.isConnected=${isDeviceOnline(context)}")

        if (isDeviceOnline(context)) {
            EverlyticPush.instance?.resubscribeIfRequired()
            UploadMessageEventsService.enqueue(context)
        }
    }
}