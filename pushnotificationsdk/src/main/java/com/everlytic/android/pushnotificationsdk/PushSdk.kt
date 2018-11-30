package com.everlytic.android.pushnotificationsdk

import com.everlytic.android.pushnotificationsdk.facades.FirebaseInstanceIdFacade
import com.everlytic.android.pushnotificationsdk.models.ContactData
import com.everlytic.android.pushnotificationsdk.models.SubscriptionEvent
import com.everlytic.android.pushnotificationsdk.network.EverlyticHttp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class PushSdk constructor(
    private val apiInstallUrl: String,
    private val apiUsername: String,
    private val apiKey: String,
    private val pushProjectId: String,
    httpBuilder: EverlyticHttp = EverlyticHttp(),
    private val firebaseInstanceId: FirebaseInstanceIdFacade = FirebaseInstanceIdFacade.getDefaultInstance()
) {

    private val api = httpBuilder.buildEverlyticApi(apiInstallUrl, apiUsername, apiKey)

    fun subscribeUser(email: String) {

        GlobalScope.launch {
            val firebaseToken = firebaseInstanceId.getInstanceId()

            val contactData = ContactData(email, firebaseToken)
            val subscription = SubscriptionEvent(pushProjectId, contactData)

            api.subscribe(subscription)
        }
    }
}