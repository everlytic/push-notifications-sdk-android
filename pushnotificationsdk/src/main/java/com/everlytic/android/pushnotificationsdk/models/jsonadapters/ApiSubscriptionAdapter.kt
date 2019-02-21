package com.everlytic.android.pushnotificationsdk.models.jsonadapters

import com.everlytic.android.pushnotificationsdk.getJSONObjectOrNull
import com.everlytic.android.pushnotificationsdk.models.ApiSubscription
import org.json.JSONObject

internal object ApiSubscriptionAdapter : JSONAdapterInterface<ApiSubscription> {
    override fun fromJson(json: JSONObject): ApiSubscription {
        return with(json.getJSONObjectOrNull("subscription") ?: json) {
            ApiSubscription(
                getString("pns_id"),
                getString("pns_list_id"),
                getString("pns_customer_id"),
                getString("pns_contact_id"),
                getString("pns_device_id")
            )
        }
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