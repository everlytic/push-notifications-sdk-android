package com.everlytic.android.sandboxapp

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.TextView
import com.everlytic.android.pushnotificationsdk.EverlyticPush
import com.everlytic.android.pushnotificationsdk.OnNotificationHistoryResultListener
import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification
import kotlinx.android.synthetic.main.activity_notification_history_viewer.*
import kotlinx.android.synthetic.main.content_notification_history_viewer.*
import kotlin.system.measureTimeMillis

class NotificationHistoryViewer : AppCompatActivity() {

    val adapter = HistoryViewRecyclerAdapter(::displayNotificationDetail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_history_viewer)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        prepareRefreshView()
        prepareRecyclerView()

        refreshNotifications()
    }

    private fun prepareRefreshView() {
        recycler_history_refresh_layout.setOnRefreshListener {
            refreshNotifications()
        }
    }

    private fun refreshNotifications() {
        recycler_history_refresh_layout.isRefreshing = true
        val sTime = System.currentTimeMillis()
        EverlyticPush.getNotificationHistory(OnNotificationHistoryResultListener { notifications ->
            val eTime = System.currentTimeMillis()
            Log.d("NotificationHistoryView","$notifications")
            recycler_history_refresh_layout.isRefreshing = false
            adapter.submitList(notifications.reversed())

            Log.d("TIMING", "Loaded history in ${eTime - sTime}ms")
        })
    }

    private fun prepareRecyclerView() {
        recycler_history_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_history_view.adapter = adapter
        recycler_history_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun displayNotificationDetail(notification: EverlyticNotification) {
        alert {
            val tv = TextView(this@NotificationHistoryViewer)

            val attrs = notification.custom_attributes.map { "${it.key}=${it.value}" }.joinToString("\n\t")

            tv.typeface = Typeface.MONOSPACE
            tv.text = """
                Title:        ${notification.title}
                Message:      ${notification.body}
                Received at:  ${notification.received_at}
                Read at:      ${notification.read_at}
                Dismissed at: ${notification.dismissed_at}
                C Attributes: ${attrs}
            """.trimIndent()
            setTitle("Notification ID ${notification.messageId}")
            setView(tv)
            setPositiveButton(android.R.string.ok, null)
        }
    }

}
