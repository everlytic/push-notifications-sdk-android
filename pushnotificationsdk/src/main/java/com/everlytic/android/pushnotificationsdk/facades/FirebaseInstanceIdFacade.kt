package com.everlytic.android.pushnotificationsdk.facades

import com.google.firebase.iid.FirebaseInstanceId
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class FirebaseInstanceIdFacade(private val firebaseInstanceId: FirebaseInstanceId) {

    @Throws(Exception::class)
    suspend fun getInstanceId(): String {

        return suspendCoroutine { continuation ->
            firebaseInstanceId.instanceId
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(it.token))
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    }

    companion object {
        fun getDefaultInstance(): FirebaseInstanceIdFacade {
            return FirebaseInstanceIdFacade(FirebaseInstanceId.getInstance())
        }
    }

}