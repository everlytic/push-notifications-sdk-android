package com.everlytic.android.pushnotificationsdk.eventreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.everlytic.android.pushnotificationsdk.isDeviceOnline
import com.everlytic.android.pushnotificationsdk.logd

class ResubscribeContactOnNetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        logd("::onReceive() activeNetwork.isConnected=${context.isDeviceOnline}")

        if (context.isDeviceOnline) {
            EverlyticPush.instance?.resubscribeIfRequired()
        }
    }
}