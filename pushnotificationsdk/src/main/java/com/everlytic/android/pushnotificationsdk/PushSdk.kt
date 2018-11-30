package com.everlytic.android.pushnotificationsdk

import com.everlytic.android.pushnotificationsdk.models.ContactData
import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult

internal class PushSdk constructor(
    private val apiInstallUrl: String,
    private val apiUsername: String,
    private val apiKey: String,
    private val pushProjectId: String,
    httpBuilder: EverlyticHttp = EverlyticHttp(),
    private val firebaseInstanceId: FirebaseInstanceId = FirebaseInstanceId.getInstance()
) {

    private val api = httpBuilder.buildEverlyticApi(apiInstallUrl, apiUsername, apiKey)

    fun subscribeUser(email: String) {

        firebaseInstanceId.instanceId
            .addOnSuccessListener { result ->
                val contactData = createContactData(email, result)
                val subscription = createSubscriptionEvent(contactData)

                api.subscribe(subscription)
            }
            .addOnFailureListener {

            }

    }

    private fun createSubscriptionEvent(contactData: ContactData) =
        SubscriptionEvent(pushProjectId, contactData)

    private fun createContactData(
        email: String,
        result: InstanceIdResult
    ) = ContactData(email, result.token)
}