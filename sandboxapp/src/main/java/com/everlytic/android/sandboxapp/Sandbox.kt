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

        btn_subscribe.setOnClickListener { _ ->

            val edit = EditText(this).apply {
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }

            AlertDialog.Builder(this)
                .setTitle("Subscribe Contact Email")
                .setView(edit)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    EverlyticPush.subscribe(edit.text.toString()) {
                        if (it.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this@Sandbox, "Subscribe success!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@Sandbox, "Subscribe failed!", Toast.LENGTH_LONG).show()

                                AlertDialog.Builder(this)
                                    .setTitle("Subscribe error")
                                    .setMessage("[MESSAGE]: ${it.exception?.message}")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                            }
                        }
                    }
                }
                .show()


        }

    }
}
