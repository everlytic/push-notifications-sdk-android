package com.everlytic.android.pushnotificationsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EvBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

    }
}