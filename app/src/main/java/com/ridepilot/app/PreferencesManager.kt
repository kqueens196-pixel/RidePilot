package com.ridepilot.app

import android.content.Context
import android.content.SharedPreferences

data class AcceptedTrip(
    val provider: String,
    val fare: String,
    val pickup: String,
    val drop: String
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ridepilot_prefs", Context.MODE_PRIVATE)

    var autoAccept: Boolean
        get() = prefs.getBoolean("auto_accept", false)
        set(value) = prefs.edit().putBoolean("auto_accept", value).apply()

    var maxPickupKm: Float
        get() = prefs.getFloat("max_pickup_km", 2.0f)
        set(value) = prefs.edit().putFloat("max_pickup_km", value).apply()

    var isParcelEnabled: Boolean
        get() = prefs.getBoolean("parcel_enabled", true)
        set(value) = prefs.edit().putBoolean("parcel_enabled", value).apply()

    var isRideEnabled: Boolean
        get() = prefs.getBoolean("ride_enabled", true)
        set(value) = prefs.edit().putBoolean("ride_enabled", value).apply()

    fun getAcceptedTrips(): List<AcceptedTrip> {
        val raw = prefs.getString("accepted_trips_log", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size >= 4) {
                AcceptedTrip(parts[0], parts[1], parts[2], parts[3])
            } else null
        }
    }

    fun addTripLog(trip: AcceptedTrip) {
        val trips = getAcceptedTrips().toMutableList()
        trips.add(0, trip)
        val raw = trips.take(50).joinToString(";;") { it.provider + "|" + it.fare + "|" + it.pickup + "|" + it.drop }
        prefs.edit().putString("accepted_trips_log", raw).apply()
    }

    fun addTripLog(provider: String, fare: String, pickup: String, drop: String) {
        addTripLog(AcceptedTrip(provider, fare, pickup, drop))
    }

    var isGoHomeEnabled: Boolean
        get() = prefs.getBoolean("go_home_enabled", false)
        set(value) = prefs.edit().putBoolean("go_home_enabled", value).apply()

    var destinationAddress: String
        get() = prefs.getString("dest_address", "") ?: ""
        set(value) = prefs.edit().putString("dest_address", value).apply()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun addAcceptedTrip(provider: String, fare: String, pickup: String, drop: String) {
        addTripLog(provider, fare, pickup, drop)
    }

    fun addAcceptedTrip(trip: AcceptedTrip) {
        addTripLog(trip)
    }

    var riderPhone: String
        get() = prefs.getString("rider_phone", "9876543210") ?: "9876543210"
        set(value) = prefs.edit().putString("rider_phone", value).apply()

}
