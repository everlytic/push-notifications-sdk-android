package com.everlytic.android.pushnotificationsdk

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.everlytic.android.pushnotificationsdk.handlers.NotificationDeliveredHandler
import com.everlytic.android.pushnotificationsdk.models.EvNotification
import com.everlytic.android.pushnotificationsdk.models.GoToUrlNotificationAction
import com.everlytic.android.pushnotificationsdk.models.LaunchAppNotificationAction
import com.everlytic.android.pushnotificationsdk.models.NotificationAction
import com.everlytic.android.pushnotificationsdk.repositories.NotificationLogRepository
import com.google.firebase.messaging.RemoteMessage
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class EvNotificationReceiverServiceTest {

    @Before
    fun setup() {
        Mock.EvLogger()
    }

    @Test
    fun testMessageReceived_CreatesNotification_CallsEvNotificationManager() {

        val notificationSlot = slot<EvNotification>()

        val mockSdkRepository = Mock.getSdkRepositoryMock()
        val mockNotificationRepository = mockk<NotificationLogRepository>(relaxed = true) {
            every { storeNotification(capture(notificationSlot), any(), any()) } just Runs
        }
        val mockNotificationHandler = mockk<EvNotificationHandler>(relaxed = true)
        val mockDeliveredHandler = mockk<NotificationDeliveredHandler>(relaxed = true)

        val receiverService = spyk<EvNotificationReceiverService>(recordPrivateCalls = true)
        val ctx = getApplication()

        every { receiverService invokeNoArgs "getContext" } returns ctx
        every { receiverService getProperty "sdkRepository" } returns mockSdkRepository
        every { receiverService getProperty "notificationRepository" } returns mockNotificationRepository
        every { receiverService getProperty "notificationHandler" } returns mockNotificationHandler
        every { receiverService getProperty "notificationDeliveredHandler" } returns mockDeliveredHandler

        val attr = mapOf(
            "title" to "This is a notification title",
            "body" to "This is some body content",
            "message_id" to "15",
            "@default" to "launch=",
            "\$custom" to "parameter"
        )
        val mockMessage = mockk<RemoteMessage>(relaxed = true) {
            every { data } returns attr
        }

        receiverService.onMessageReceived(mockMessage)

        verify { mockNotificationRepository.storeNotification(ofType(), any(), any()) }
        verify { mockNotificationHandler.displayNotification(any()) }
        verify { mockDeliveredHandler.processDeliveryEventForNotification(ofType()) }

        notificationSlot.captured.let { notification ->
            assertEquals(attr["title"], notification.title)
            assertEquals(attr["body"], notification.body)
            assertEquals(attr["message_id"]?.toLong(), notification.messageId)
            assertEquals(1, notification.actions.size)
            assertEquals(1, notification.customParameters.size)
            assertTrue { notification.customParameters.containsKey("custom") }
            assertEquals("parameter", notification.customParameters["custom"])
            assertTrue { notification.actions.firstOrNull { it.action == NotificationAction.Action.DEFAULT } != null }
            assertTrue { notification.actions.first() is LaunchAppNotificationAction }
        }
    }

    @Test
    fun testUrlLaunchAction_CreatesNotification_WithUrlClickAction() {

        val notificationSlot = slot<EvNotification>()

        val mockSdkRepository = Mock.getSdkRepositoryMock()
        val mockNotificationRepository = mockk<NotificationLogRepository>(relaxed = true) {
            every { storeNotification(capture(notificationSlot), any(), any()) } just Runs
        }
        val mockNotificationHandler = mockk<EvNotificationHandler>(relaxed = true)
        val mockDeliveredHandler = mockk<NotificationDeliveredHandler>(relaxed = true)

        val receiverService = spyk<EvNotificationReceiverService>(recordPrivateCalls = true)

        val ctx = getApplication()

        every { receiverService invokeNoArgs "getContext" } returns ctx
        every { receiverService getProperty "sdkRepository" } returns mockSdkRepository
        every { receiverService getProperty "notificationRepository" } returns mockNotificationRepository
        every { receiverService getProperty "notificationHandler" } returns mockNotificationHandler
        every { receiverService getProperty "notificationDeliveredHandler" } returns mockDeliveredHandler

        val url = "http://test.com"
        val attr = mapOf(
            "title" to "This is a notification title",
            "body" to "This is some body content",
            "message_id" to "15",
            "@default" to "url=[]($url)",
            "\$custom" to "parameter"
        )

        val mockMessage = mockk<RemoteMessage>(relaxed = true) {
            every { data } returns attr
        }

        receiverService.onMessageReceived(mockMessage)

        verify { mockNotificationRepository.storeNotification(ofType(), any(), any()) }
        verify { mockNotificationHandler.displayNotification(any()) }
        verify { mockDeliveredHandler.processDeliveryEventForNotification(ofType()) }

        notificationSlot.captured.let { notification ->
            assertTrue { notification.actions.firstOrNull { it.action == NotificationAction.Action.DEFAULT } != null }
            assertTrue { notification.actions.first() is GoToUrlNotificationAction }
            (notification.actions.first() as GoToUrlNotificationAction).let {
                assertEquals(url, it.uri.toString())
            }
        }
    }

    private fun getApplication(): Application {
        return spyk(ApplicationProvider.getApplicationContext<Application>()) {
            every { resources.getColor(any(), any()) } returns 5
            every { resources.getColor(any()) } returns 5
            every { resources.getIdentifier(any(), any(), any()) } returns 0
        }
    }

}