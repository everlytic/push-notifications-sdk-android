package com.everlytic.android.sandboxapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import kotlinx.android.synthetic.main.activity_sandbox.*

class Sandbox : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sandbox)

        updateSubscriptionDisplay()

        prepareSubscribeButton()
        prepareUnsubscribeButton()

    }

    private fun alert(block: AlertDialog.Builder.() -> Unit) {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setPositiveButton(android.R.string.ok, null)
                block()
            }.show()
        }
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

                    EverlyticPush.subscribe(edit.text.toString()) {
                        if (it.isSuccessful) {
                            alert {
                                setMessage("Subscribe success!")
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@Sandbox, "Subscribe failed!", Toast.LENGTH_LONG).show()

                                alert {
                                    setTitle("Subscribe error")
                                    setMessage("[MESSAGE]: ${it.exception?.message}")
                                    setPositiveButton(android.R.string.ok, null)
                                }
                            }
                        }

                        updateSubscriptionDisplay()
                    }
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
                    EverlyticPush.unsubscribe {
                        if (it.isSuccessful) {
                            alert { setMessage("Contact unsubscribed successfully") }
                        } else {
                            alert { setMessage("Failed to unsubscribe contact") }
                        }

                        updateSubscriptionDisplay()
                    }
                }

                setNegativeButton(android.R.string.cancel, null)
            }
        }
    }
}
