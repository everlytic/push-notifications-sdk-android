package com.everlytic.android.pushnotificationsdk

import android.content.Context
import com.everlytic.android.pushnotificationsdk.handlers.NotificationDeliveredHandler
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.google.firebase.messaging.RemoteMessage
import io.mockk.*
import org.junit.Test

class EvNotificationReceiverServiceTest {

    @Test
    fun testMessageReceived_CreatesNotification_CallsEvNotificationManager() {
        val mockSdkRepository = Mock.getSdkRepositoryMock()
        val mockNotificationRepository = mockk<NotificationLogRepository>(relaxed = true)
        val mockNotificationHandler = mockk<EvNotificationHandler>(relaxed = true)
        val mockDeliveredHandler = mockk<NotificationDeliveredHandler>(relaxed = true)

        val receiverService = spyk<EvNotificationReceiverService>(recordPrivateCalls = true)
        val ctx = mockk<Context>(relaxed = true) {
            every { applicationContext } returns this
        }
        every { receiverService invokeNoArgs "getContext" } returns ctx
        every { receiverService getProperty "sdkRepository" } returns mockSdkRepository
        every { receiverService getProperty "notificationRepository" } returns mockNotificationRepository
        every { receiverService getProperty "notificationHandler" } returns mockNotificationHandler
        every { receiverService getProperty "notificationDeliveredHandler" } returns mockDeliveredHandler

        val mockMessage = mockk<RemoteMessage>(relaxed = true) {
            every { data } returns mapOf<String, String>(
                "title" to "This is a notification title",
                "body" to "This is some body content",
                "message_id" to "15",
                "@default" to "launch=",
                "\$custom" to "parameter"
            )
        }

        receiverService.onMessageReceived(mockMessage)

        verify { mockNotificationRepository.storeNotification(ofType(), any(), any()) }
        verify { mockNotificationHandler.displayNotification(any()) }
        verify { mockDeliveredHandler.processDeliveryEventForNotification(ofType()) }
    }

}