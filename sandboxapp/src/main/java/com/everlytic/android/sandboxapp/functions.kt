package com.everlytic.android.sandboxapp

import android.app.Activity
import android.support.v7.app.AlertDialog

fun Activity.alert(block: AlertDialog.Builder.() -> Unit) {
    runOnUiThread {
        AlertDialog.Builder(this).apply {
            setPositiveButton(android.R.string.ok, null)
            block()
        }.show()
    }
}