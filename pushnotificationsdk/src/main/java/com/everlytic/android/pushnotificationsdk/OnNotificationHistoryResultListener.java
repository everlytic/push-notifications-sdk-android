package com.everlytic.android.pushnotificationsdk;

import com.everlytic.android.pushnotificationsdk.models.EverlyticNotification;

import java.util.List;

public interface OnNotificationHistoryResultListener {

    void onResult(List<EverlyticNotification> notifications);

}
