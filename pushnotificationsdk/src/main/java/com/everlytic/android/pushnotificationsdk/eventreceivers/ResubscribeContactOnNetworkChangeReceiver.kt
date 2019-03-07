package com.everlytic.android.pushnotificationsdk.eventreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.everlytic.android.pushnotificationsdk.logd

class ResubscribeContactOnNetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        logd("::onReceive() activeNetwork.isConnected=${connManager.activeNetworkInfo?.isConnected}")

        if (connManager.activeNetworkInfo?.isConnected == true) {
            EverlyticPush.instance?.resubscribeIfRequired()
        }
    }
}