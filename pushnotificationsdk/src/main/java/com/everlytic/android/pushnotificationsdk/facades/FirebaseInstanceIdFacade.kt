package com.everlytic.android.pushnotificationsdk.facades

import com.google.firebase.iid.FirebaseInstanceId
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class TokenResult(val success: Boolean, val value: String?, val throwable: Throwable? = null)

internal class FirebaseInstanceIdFacade(private val firebaseInstanceId: FirebaseInstanceId) {

    @Throws(Exception::class)
    fun getInstanceId(onComplete: (TokenResult) -> Unit) {
        firebaseInstanceId.instanceId
            .addOnSuccessListener {
                onComplete(TokenResult(true, it.token))
            }
            .addOnFailureListener {
                onComplete(TokenResult(false, null, it))
            }
    }

    companion object {
        fun getDefaultInstance(): FirebaseInstanceIdFacade {
            return FirebaseInstanceIdFacade(FirebaseInstanceId.getInstance())
        }
    }

}