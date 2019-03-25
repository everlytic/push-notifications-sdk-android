package com.everlytic.android.sandboxapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.everlytic.android.pushnotificationsdk.OnResultReceiver
import com.everlytic.android.pushnotificationsdk.logw
import kotlinx.android.synthetic.main.activity_sandbox.*

class Sandbox : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sandbox)

        updateSubscriptionDisplay()

        prepareSubscribeButton()
        prepareUnsubscribeButton()
        prepareViewHistory()

    }

    private fun updateSubscriptionDisplay() {
        EverlyticPush.isContactSubscribed().let {
            btn_unsubscribe.isEnabled = it
        }
    }

    private fun prepareSubscribeButton() {
        btn_subscribe.setOnClickListener {

            val edit = EditText(this).apply {
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }

            alert {
                setTitle("Subscribe Contact Email")
                setView(edit)
                setPositiveButton(android.R.string.ok) { _, _ ->

                    EverlyticPush.subscribe(edit.text.toString(), OnResultReceiver {
                        if (it.isSuccessful) {
                            alert {
                                setMessage("Subscribe success!")
                            }
                        } else {
                            alert {
                                setTitle("Subscribe error")
                                setMessage("[MESSAGE]: ${it.exception?.message}")
                                setPositiveButton(android.R.string.ok, null)
                            }
                        }

                        updateSubscriptionDisplay()
                    })
                }
            }
        }
    }

    private fun prepareUnsubscribeButton() {
        btn_unsubscribe.setOnClickListener {
            alert {
                setTitle("Unsubscribe contact?")
                setMessage("Unsubscribe the current contact from receiving push notifications?")
                setPositiveButton(android.R.string.ok) { _, _ ->
                    EverlyticPush.unsubscribe( OnResultReceiver{
                        if (it.isSuccessful) {
                            alert { setMessage("Contact unsubscribed successfully") }
                        } else {
                            logw(throwable = it.exception)
                            alert { setMessage("Failed to unsubscribe contact") }
                        }

                        updateSubscriptionDisplay()
                    })
                }

                setNegativeButton(android.R.string.cancel, null)
            }
        }
    }

    private fun prepareViewHistory() {
        btn_view_history.setOnClickListener {
            startActivity(Intent(this, NotificationHistoryViewer::class.java))
        }
    }
}
