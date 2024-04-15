package com.grimsteel.locationshortcuts.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

@Entity(tableName = "shortcuts", indices = [Index(value = ["latitude", "longitude"], unique = true)])
data class Shortcut(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val lastUsed: Date,
    val region: String,
    val type: String
)

fun List<Shortcut>.toJson(): String {
    // convert to json
    val json = JSONObject()
    val shortcutsArray = JSONArray()
    forEach {
        val shortcutObject = JSONObject().apply {
            put("label", it.label)
            put("address", it.address)
            put("latitude", it.latitude)
            put("longitude", it.longitude)
            put("lastUsed", it.lastUsed.time)
            put("region", it.region)
            put("type", it.type)
        }
        shortcutsArray.put(shortcutObject)
    }
    json.put("shortcuts", shortcutsArray)

    return json.toString()
}

fun parseShortcutListJson(json: JSONObject): List<Shortcut> {
    // convert from json
    val shortcutsArray = json.getJSONArray("shortcuts")
    val shortcuts = List(shortcutsArray.length()) { i ->
        val shortcutObject = shortcutsArray.getJSONObject(i)
        with(shortcutObject) {
            // extract the individual properties
            Shortcut(
                0,
                getString("label"),
                getString("address"),
                getDouble("latitude"),
                getDouble("longitude"),
                Date(getLong("lastUsed")),
                getString("region"),
                getString("type")
            )
        }
    }
    return shortcuts
}