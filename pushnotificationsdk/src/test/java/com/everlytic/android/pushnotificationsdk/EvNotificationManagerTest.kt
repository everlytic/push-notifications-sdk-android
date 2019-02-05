package com.everlytic.android.pushnotificationsdk

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import io.mockk.*
import org.junit.Ignore
import org.junit.Test
import java.util.*

class EvNotificationHandlerTest {

    @Test
    @Ignore("Cannot mock out NotificationCompat.Builder, which is used directly by the EvNotificationHandler class")
    fun testDisplayNotification_WithNotification_PostsToNotificationManager() {

        val mockContext = mockk<Context> {
            val notificationManager = mockk<NotificationManager>()
            every { getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        }

        val handler = spyk(EvNotificationHandler(mockContext), recordPrivateCalls = true)

        val notification = EvNotification(
            0,
            100,
            "title",
            "body",
            false,
            10,
            1000,
            0,
            emptyList(),
            Date(0),
            Date()
        )

        every { handler invoke "createLauncherIntent" withArguments listOf(notification) } answers { mockk<Intent>() }
        every { handler invoke "createPendingIntent" withArguments listOf(ofType<Intent>()) } answers { mockk<PendingIntent>() }
        every { handler invokeNoArgs "getSmallIconReference" } returns 0

        handler.displayNotification(notification)

        val mockBuilder = mockk<NotificationCompat.Builder>()

        mockkConstructor(NotificationCompat.Builder::class)

        every { anyConstructed<NotificationCompat.Builder>().setWhen(any()) } returns mockBuilder

        verifySequence {
            anyConstructed<NotificationCompat.Builder>().setSmallIcon(any())
            anyConstructed<NotificationCompat.Builder>().setContentTitle(notification.title)
            anyConstructed<NotificationCompat.Builder>().setContentText(notification.body)
            anyConstructed<NotificationCompat.Builder>().priority = notification.priority
            anyConstructed<NotificationCompat.Builder>().setGroup(any())
            anyConstructed<NotificationCompat.Builder>().setContentIntent(any())
//            anyConstructed<NotificationCompat.Builder>().build()
        }
    }

}