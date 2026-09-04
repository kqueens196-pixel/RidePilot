package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class AcceptedTrip(
    val id: String,
    val provider: String,
    val pickup: String,
    val drop: String,
    val fare: String,
    val time: String
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ridepilot_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var riderPhone: String
        get() = prefs.getString("rider_phone", "") ?: ""
        set(value) = prefs.edit().putString("rider_phone", value).apply()

    var vehicleType: String
        get() = prefs.getString("vehicle_type", "Bike") ?: "Bike"
        set(value) = prefs.edit().putString("vehicle_type", value).apply()

    var isRideEnabled: Boolean
        get() = prefs.getBoolean("is_ride_enabled", true)
        set(value) = prefs.edit().putBoolean("is_ride_enabled", value).apply()

    var isParcelEnabled: Boolean
        get() = prefs.getBoolean("is_parcel_enabled", true)
        set(value) = prefs.edit().putBoolean("is_parcel_enabled", value).apply()

    var isComboRouteEnabled: Boolean
        get() = prefs.getBoolean("combo_route_500m", true)
        set(value) = prefs.edit().putBoolean("combo_route_500m", value).apply()

    var isGoHomeEnabled: Boolean
        get() = prefs.getBoolean("go_home_enabled", false)
        set(value) = prefs.edit().putBoolean("go_home_enabled", value).apply()

    var destinationAddress: String
        get() = prefs.getString("destination_address", "") ?: ""
        set(value) = prefs.edit().putString("destination_address", value).apply()

    var destinationRadiusKm: Float
        get() = prefs.getFloat("destination_radius_km", 1.0f)
        set(value) = prefs.edit().putFloat("destination_radius_km", value).apply()

    var maxPickupKm: Float
        get() = prefs.getFloat("max_pickup_km", 2.0f)
        set(value) = prefs.edit().putFloat("max_pickup_km", value).apply()

    var autoAccept: Boolean
        get() = prefs.getBoolean("auto_accept", true)
        set(value) = prefs.edit().putBoolean("auto_accept", value).apply()

    // 📜 Save Accepted Trip to History
    fun addAcceptedTrip(trip: AcceptedTrip) {
        val list = getAcceptedTrips().toMutableList()
        list.add(0, trip) // Newest on top
        if (list.size > 20) list.removeAt(list.size - 1) // Keep last 20 trips

        val jsonArray = JSONArray()
        list.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("provider", it.provider)
                put("pickup", it.pickup)
                put("drop", it.drop)
                put("fare", it.fare)
                put("time", it.time)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("accepted_trips_json", jsonArray.toString()).apply()
    }

    fun getAcceptedTrips(): List<AcceptedTrip> {
        val raw = prefs.getString("accepted_trips_json", null) ?: return emptyList()
        val list = mutableListOf<AcceptedTrip>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AcceptedTrip(
                        id = obj.optString("id"),
                        provider = obj.optString("provider"),
                        pickup = obj.optString("pickup"),
                        drop = obj.optString("drop"),
                        fare = obj.optString("fare"),
                        time = obj.optString("time")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
