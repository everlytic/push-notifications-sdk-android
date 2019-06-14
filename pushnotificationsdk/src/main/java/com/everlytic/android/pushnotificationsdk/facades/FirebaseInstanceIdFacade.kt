package com.everlytic.android.pushnotificationsdk.facades

import android.content.Context
import com.everlytic.android.pushnotificationsdk.logd
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import java.lang.IllegalStateException
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @suppress
 * */
data class TokenResult(val success: Boolean, val value: String?, val throwable: Throwable? = null)

/**
 * @suppress
 * */
internal class FirebaseInstanceIdFacade(private val firebaseInstanceId: FirebaseInstanceId) {

    @Throws(Exception::class)
    fun getInstanceId(onComplete: (TokenResult) -> Unit) {
        firebaseInstanceId.token?.let {
            onComplete(TokenResult(true, it))
        } ?: onComplete(TokenResult(false, null, Exception("FCM Token is null")))
    }

    companion object {
        fun getDefaultInstance(context: Context): FirebaseInstanceIdFacade {

            try {
                logd("Checking if Firebase is initialized...")
                FirebaseApp.getInstance()
            } catch (exception: IllegalStateException) {
                logd("Firebase not initialized, initializing now...")
                FirebaseApp.initializeApp(context)
            }

            return FirebaseInstanceIdFacade(FirebaseInstanceId.getInstance())
        }
    }

}