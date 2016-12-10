package com.rubyhuntersky.angleedit.app

import android.net.Uri
import org.json.JSONObject
import java.sql.Timestamp

/**
 * @author Jeffrey Yu
 * @since 12/10/16.
 */

data class RecentSource(val sourceUri: Uri, val accessTime: Timestamp) {

    constructor(jsonObject: JSONObject) : this(
            Uri.parse(jsonObject.getString("sourceUri")),
            Timestamp.valueOf(jsonObject.getString("accessTime"))
    )

    fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("sourceUri", sourceUri.toString())
        jsonObject.put("accessTime", accessTime.toString())
        return jsonObject
    }
}
