package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import org.json.JSONObject

internal object ApiSubscriptionAdapter : JSONAdapterInterface<ApiSubscription> {
    override fun fromJson(json: JSONObject): ApiSubscription {
        return ApiSubscription(
            json.getString("pns_id"),
            json.getString("pns_list_id"),
            json.getString("pns_customer_id"),
            json.getString("pns_contact_id"),
            json.getString("pns_device_id")
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