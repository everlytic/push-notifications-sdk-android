package com.everlytic.android.pushnotificationsdk.database

object EvDbContract {

    object NotificationLogTable {
        const val TBL_NAME = "notification_log"

        const val COL_ID = "_id"
        const val COL_MESSAGE_ID = "message_id"
        const val COL_ANDROID_NOTIFICAITON_ID = "android_notification_id"
        const val COL_SUBSCRIPTION_ID = "subscription_id"
        const val COL_CONTACT_ID = "contact_id"
        const val COL_TITLE = "title"
        const val COL_BODY = "body"
        const val COL_METADATA = "metadata"
        const val COL_ACTIONS = "actions"
        const val COL_COLOR = "color"
        const val COL_GROUP_ID = "group_id"
        const val COL_RAW_NOTIFICATION = "raw_notification"
        const val COL_RECEIVED_AT = "received_at"
        const val COL_READ_AT = "read_at"
        const val COL_DISMISSED_AT = "dismissed_at"

        val CREATE_STATEMENT = """
            CREATE TABLE $TBL_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MESSAGE_ID INTEGER,
                $COL_ANDROID_NOTIFICAITON_ID INTEGER,
                $COL_SUBSCRIPTION_ID INTEGER,
                $COL_CONTACT_ID INTEGER,
                $COL_TITLE TEXT,
                $COL_BODY TEXT,
                $COL_METADATA TEXT DEFAULT '{}',
                $COL_ACTIONS TEXT,
                $COL_COLOR INTEGER DEFAULT -1,
                $COL_GROUP_ID INTEGER DEFAULT 0,
                $COL_RAW_NOTIFICATION TEXT,
                $COL_RECEIVED_AT TEXT NOT NULL,
                $COL_READ_AT TEXT DEFAULT NULL,
                $COL_DISMISSED_AT TEXT DEFAULT NULL
            );
        """.trimIndent()
    }

    object NotificationEventsLogTable {
        const val TBL_NAME = "notification_events_log"
        const val COL_ID = "_id"
        const val COL_ANDROID_NOTIFICATION_ID = "android_notification_id"
        const val COL_EVENT_TYPE = "event_type"
        const val COL_SUBSCRIPTION_ID = "subscription_id"
        const val COL_MESSAGE_ID = "message_id"
        const val COL_DEVICE_ID = "device_id"
        const val COL_METADATA = "metadata"
        @Deprecated("Removed from db version 2")
        const val COL_IS_UPLOADED = "is_uploaded"
        const val COL_DATETIME = "datetime"

        val CREATE_STATEMENT = """
            CREATE TABLE $TBL_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ANDROID_NOTIFICATION_ID INTEGER NOT NULL,
                $COL_EVENT_TYPE TEXT NOT NULL,
                $COL_SUBSCRIPTION_ID INTEGER NOT NULL,
                $COL_MESSAGE_ID INTEGER NOT NULL,
                $COL_DEVICE_ID TEXT NOT NULL,
                $COL_METADATA TEXT NOT NULL DEFAULT '{}',
                $COL_DATETIME TEXT NOT NULL
            );
        """.trimIndent()

        const val INDEX_CREATE_EVENT_TYPE =
            "CREATE INDEX ev_notification_event_log_event_type ON $TBL_NAME($COL_EVENT_TYPE)"
        const val INDEX_CREATE_IS_UPLOADED_EVENT_TYPE =
            "CREATE INDEX ev_notification_event_log_event_type_uploaded ON $TBL_NAME($COL_EVENT_TYPE, $COL_IS_UPLOADED)"

        val UPGRADE_V2 = listOf("ALTER $TBL_NAME DROP COLUMN $COL_IS_UPLOADED")
    }

}