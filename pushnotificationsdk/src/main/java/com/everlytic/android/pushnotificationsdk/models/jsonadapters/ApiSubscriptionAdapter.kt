package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import org.json.JSONObject

internal object ApiSubscriptionAdapter : JSONAdapterInterface<ApiSubscription> {
    override fun fromJson(json: JSONObject): ApiSubscription {
        val subscription = json.getJSONObject("subscription")
        return ApiSubscription(
            subscription.getString("pns_id"),
            subscription.getString("pns_list_id"),
//            subscription.getString("pns_project_id"), // TODO set to pns_list_id
            subscription.getString("pns_customer_id"),
            subscription.getString("pns_contact_id"),
            subscription.getString("pns_device_id")
        )
    }

    override fun toJson(obj: ApiSubscription): JSONObject {
        return JSONObject()
            .put("pns_id", obj.pns_id)
            .put("pns_list_id", obj.pns_list_id)
            .put("pns_customer_id", obj.pns_customer_id)
            .put("pns_contact_id", obj.pns_contact_id)
            .put("pns_device_id", obj.pns_device_id)
    }
}