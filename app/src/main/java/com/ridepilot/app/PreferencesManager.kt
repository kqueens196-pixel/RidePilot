package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ridepilot_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var riderPhone: String
        get() = prefs.getString("rider_phone", "") ?: ""
        set(value) = prefs.edit().putString("rider_phone", value).apply()

    var riderName: String
        get() = prefs.getString("rider_name", "Pilot") ?: "Pilot"
        set(value) = prefs.edit().putString("rider_name", value).apply()

    var autoAccept: Boolean
        get() = prefs.getBoolean("auto_accept", false)
        set(value) = prefs.edit().putBoolean("auto_accept", value).apply()

    var isGoHomeEnabled: Boolean
        get() = prefs.getBoolean("go_home_enabled", false)
        set(value) = prefs.edit().putBoolean("go_home_enabled", value).apply()

    var destinationAddress: String
        get() = prefs.getString("dest_address", "") ?: ""
        set(value) = prefs.edit().putString("dest_address", value).apply()

    var destinationRadiusKm: Double
        get() = prefs.getFloat("dest_radius", 5.0f).toDouble()
        set(value) = prefs.edit().putFloat("dest_radius", value.toFloat()).apply()

    var maxPickupKm: Double
        get() = prefs.getFloat("max_pickup", 3.0f).toDouble()
        set(value) = prefs.edit().putFloat("max_pickup", value.toFloat()).apply()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun addAcceptedTrip(trip: AcceptedTrip) {
        val list = getAcceptedTrips().toMutableList()
        list.add(0, trip)
        val array = JSONArray()
        list.take(20).forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("provider", it.provider)
            obj.put("pickup", it.pickup)
            obj.put("drop", it.drop)
            obj.put("fare", it.fare)
            obj.put("time", it.time)
            array.put(obj)
        }
        prefs.edit().putString("trip_logs", array.toString()).apply()
    }

    fun getAcceptedTrips(): List<AcceptedTrip> {
        val jsonStr = prefs.getString("trip_logs", null) ?: return emptyList()
        val list = mutableListOf<AcceptedTrip>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AcceptedTrip(
                        id = obj.getString("id"),
                        provider = obj.getString("provider"),
                        pickup = obj.getString("pickup"),
                        drop = obj.getString("drop"),
                        fare = obj.getString("fare"),
                        time = obj.getString("time")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
